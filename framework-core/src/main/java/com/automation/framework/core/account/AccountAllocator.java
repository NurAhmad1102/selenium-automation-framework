package com.automation.framework.core.account;

import com.automation.framework.core.config.FrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Agnostic, thread-safe test account allocator.
 * Manages account pooling by role, tracks current account alias per test thread,
 * and ensures accounts are not accessed concurrently by multiple parallel tests.
 */
public final class AccountAllocator {

    public static final String DEFAULT_ACCOUNT_ALIAS = "default";
    private static final Logger logger = LogManager.getLogger(AccountAllocator.class);

    private static final Map<String, List<UserAccount>> roleAccountPool = new ConcurrentHashMap<>();
    private static final Map<UserAccount, Boolean> allocatedAccounts = new ConcurrentHashMap<>();

    private static final ThreadLocal<Map<String, UserAccount>> accountAliases =
            ThreadLocal.withInitial(LinkedHashMap::new);
    private static final ThreadLocal<UserAccount> currentAccount = new ThreadLocal<>();
    private static final ThreadLocal<String> currentAlias = new ThreadLocal<>();

    private AccountAllocator() {
    }

    /**
     * Initializes or registers accounts into the allocator pool.
     */
    public static synchronized void registerAccounts(Collection<? extends UserAccount> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        Map<String, List<UserAccount>> grouped = accounts.stream()
                .collect(Collectors.groupingBy(acc -> normalizeRole(acc.getRole())));
        roleAccountPool.putAll(grouped);
        logger.info("Account pool registered with roles: {}. Total accounts: {}",
                roleAccountPool.keySet(), accounts.size());
    }

    public static void clearPool() {
        roleAccountPool.clear();
        allocatedAccounts.clear();
    }

    /**
     * Allocates an available account for the specified role for the current test thread.
     * Uses the default account alias.
     *
     * @param role role name of the account to allocate
     * @return allocated UserAccount
     */
    public static UserAccount allocate(String role) {
        String testName = Thread.currentThread().getName();
        return useAccount(DEFAULT_ACCOUNT_ALIAS, testName, role);
    }

    /**
     * Releases a specific allocated account back to the pool.
     *
     * @param account account to release
     */
    public static void release(UserAccount account) {
        if (account != null) {
            allocatedAccounts.remove(account);
            accountAliases.get().values().remove(account);
            if (currentAccount.get() == account) {
                currentAccount.remove();
                currentAlias.remove();
            }
            logger.info("Released account: [Role: {}, Identifier: {}]", account.getRole(), account.getIdentifier());
        }
    }

    public static void allocateAccount(String testName, String role) {
        useAccount(DEFAULT_ACCOUNT_ALIAS, testName, role);
    }

    public static UserAccount useAccount(String alias, String testName, String role) {
        String normalizedAlias = normalizeAlias(alias);
        String normalizedRole = normalizeRole(role);
        Map<String, UserAccount> aliases = accountAliases.get();
        UserAccount existingAccount = aliases.get(normalizedAlias);

        if (existingAccount != null) {
            if (!normalizedRole.equalsIgnoreCase(existingAccount.getRole())) {
                throw new IllegalStateException(String.format(
                        "Account alias '%s' already uses role '%s', cannot reuse it as role '%s'.",
                        normalizedAlias, existingAccount.getRole(), normalizedRole));
            }
            setCurrentAccount(normalizedAlias, existingAccount);
            logger.info("Test [{}] reused account alias [{}]: [Role: {}, Identifier: {}]",
                    testName, normalizedAlias, existingAccount.getRole(), existingAccount.getIdentifier());
            return existingAccount;
        }

        UserAccount account = allocateAvailableAccount(testName, normalizedAlias, normalizedRole);
        aliases.put(normalizedAlias, account);
        setCurrentAccount(normalizedAlias, account);

        logger.info("Test [{}] allocated account alias [{}]: [Role: {}, Identifier: {}]",
                testName, normalizedAlias, account.getRole(), account.getIdentifier());

        return account;
    }

