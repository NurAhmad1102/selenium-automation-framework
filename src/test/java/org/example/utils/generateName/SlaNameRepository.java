package org.example.utils.generateName;

import java.util.ArrayList;
import java.util.List;

public class SlaNameRepository {
    private List<String> slaNames;

    protected SlaNameRepository(){
        slaNames = new ArrayList<>();
        slaNames.add("SLA");
        slaNames.add("Quarter");
        slaNames.add("Report");
        slaNames.add("Analysis");
        slaNames.add("Status");
        slaNames.add("Summary");
        slaNames.add("Update");
        slaNames.add("Assessment");
        slaNames.add("Review");
        slaNames.add("Performance");
        slaNames.add("Division");
        slaNames.add("Department");
    }

    public List<String> getSlaNames(){
        return slaNames;
    }

}
