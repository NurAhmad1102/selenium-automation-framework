package org.example.config.slaSettingStructure.slaPreviousSetting;

public class SlaDetailItem {
    private int No;
    private String Type;
    private String Condition;
    private String ConditionDetail;
    private String SLA;

    public int getNo() { return No; }
    public void setNo(int No) { this.No = No; }

    public String getType() { return Type; }
    public void setType(String Type) { this.Type = Type; }

    public String getCondition() { return Condition; }
    public void setCondition(String Condition) { this.Condition = Condition; }

    public String getConditionDetail() { return ConditionDetail; }
    public void setConditionDetail(String ConditionDetail) { this.ConditionDetail = ConditionDetail; }

    public String getSLA() { return SLA; }
    public void setSLA(String SLA) { this.SLA = SLA; }
}
