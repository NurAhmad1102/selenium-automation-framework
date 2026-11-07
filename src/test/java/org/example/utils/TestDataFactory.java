package org.example.utils;

import org.example.config.Config;
import org.example.config.slaSettingStructure.MultipleConfig;
import org.example.config.slaSettingStructure.SingleConfig;
import org.example.config.slaSettingStructure.SlaConfig;
import org.example.config.slaSettingStructure.alertConfirmation.ConfirmationDetailConfig;
import org.example.config.slaSettingStructure.slaPreviousSetting.SlaExistingConfig;
import org.example.utils.actionUtils.JsonReader;
import org.example.utils.config.UrlProvider;

import java.util.ArrayList;
import java.util.List;

public class TestDataFactory{
    private static Config config;

    private static synchronized Config getConfig() {
        if (config == null) {
            config = JsonReader.getConfig();
        }
        return config;
    }

    //   User Login
    public static String getPassword(){
        String systemPassword = System.getProperty("automation.password");
        if (systemPassword != null && !systemPassword.isBlank()) {
            return systemPassword;
        }

        String envPassword = System.getenv("AUTOMATION_PASSWORD");
        if (envPassword != null && !envPassword.isBlank()) {
            return envPassword;
        }

        Config cfg = getConfig();
        return (cfg != null && cfg.getUserlogin() != null) ? cfg.getUserlogin().getPassword() : null;
    }

//    rest for nip, username kalo butuh



    // SLA Detail Config
    public static MultipleConfig getMultipleConfig() {
        return getConfig().getSlaDetailProperty().getMultiple();
    }

    public static SingleConfig getSingleConfig() {
        return getConfig().getSlaDetailProperty().getSingle();
    }

    // SLA Period Creation
    public static String getEffTodayPeriodName() {
        return getConfig().getCreate().getEffToday().getSlaPeriod();
    }

    public static String getEffFuturePeriodName() {
        return getConfig().getCreate().getEffFuture().getSlaPeriod();
    }

    public static String getEffFutureDate() {
        return getConfig().getCreate().getEffFuture().getEffDate();
    }

    // SLA Status List
    public static List<String> getSlaStatusList() {
        return getConfig().getSlaSetting().getSlaStatus();
    }

    // Default Actions
//    public static List<String> getDefaultActions() {
//        return ActionType.getAll().subList(0, 3);
//    }

    // Alert Messages
    public static String getEmptySlaPeriodMessage() {
        return getConfig().getAlertMessage().getEmptyMandatoryField().getFieldSlaPeriod();
    }

    public static String getEmptyEffDateMessage() {
        return getConfig().getAlertMessage().getEmptyMandatoryField().getFieldEffDate();
    }

    public static String getEmptySlaDetailMessage() {
        return getConfig().getAlertMessage().getEmptyData().getSlaDetail();
    }

    public static String getSuccessNotifyMessage() {
        return getConfig().getAlertMessage().getNotify().getSuccess();
    }

    public static String getAlreadyExistNotifyMessage() {
        return getConfig().getAlertMessage().getNotify().getAlreadyExist();
    }

    // Confirmation Messages
    public static ConfirmationDetailConfig getAddConfirmation() {
        return getConfig().getConfirmation().getAdd();
    }

    public static ConfirmationDetailConfig getUpdateConfirmation() {
        return getConfig().getConfirmation().getUpdate();
    }

    public static ConfirmationDetailConfig getDeleteConfirmation() {
        return getConfig().getConfirmation().getDelete();
    }

    // Previous SLA
    public static SlaExistingConfig getPreviousSlaExisting() {
        return getConfig().getSlaExisting();
    }

    // Utility to build expected SLA detail row
    public static List<String> buildExpectedDetail(SlaConfig config, List<String> actions) {
        List<String> expectedDetail = new ArrayList<>(List.of(
                config.getSlaType(),
                config.getCondition(),
                config.getSlaDay()
        ));
        expectedDetail.addAll(2, actions); // insert actions after condition
        return expectedDetail;
    }
}


