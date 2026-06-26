package org.example.utils.account;

public enum UserRole {
    HR_OD("HR OD"),
    HR_PSOM("HR PSOM");

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
