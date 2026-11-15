# Multi-Account and Parallel Test Execution

The framework is architected from the ground up for multi-threaded parallel test execution and multi-user workflow testing.

---

## 1. Thread-Safe Account Pooling

In enterprise end-to-end tests, multiple tests running in parallel often need user accounts with specific roles without causing concurrency collisions or session race conditions.

### AccountAllocator Capabilities
- Accounts are grouped by role (for example, `Admin`, `Approver`, `Operator`).
- When a test requests an account with role `R`, `AccountAllocator` assigns an available account and marks it as busy.
- If all accounts of that role are currently in use by other parallel threads, `AccountAllocator` safely waits (default: 30-second timeout, configurable via `account.wait.timeout.seconds`) until an account is released.
- During test teardown (`BaseTest.tearDown()`), all allocated accounts for that thread are automatically released back to the shared pool.

---

## 2. Multi-Actor Workflow Testing

For tests that verify handoffs between multiple users (such as Maker-Checker approval flows):

```java
@Test
public void makerCheckerApprovalFlow() {
    // 1. Allocate initial submitter
    useCurrentUserAs("submitter");
    submitDocument();

    // 2. Switch to approver role
    AccountAllocator.useAccount("approver", currentTestName(), "Manager");
    getLoginHandler().login(DriverManager.getDriver(), account("approver"));
    approveDocument();

    // 3. Switch back to submitter
    AccountAllocator.useAccount("submitter");
    getLoginHandler().login(DriverManager.getDriver(), account("submitter"));
    verifyDocumentStatusApproved();
}
```

---

## 3. Parallel Test Execution via TestNG

Configure parallel execution in your TestNG XML suite:

```xml
<suite name="Regression Suite" parallel="methods" thread-count="4">
    <test name="Regression Tests">
        <packages>
            <package name="com.myproject.tests.*"/>
        </packages>
    </test>
</suite>
```

All framework components use `ThreadLocal`:
- `DriverManager.getDriver()`
- `ReportAPI` event tracking
- `NetworkMonitor`
- `AccountAllocator` current session
