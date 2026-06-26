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
    static Config config = JsonReader.getConfig();

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

        return config.getUserlogin().getPassword();
    }

    /**
     * @deprecated Use {@link UrlProvider#getBackOfficeUrl()} instead.
     */
    @Deprecated
    public static String getBackOfficeUrl(){
        return UrlProvider.getBackOfficeUrl();
    }

    /**
     * @deprecated Use {@link UrlProvider#getFrontOfficeUrl()} instead.
     */
    @Deprecated
    public static String getFrontOfficeUrl(){
        return UrlProvider.getFrontOfficeUrl();
    }

//    rest for nip, username kalo butuh



    // SLA Detail Config
    public static MultipleConfig getMultipleConfig() {
        return config.getSlaDetailProperty().getMultiple();
    }

    public static SingleConfig getSingleConfig() {
        return config.getSlaDetailProperty().getSingle();
    }

    // SLA Period Creation
    public static String getEffTodayPeriodName() {
        return config.getCreate().getEffToday().getSlaPeriod();
    }

    public static String getEffFuturePeriodName() {
        return config.getCreate().getEffFuture().getSlaPeriod();
    }

    public static String getEffFutureDate() {
        return config.getCreate().getEffFuture().getEffDate();
    }

    // SLA Status List
    public static List<String> getSlaStatusList() {
        return config.getSlaSetting().getSlaStatus();
    }

    // Default Actions
//    public static List<String> getDefaultActions() {
//        return ActionType.getAll().subList(0, 3);
//    }

    // Alert Messages
    public static String getEmptySlaPeriodMessage() {
        return config.getAlertMessage().getEmptyMandatoryField().getFieldSlaPeriod();
    }

    public static String getEmptyEffDateMessage() {
        return config.getAlertMessage().getEmptyMandatoryField().getFieldEffDate();
    }

    public static String getEmptySlaDetailMessage() {
        return config.getAlertMessage().getEmptyData().getSlaDetail();
    }

    public static String getSuccessNotifyMessage() {
        return config.getAlertMessage().getNotify().getSuccess();
    }

    public static String getAlreadyExistNotifyMessage() {
        return config.getAlertMessage().getNotify().getAlreadyExist();
    }

    // Confirmation Messages
    public static ConfirmationDetailConfig getAddConfirmation() {
        return config.getConfirmation().getAdd();
    }

    public static ConfirmationDetailConfig getUpdateConfirmation() {
        return config.getConfirmation().getUpdate();
    }

    public static ConfirmationDetailConfig getDeleteConfirmation() {
        return config.getConfirmation().getDelete();
    }

    // Previous SLA
    public static SlaExistingConfig getPreviousSlaExisting() {
        return config.getSlaExisting();
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


