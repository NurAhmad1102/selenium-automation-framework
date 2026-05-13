package org.example.utils.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.userLoginStructure.UserloginConfig;
import org.example.utils.ExcelReader;
import org.testng.ITestContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AccountAllocator {
    private static final Logger logger = LogManager.getLogger(AccountAllocator.class);
    private static final Map<String, List<UserloginConfig>> roleAccountPool = new HashMap<>();
    private static final Map<String, AtomicInteger> roleCounters = new HashMap<>();
    private static final ThreadLocal<UserloginConfig> currentAccount = new ThreadLocal<>();

    static {
        String filePath = "src/test/java/resources/data/Data Userlogin.xlsx";
        List<UserloginConfig> allAccounts = ExcelReader.getLoginData(filePath);

        // Group by role
        roleAccountPool.putAll(allAccounts.stream()
                .collect(Collectors.groupingBy(UserloginConfig::getRole)));

        // Initialize counters
        for (String role : roleAccountPool.keySet()) {
            roleCounters.put(role, new AtomicInteger(0));
        }

        logger.info("Account pool initialized with roles: {}. Total accounts: {}",
                roleAccountPool.keySet(), allAccounts.size());
    }

    /**
     * Allocate account based on role. Defaults to first available role if not
     * found.
     */
    public static void allocateAccount(String testName, String role) {
        List<UserloginConfig> pool = roleAccountPool.get(role);

        if (pool == null || pool.isEmpty()) {
            logger.warn("No accounts found for role: {}. Falling back to any available account.", role);
            pool = roleAccountPool.values().stream().flatMap(List::stream).collect(Collectors.toList());
            if (pool.isEmpty()) {
                throw new RuntimeException("Account pool is completely empty!");
            }
        }

        AtomicInteger counter = roleCounters.computeIfAbsent(role, k -> new AtomicInteger(0));
        int index = counter.getAndIncrement() % pool.size();
        UserloginConfig account = pool.get(index);
        currentAccount.set(account);

        logger.info("Test [{}] allocated account: [Role: {}, NIP: {}, Username: {}]",
                testName, account.getRole(), account.getNip(), account.getUsername());
    }

    /**
     * Overloaded method for backward compatibility or when role doesn't matter.
     */
    public static void allocateAccount(String testName) {
        allocateAccount(testName, "Admin"); // Default to Admin
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

    public static String getUrlBackOffice() {
        return currentAccount.get() != null ? currentAccount.get().getUrlBackOffice() : null;
    }

    public static String getUrlFrontOffice() {
        return currentAccount.get() != null ? currentAccount.get().getUrlFrontOffice() : null;
    }

    public static void releaseAccount() {
        currentAccount.remove();
    }
}
