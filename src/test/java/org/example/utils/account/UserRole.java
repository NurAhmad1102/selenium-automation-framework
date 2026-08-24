package org.example.utils.account;

public enum UserRole {
    HR_OD("HR_OD"),
    HR_PSOM("HR_PSOM"),
    USER_MOBILE("USER_MOBILE"),
    ADMIN_ARJUNA("ADMIN_ARJUNA");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