    public static UserAccount useAccount(String alias) {
        String normalizedAlias = normalizeAlias(alias);
        UserAccount account = accountAliases.get().get(normalizedAlias);
        if (account == null) {
            throw new IllegalArgumentException("Account alias not found for this test thread: " + normalizedAlias);
        }

        setCurrentAccount(normalizedAlias, account);
        logger.info("Switched current account to alias [{}]: [Role: {}, Identifier: {}]",
                normalizedAlias, account.getRole(), account.getIdentifier());
        return account;
    }

    public static void aliasCurrentAccount(String alias) {
        String normalizedAlias = normalizeAlias(alias);
        UserAccount account = requireCurrentAccount();
        UserAccount existingAccount = accountAliases.get().get(normalizedAlias);
        if (existingAccount != null && existingAccount != account) {
            throw new IllegalStateException(String.format(
                    "Account alias '%s' already points to account '%s', cannot remap it to '%s'.",
                    normalizedAlias, existingAccount.getIdentifier(), account.getIdentifier()));
        }

        accountAliases.get().put(normalizedAlias, account);
        logger.info("Registered current account as alias [{}]: [Role: {}, Identifier: {}]",
                normalizedAlias, account.getRole(), account.getIdentifier());
    }

    private static UserAccount allocateAvailableAccount(String testName, String alias, String role) {
        List<UserAccount> pool = findPoolByRole(role);
        UserAccount account = null;
        long startTime = System.currentTimeMillis();
        long timeoutMs = FrameworkConfig.getAccountWaitTimeoutMs();

        while (account == null) {
            for (UserAccount candidate : pool) {
                if (allocatedAccounts.putIfAbsent(candidate, Boolean.TRUE) == null) {
                    account = candidate;
                    break;
                }
            }
            if (account == null) {
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    logger.error("Timeout waiting for available account for role: {} alias: {}", role, alias);
                    throw new RuntimeException(String.format(
                            "No available accounts for role '%s' after %s seconds. Test: '%s', alias: '%s'.",
                            role, timeoutMs / 1000, testName, alias));
                }
                logger.info("All accounts for role [{}] are busy for alias [{}]. Waiting for release...", role, alias);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for available account.", e);
                }
            }
        }

        return account;
    }

    @SuppressWarnings("unchecked")
    public static <T extends UserAccount> T getCurrentAccount() {
        return (T) currentAccount.get();
    }

    public static String getCurrentAlias() {
        return currentAlias.get();
    }

    @SuppressWarnings("unchecked")
    public static <T extends UserAccount> T getAccount(String alias) {
        return (T) accountAliases.get().get(normalizeAlias(alias));
    }

    public static void releaseAllAccounts() {
        Set<UserAccount> accountsToRelease = new LinkedHashSet<>(accountAliases.get().values());
        if (currentAccount.get() != null) {
            accountsToRelease.add(currentAccount.get());
        }

        for (UserAccount account : accountsToRelease) {
            allocatedAccounts.remove(account);
            logger.info("Released account: [Role: {}, Identifier: {}]", account.getRole(), account.getIdentifier());
        }

        accountAliases.remove();
        currentAccount.remove();
        currentAlias.remove();
    }

    private static void setCurrentAccount(String alias, UserAccount account) {
        currentAlias.set(alias);
        currentAccount.set(account);
    }

    private static UserAccount requireCurrentAccount() {
        UserAccount account = currentAccount.get();
        if (account == null) {
            throw new IllegalStateException("No current account is allocated for this test thread.");
        }
        return account;
    }

    private static List<UserAccount> findPoolByRole(String role) {
        List<UserAccount> pool = roleAccountPool.get(role);
        if (pool == null || pool.isEmpty()) {
            pool = roleAccountPool.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getKey().equalsIgnoreCase(role))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (pool == null || pool.isEmpty()) {
            throw new IllegalArgumentException("No accounts found for role: " + role);
        }

        return pool;
    }

    private static String normalizeAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalArgumentException("Account alias must not be blank.");
        }
        return alias.trim();
    }

    private static String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Account role must not be blank.");
        }
        return role.trim();
    }
}
