package org.example.test.backoffice.manageOrganization.slaSetting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.slaSettingStructure.MultipleConfig;
import org.example.config.slaSettingStructure.SingleConfig;
import org.example.config.slaSettingStructure.SlaConfig;
import org.example.mainPagesFactory.BackOfficeMainPagesFactory;
import org.example.pageObject.backoffice.slaSettingMenu.SlaSetting.SlaSettingPage;
import org.example.pageObject.backoffice.slaSettingMenu.DashboardSlaSettingPage;
import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.ActionSelectorModal;
import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.SlaDetailModal;
import org.example.utils.actionUtils.ActionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

public class SlaSettingHelper {
    private final Logger logger = LogManager.getLogger(getClass());

    DashboardSlaSettingPage dashboardSlaSettingPage;
    SlaSettingPage slaSettingPage;
    ActionUtils actionUtils;
    SlaDetailModal slaDetailModal;
    ActionSelectorModal actionSelectorModal;


    public SlaSettingHelper(BackOfficeMainPagesFactory backOfficeMainPagesFactory, ActionUtils actionUtils){
        this.actionUtils = actionUtils;
        dashboardSlaSettingPage = backOfficeMainPagesFactory.manageOrganization()
                .getDashboardSlaSettingPageFactory()
                .getDashboardSlaSettingPage();
        slaSettingPage = backOfficeMainPagesFactory.manageOrganization()
                .getSlaSettingFactory()
                .getSlaSettingPage();
        slaDetailModal = backOfficeMainPagesFactory.manageOrganization()
                .getModalFactory()
                .getAddSlaDetailModalFactory()
                .getSlaDetailModal();
        actionSelectorModal = backOfficeMainPagesFactory.manageOrganization()
                .getModalFactory()
                .getAddSlaDetailModalFactory()
                .getActionSelectorModal();
    }


    public String addSlaSettingHelper(String periodName, boolean futureEffDate) {
        dashboardSlaSettingPage.clickAddSlaSettingButton();
        slaSettingPage.isAddSlaSettingPage();
        slaSettingPage.setPeriodNameField().sendKeys(periodName);
        slaSettingPage.clickEffectiveDateField();
        return slaSettingPage.pickEffectiveDate(futureEffDate);
    }

    public String createSlaSetting(String periodName, boolean futureDate, MultipleConfig config, List<String> actions){
        logger.info("Create SLA Setting...");

        dashboardSlaSettingPage.clickAddSlaSettingButton();
        slaSettingPage.isAddSlaSettingPage();

        slaSettingPage.setPeriodNameField().sendKeys(periodName);
        logger.info("SLA Period Name...{}", periodName);

        slaSettingPage.clickEffectiveDateField();
        String effectiveDate = slaSettingPage.pickEffectiveDate(futureDate);
        logger.info("SLA Effective Date...{}", futureDate);

        addSlaDetailHelper(config.getSlaType(), config.getSlaDay(), actions);
        return effectiveDate;
    }

    public String createSlaSetting(String periodName, boolean futureDate, SingleConfig config, String action){
        dashboardSlaSettingPage.clickAddSlaSettingButton();
        slaSettingPage.isAddSlaSettingPage();

        slaSettingPage.setPeriodNameField().sendKeys(periodName);
        slaSettingPage.clickEffectiveDateField();
        String effectiveDate = slaSettingPage.pickEffectiveDate(futureDate);

        addSlaDetailHelper(config.getSlaType(), config.getSlaDay(), action);
        return effectiveDate;
    }

    public void submitSlaSetting(){
        slaSettingPage.clickSubmitButton();
        slaDetailModal.clickCreateYesButton();
        Assert.assertTrue(slaDetailModal.isHeaderPopUpSuccess());
        slaDetailModal.clickDonePopUpButton();
        Assert.assertTrue(dashboardSlaSettingPage.isSlaSettingPage());
    }

    public List<String> buildExpectedDetail(SlaConfig config, List<String> actions){
        List<String> expectedDetail = new ArrayList<>(List.of(
                config.getSlaType(),
                config.getCondition(),
                config.getSlaDay()
        ));
        expectedDetail.addAll(2, actions);
        return expectedDetail;
    }


    public void addSlaDetailHelper(String slaType,String slaDay , Object data){
        slaSettingPage.clickAddSlaDetailButton();
        slaDetailModal.isAddSlaDetailHeader();
        slaDetail(false,slaType, slaDay, data);

    }

    public void editSlaDetailHelper(String slaType, String slaDay , Object data) {
        slaDetailModal.isEditSlaDetailHeader();
        slaDetail(true,slaType, slaDay, data);

    }

    private void slaDetail(boolean isActionEdit,String type, String day, Object data) {
        WebElement inputType = slaDetailModal.setSlaDetailType();
        WebElement inputSlaDay = slaDetailModal.setSlaDetailSlaDay();

//        pre-condition
        if(isActionEdit){
            actionUtils.clearInputWithKeys(inputType);
            actionUtils.clearInputWithKeys(inputSlaDay);
        }

        inputType.sendKeys(type);
        slaDetailModal.clickSlaDetailCondition();

        if(data instanceof List){
            List<String> actions = (List<String>) data;
            slaDetailModal.clickMultipleChanges();
            actionUtils.resetCheckboxes(By.cssSelector("#actions input[type='checkbox']"));
            //        select checkbox sla condition
            for (String action: actions){
                actionSelectorModal.selectMultipleAction(action);
            }
        } else if (data instanceof String){
            String actions = (String) data;
            slaDetailModal.clickSingleChange();
            actionSelectorModal.selectSingleAction(actions);
        } else {
            throw new IllegalArgumentException("Data harus bertipe String atau List<String>");
        }

        slaDetailModal.setSlaDetailSlaDay().sendKeys(day);
        slaDetailModal.clickSaveButton();
    }

}
