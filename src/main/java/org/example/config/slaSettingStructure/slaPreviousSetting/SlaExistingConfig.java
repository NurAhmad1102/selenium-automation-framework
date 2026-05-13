package org.example.config.slaSettingStructure.slaPreviousSetting;

import java.util.List;

public class SlaExistingConfig {
    private String slaPeriod;
    private List<SlaDetailItem> slaDetail;

    public String getSlaPeriod() { return slaPeriod; }
    public void setSlaPeriod(String slaPeriod) { this.slaPeriod = slaPeriod; }

    public List<SlaDetailItem> getSlaDetail() { return slaDetail; }
    public void setSlaDetail(List<SlaDetailItem> slaDetail) { this.slaDetail = slaDetail; }
}
