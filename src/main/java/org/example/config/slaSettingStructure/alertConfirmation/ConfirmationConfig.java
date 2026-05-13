package org.example.config.slaSettingStructure.alertConfirmation;

public class ConfirmationConfig {
    private ConfirmationDetailConfig add;
    private ConfirmationDetailConfig update;
    private ConfirmationDetailConfig delete;

    public ConfirmationDetailConfig getAdd() { return add; }
    public void setAdd(ConfirmationDetailConfig add) { this.add = add; }

    public ConfirmationDetailConfig getUpdate() { return update; }
    public void setUpdate(ConfirmationDetailConfig update) { this.update = update; }

    public ConfirmationDetailConfig getDelete() { return delete; }
    public void setDelete(ConfirmationDetailConfig delete) { this.delete = delete; }
}
