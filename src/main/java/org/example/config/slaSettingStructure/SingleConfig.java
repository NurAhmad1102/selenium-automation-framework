package org.example.config.slaSettingStructure;

public class SingleConfig implements SlaConfig {
    private String slaType;
    private String slaDay;
    private String condition;

    public String getSlaType() { return slaType; }
    public void setSlaType(String slaType) { this.slaType = slaType; }

    public String getSlaDay() { return slaDay; }
    public void setSlaDay(String slaDay) { this.slaDay = slaDay; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
