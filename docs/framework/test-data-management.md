# Test Data Management Guide

The framework separates test logic from test data, supporting multiple formats (JSON, Excel, Properties) via an extensible `DataReader<T>` interface.

---

## 1. Core Architecture

```
                    ┌─────────────────────────┐
                    │      DataReader<T>      │ (Interface)
                    └────────────┬────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 ▼                               ▼
       ┌──────────────────┐            ┌──────────────────┐
       │  JsonDataReader  │            │ ExcelDataReader  │
       └──────────────────┘            └──────────────────┘
```

---

## 2. Reading JSON Test Data

Use `JsonDataReader` to map JSON arrays directly into strongly typed Java models or DTOs:

### Step 1: Define your Model class
```java
package com.mycompany.app.models;

import lombok.Data;

@Data
public class UserPayload {
    private String username;
    private String email;
    private String role;
}
```

### Step 2: Place JSON in test resources (`src/test/resources/data/users.json`)
```json
[
  {
    "username": "tester1",
    "email": "tester1@example.com",
    "role": "QA"
  },
  {
    "username": "admin1",
    "email": "admin1@example.com",
    "role": "ADMIN"
  }
]
```

### Step 3: Consume in TestNG DataProvider or Test
```java
import com.automation.framework.core.data.JsonDataReader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;

public class UserRegistrationTest extends SampleBaseTest {

    @DataProvider(name = "userData")
    public Object[][] getUserData() {
        JsonDataReader reader = new JsonDataReader();
        List<UserPayload> users = reader.readList("data/users.json", UserPayload.class);
        return users.stream().map(u -> new Object[]{u}).toArray(Object[][]::new);
    }

    @Test(dataProvider = "userData")
    public void testCreateUser(UserPayload payload) {
        // Test logic using payload.getUsername(), payload.getEmail()
    }
}
```

---

## 3. Reading Excel Test Data

Use `ExcelDataReader` to read spreadsheets dynamically into `List<Map<String, String>>` where keys represent header column names:

```java
import com.automation.framework.core.data.ExcelDataReader;
import java.util.List;
import java.util.Map;

public class CheckoutTest extends SampleBaseTest {

    @Test
    public void testBulkOrder() {
        ExcelDataReader reader = new ExcelDataReader();
        List<Map<String, String>> rows = reader.read("data/orders.xlsx", "Sheet1");

        for (Map<String, String> row : rows) {
            String orderId = row.get("Order ID");
            String quantity = row.get("Quantity");
            String status = row.get("Status");
            // Perform actions
        }
    }
}
```

---

## 4. Resource Path Resolution

`ResourcePathResolver` intelligently resolves paths across classpath resources, root project directories, and filesystem environments:

```java
import com.automation.framework.core.data.ResourcePathResolver;
import java.io.InputStream;

// Automatically checks:
// 1. Classpath (e.g. src/test/resources/data/test.json)
// 2. Direct filesystem path
// 3. User directory relative path
InputStream stream = ResourcePathResolver.resolve("data/test.json");
```
