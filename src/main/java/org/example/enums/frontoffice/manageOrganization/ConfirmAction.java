package org.example.enums.frontoffice.manageOrganization;

public enum ConfirmAction {
    ADD("Add", "Added"),
    RENAME("Rename", "Renamed"),
    MOVE("Move", "Moved"),
    MERGE("Merge", "Merged"),
    SPLIT("Split", "Split"),
    REMOVE("Remove", "Removed");

    private final String actionName;
    private final String successPastTense;

    ConfirmAction(String actionName, String successPastTense) {
        this.actionName = actionName;
        this.successPastTense = successPastTense;
    }

    public String getActionName() {
        return actionName;
    }

    public String getSuccessPastTense() {
        return successPastTense;
    }
}
