package org.example.model;

import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

public class SlaSettingModel {
    private String startDate;
    private String createdBy;
    private String status;
    private Map<String, WebElement> buttonElements;

    public SlaSettingModel(String startDate, String createdBy, String status){
        this.startDate = startDate;
        this.createdBy = createdBy;
        this.status = status;
        this.buttonElements = new HashMap<>();
    }

    public SlaSettingModel(String startDate, String createdBy, String status, Map<String, WebElement> buttonElements){
        this.startDate = startDate;
        this.createdBy = createdBy;
        this.status = status;
        this.buttonElements = buttonElements;
    }

    public String getSlaStartDate(){
        return startDate;
    }

    public String getSlaCreatedBy(){
        return createdBy;
    }

    public String getSlaStatus(){
        return status;
    }

//    for button
    private Map<String, WebElement> getButtonElements(){
        return buttonElements;
    }

    private void setButtonElements(Map<String, WebElement> buttonElements){
        this.buttonElements = buttonElements;
    }

    public WebElement getEditButton(){
        return buttonElements.get("edit");
    }

    public WebElement getDeleteButton(){
        return buttonElements.get("delete");
    }

    public WebElement getDetailButton(){
        return buttonElements.get("eye");
    }

}
