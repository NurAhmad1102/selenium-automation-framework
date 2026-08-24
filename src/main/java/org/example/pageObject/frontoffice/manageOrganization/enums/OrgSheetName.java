package org.example.pageObject.frontoffice.manageOrganization.enums;

public enum OrgSheetName {
    SET_FILTER("SET_FILTER"),
    ADD_NEW("ADD_NEW"),
    RENAME("RENAME"),
    MERGE("MERGE"),
    MOVE("MOVE"),
    SPLIT("SPLIT"),
    REMOVE("REMOVE"),
    JOB_POSITION("JOB_POSITION"),
    ASSIGN_EMPLOYEE("ASSIGN_EMPLOYEE"),
    DUPLICATE("DUPLICATE");

    private final String sheetName;

    OrgSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getSheetName() {
        return sheetName;
    }
}
