package org.example.config.slaSettingStructure.alertConfirmation;

public class AlertMessageConfig {
    private EmptyMandatoryFieldConfig emptyMandatoryField;
    private EmptyDataConfig emptyData;
    private ConfirmationConfig confirmation;
    private NotifyConfig notify;

    public EmptyMandatoryFieldConfig getEmptyMandatoryField() { return emptyMandatoryField; }
    public void setEmptyMandatoryField(EmptyMandatoryFieldConfig emptyMandatoryField) { this.emptyMandatoryField = emptyMandatoryField; }

    public EmptyDataConfig getEmptyData() { return emptyData; }
    public void setEmptyData(EmptyDataConfig emptyData) { this.emptyData = emptyData; }

    public ConfirmationConfig getConfirmation() { return confirmation; }
    public void setConfirmation(ConfirmationConfig confirmation) { this.confirmation = confirmation; }

    public NotifyConfig getNotify() { return notify; }
    public void setNotify(NotifyConfig notify) { this.notify = notify; }
}
