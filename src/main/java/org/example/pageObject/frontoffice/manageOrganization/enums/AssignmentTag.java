package org.example.pageObject.frontoffice.manageOrganization.enums;

public enum AssignmentTag {
    NEW("New"),
    MOVE("Move"),
    PJS("PJS"),
    PJS_NEW("PJS", "New"),
    NONE();

    private final String[] tags;

    AssignmentTag(String... tags) {
        this.tags = tags;
    }

    public String[] getTags() {
        return tags;
    }
}
