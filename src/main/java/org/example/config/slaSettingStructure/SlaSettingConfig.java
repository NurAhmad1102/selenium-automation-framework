package org.example.config.slaSettingStructure;

import org.example.config.slaSettingStructure.alertConfirmation.AlertMessageConfig;
import org.example.config.slaSettingStructure.slaPreviousSetting.SlaPreviousConfig;

import java.util.List;

public class SlaSettingConfig {
    private CreateConfig create;
    private List<String> slaStatus;
    private SlaPreviousConfig slaPrevious;
    private PropertyConfig property;
    private AlertMessageConfig alertMessage;

    public CreateConfig getCreate() { return create; }
    public void setCreate(CreateConfig create) { this.create = create; }

    public List<String> getSlaStatus() { return slaStatus; }
    public void setSlaStatus(List<String> slaStatus) { this.slaStatus = slaStatus; }

    public SlaPreviousConfig getSlaPrevious() { return slaPrevious; }
    public void setSlaPrevious(SlaPreviousConfig slaPrevious) { this.slaPrevious = slaPrevious; }

    public PropertyConfig getProperty() { return property; }
    public void setProperty(PropertyConfig property) { this.property = property; }

    public AlertMessageConfig getAlertMessage() { return alertMessage; }
    public void setAlertMessage(AlertMessageConfig alertMessage) { this.alertMessage = alertMessage; }
}
