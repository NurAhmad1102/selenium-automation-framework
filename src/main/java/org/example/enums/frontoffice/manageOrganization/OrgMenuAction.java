package org.example.enums.frontoffice.manageOrganization;

public enum OrgMenuAction {
    RENAME("Rename"),
    MOVE("Move"),
    MERGE("Merge"),
    SPLIT("Split"),
    REMOVE("Remove"),
    ADD_NEW("Add New Organization"),
    EXPAND("Expand Organization");

    private final String actionName;

    OrgMenuAction(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return this.actionName;
    }
}
