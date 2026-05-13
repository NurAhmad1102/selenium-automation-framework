package org.example.model;

import org.openqa.selenium.WebElement;

import java.util.List;

public class SlaDetailModel {
    private WebElement checkbox;
    private String number;
    private String slaType;
    private String condition;
    private List<String> conditionDetails;
    private String slaDay;
    private WebElement editButton;

    public SlaDetailModel(WebElement checkbox,
                          String number,
                          String slaType,
                          String condition,
                          List<String> conditionDetails,
                          String slaDay,
                          WebElement editButton ){
        this.checkbox = checkbox;
        this.number = number;
        this.slaType = slaType;
        this.condition = condition;
        this.conditionDetails = conditionDetails;
        this.slaDay = slaDay;
        this.editButton = editButton;
    }

    public WebElement getCheckbox() {
        return checkbox;
    }

    public String getNumber() {
        return number;
    }

    public String getSlaType() {
        return slaType;
    }

    public String getCondition() {
        return condition;
    }

    public List<String> getConditionDetails() {
        return conditionDetails;
    }

    public String getSlaDay() {
        return slaDay;
    }

    public WebElement getEditButton() {
        return editButton;
    }
}
