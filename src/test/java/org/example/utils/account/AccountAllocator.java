package org.example.utils.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.userLoginStructure.UserloginConfig;
import org.example.utils.config.LoginDataRepository;
import org.example.utils.config.UrlProvider;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AccountAllocator {
    public static final String DEFAULT_ACCOUNT_ALIAS = "default";

    private static final Logger logger = LogManager.getLogger(AccountAllocator.class);
    private static final Map<String, List<UserloginConfig>> roleAccountPool = new ConcurrentHashMap<>();
    private static final Map<UserloginConfig, Boolean> allocatedAccounts = new ConcurrentHashMap<>();
    private static final ThreadLocal<Map<String, UserloginConfig>> accountAliases =
            ThreadLocal.withInitial(LinkedHashMap::new);
    private static final ThreadLocal<UserloginConfig> currentAccount = new ThreadLocal<>();
    private static final ThreadLocal<String> currentAlias = new ThreadLocal<>();

    static {
        // Group by role
        roleAccountPool.putAll(LoginDataRepository.getLoginData().getAccounts().stream()
                .collect(Collectors.groupingBy(UserloginConfig::getRole)));

        logger.info("Account pool initialized with roles: {}. Total accounts: {}",
                roleAccountPool.keySet(), LoginDataRepository.getLoginData().getAccounts().size());
    }

    /**
     * Allocate the default account for a test method. The account is stored under
     * the "default" alias so multi-user tests can switch back to the initial user.
     */
    public static void allocateAccount(String testName, String role) {
        useAccount(DEFAULT_ACCOUNT_ALIAS, testName, role);
    }

    public static UserloginConfig useAccount(String alias, String testName, String role) {
        String normalizedAlias = normalizeAlias(alias);
        String normalizedRole = normalizeRole(role);
        Map<String, UserloginConfig> aliases = accountAliases.get();
        UserloginConfig existingAccount = aliases.get(normalizedAlias);

        if (existingAccount != null) {
            if (!normalizedRole.equalsIgnoreCase(existingAccount.getRole())) {
                throw new IllegalStateException(String.format(
                        "Account alias '%s' already uses role '%s', cannot reuse it as role '%s'.",
                        normalizedAlias, existingAccount.getRole(), normalizedRole));
            }
            setCurrentAccount(normalizedAlias, existingAccount);
            logger.info("Test [{}] reused account alias [{}]: [Role: {}, NIP: {}, Username: {}]",
                    testName, normalizedAlias, existingAccount.getRole(),
                    existingAccount.getNip(), existingAccount.getUsername());
            return existingAccount;
        }

        UserloginConfig account = allocateAvailableAccount(testName, normalizedAlias, normalizedRole);
        aliases.put(normalizedAlias, account);
        setCurrentAccount(normalizedAlias, account);

        logger.info("Test [{}] allocated account alias [{}]: [Role: {}, NIP: {}, Username: {}]",
                testName, normalizedAlias, account.getRole(), account.getNip(), account.getUsername());

        return account;
    }

    public static UserloginConfig useAccount(String alias) {
        String normalizedAlias = normalizeAlias(alias);
        UserloginConfig account = accountAliases.get().get(normalizedAlias);
        if (account == null) {
            throw new IllegalArgumentException("Account alias not found for this test thread: " + normalizedAlias);
        }

        setCurrentAccount(normalizedAlias, account);
        logger.info("Switched current account to alias [{}]: [Role: {}, NIP: {}, Username: {}]",
                normalizedAlias, account.getRole(), account.getNip(), account.getUsername());
        return account;
    }

    public static void aliasCurrentAccount(String alias) {
        String normalizedAlias = normalizeAlias(alias);
        UserloginConfig account = requireCurrentAccount();
        UserloginConfig existingAccount = accountAliases.get().get(normalizedAlias);
        if (existingAccount != null && existingAccount != account) {
            throw new IllegalStateException(String.format(
                    "Account alias '%s' already points to NIP '%s', cannot remap it to NIP '%s'.",
                    normalizedAlias, existingAccount.getNip(), account.getNip()));
        }

        accountAliases.get().put(normalizedAlias, account);
        logger.info("Registered current account as alias [{}]: [Role: {}, NIP: {}, Username: {}]",
                normalizedAlias, account.getRole(), account.getNip(), account.getUsername());
    }

    private static UserloginConfig allocateAvailableAccount(String testName, String alias, String role) {
        List<UserloginConfig> pool = findPoolByRole(role);
        UserloginConfig account = null;
        long startTime = System.currentTimeMillis();
        long timeoutMs = getAccountWaitTimeoutMs();

        while (account == null) {
            for (UserloginConfig candidate : pool) {
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
                logger.info("All accounts for role [{}] are busy for alias [{}]. Waiting for release...",
                        role, alias);
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

    /**
     * Overloaded method for backward compatibility or when role doesn't matter.
     */
    public static void allocateAccount(String testName) {
        allocateAccount(testName, "Admin"); // Default to Admin
    }

    public static UserloginConfig getCurrentAccount() {
        return currentAccount.get();
    }

    public static String getCurrentAlias() {
        return currentAlias.get();
    }

    public static UserloginConfig getAccount(String alias) {
        return accountAliases.get().get(normalizeAlias(alias));
    }

    public static String getNip(String alias) {
        UserloginConfig account = getAccount(alias);
        return account != null ? account.getNip() : null;
    }

    public static String getUsername(String alias) {
        UserloginConfig account = getAccount(alias);
        return account != null ? account.getUsername() : null;
    }

    public static String getNip() {
        return currentAccount.get() != null ? currentAccount.get().getNip() : null;
    }

    public static String getUsername() {
        return currentAccount.get() != null ? currentAccount.get().getUsername() : null;
    }

    public static String getPassword() {
        return currentAccount.get() != null ? currentAccount.get().getPassword() : null;
    }

    /**
     * @deprecated Use {@link UrlProvider#getUrl(String)} instead.
     */
    @Deprecated
    public static String getUrl(String interfaceType) {
        return UrlProvider.getUrl(interfaceType);
    }

    /**
     * @deprecated Use {@link UrlProvider#getBackOfficeUrl()} instead.
     */
    @Deprecated
    public static String getUrlBackOffice() {
        return getUrl("backoffice");
    }

    /**
     * @deprecated Use {@link UrlProvider#getFrontOfficeUrl()} instead.
     */
    @Deprecated
    public static String getUrlFrontOffice() {
        return getUrl("frontoffice");
    }

    /**
     * @deprecated Use {@link #releaseAllAccounts()} to release every account alias
     * allocated in the current test thread.
     */
    @Deprecated
    public static void releaseAccount() {
        UserloginConfig account = currentAccount.get();
        if (account != null) {
            accountAliases.get().entrySet().removeIf(entry -> entry.getValue() == account);
            allocatedAccounts.remove(account);
            logger.info("Released account: [Role: {}, Username: {}]", account.getRole(), account.getUsername());
        }
        currentAccount.remove();
        currentAlias.remove();
    }

    public static void releaseAllAccounts() {
        Set<UserloginConfig> accountsToRelease = new LinkedHashSet<>(accountAliases.get().values());
        if (currentAccount.get() != null) {
            accountsToRelease.add(currentAccount.get());
        }

        for (UserloginConfig account : accountsToRelease) {
            allocatedAccounts.remove(account);
            logger.info("Released account: [Role: {}, Username: {}]", account.getRole(), account.getUsername());
        }

        accountAliases.remove();
        currentAccount.remove();
        currentAlias.remove();
    }

    private static void setCurrentAccount(String alias, UserloginConfig account) {
        currentAlias.set(alias);
        currentAccount.set(account);
    }

    private static UserloginConfig requireCurrentAccount() {
        UserloginConfig account = currentAccount.get();
        if (account == null) {
            throw new IllegalStateException("No current account is allocated for this test thread.");
        }
        return account;
    }

    private static List<UserloginConfig> findPoolByRole(String role) {
        List<UserloginConfig> pool = roleAccountPool.get(role);
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

    private static long getAccountWaitTimeoutMs() {
        String timeoutSeconds = System.getProperty("account.wait.timeout.seconds", "30");
        try {
            long parsedTimeout = Long.parseLong(timeoutSeconds);
            if (parsedTimeout > 0) {
                return parsedTimeout * 1000;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid account.wait.timeout.seconds value: {}. Falling back to 30 seconds.", timeoutSeconds);
        }
        return 30000;
    }
}
