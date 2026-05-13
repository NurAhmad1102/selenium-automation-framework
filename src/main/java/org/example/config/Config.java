package org.example.config;

import org.example.config.slaSettingStructure.CreateConfig;
import org.example.config.slaSettingStructure.SlaDetailPropertyConfig;
import org.example.config.slaSettingStructure.SlaSettingConfig;
import org.example.config.slaSettingStructure.alertConfirmation.AlertMessageConfig;
import org.example.config.slaSettingStructure.alertConfirmation.ConfirmationConfig;
import org.example.config.slaSettingStructure.slaPreviousSetting.SlaExistingConfig;
import org.example.config.userLoginStructure.UserloginConfig;


public class Config {
    private SlaSettingConfig slaSetting;
    private UserloginConfig userlogin;

    public SlaSettingConfig getSlaSetting() {
        return slaSetting;
    }

    public void setSlaSetting(SlaSettingConfig slaSetting) {
        this.slaSetting = slaSetting;
    }

    public UserloginConfig getUserlogin() {
        return userlogin;
    }

    public void setUserlogin(UserloginConfig userlogin) {
        this.userlogin = userlogin;
    }

    public SlaDetailPropertyConfig getSlaDetailProperty(){
        return getSlaSetting().getProperty().getSlaDetail();
    }

    public AlertMessageConfig getAlertMessage(){
        return getSlaSetting().getAlertMessage();
    }

    public ConfirmationConfig getConfirmation(){
        return getSlaSetting().getAlertMessage().getConfirmation();
    }

    public CreateConfig getCreate(){
        return getSlaSetting().getCreate();
    }

    public SlaExistingConfig getSlaExisting(){
        return getSlaSetting().getSlaPrevious().getSlaExisting();
    }
}

