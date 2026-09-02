package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.ManageOrganizationExcelReader;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;

import java.time.LocalDate;

public class ManageOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final DashboardManageOrganizationPage dashboardManageOrganizationPage;
    private final FilterUpdateOrganizationPage filterUpdateOrganizationPage;
    private final SetOrganizationPage setOrganizationPage;

    private final ActionElementsPage actionElementsPage;
    private final SetOrganizationHelper setOrganizationHelper;

    public ManageOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this(mainPagesFactory, new SetOrganizationHelper(mainPagesFactory));
    }

    public ManageOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory, SetOrganizationHelper setOrganizationHelper) {
        this.dashboardManageOrganizationPage = mainPagesFactory.manageOrganization()
                .getDashboardManageOrganizationPage();
        this.filterUpdateOrganizationPage = mainPagesFactory.manageOrganization()
                .getFilterUpdateOrganizationPage();
        this.setOrganizationPage = mainPagesFactory.manageOrganization()
                .getSetOrganizationFlow1Page();
        this.actionElementsPage = mainPagesFactory.manageOrganization()
                .getActionElementsPage();
        this.setOrganizationHelper = setOrganizationHelper;
    }

    protected boolean isManageOrganizationPageDisplayed() {
        logger.info("Validate Manage Organization page is displayed");
        return dashboardManageOrganizationPage.isManageOrganizationHeaderDisplayed();
    }

    public void navigateToUpdateOrganization() {
        logger.info("Step: Navigating to Update Organization via Filter Page");
        dashboardManageOrganizationPage.clickUpdateOrganizationButton();

        if (!filterUpdateOrganizationPage.isUpdateOrganizationHeaderDisplayed()) {
            throw new RuntimeException("Failed to navigate to Filter Update Organization Page");
        }
    }

    public void setFilterUpdateOrganizationPage(boolean isNewProposal) {
        logger.info("Step: Setting filter update organization page");
        int todayDate = LocalDate.now().getDayOfMonth();

        if (!isNewProposal) {
            copyFromPreviousProposal();
        } else {
            OrganizationTestData filterData = ManageOrganizationExcelReader.getSingleData(OrgSheetName.SET_FILTER);

            filterUpdateOrganizationPage.clickEffectiveDateDropdown();
            filterUpdateOrganizationPage.selectNewOrganizationEffectiveDate(todayDate);
            filterUpdateOrganizationPage.clickCalenderApplyButton();
            
            logger.info("Step: Selecting Directorate: {}", filterData.getDirectorate());
            filterUpdateOrganizationPage.selectDirectorate(filterData.getDirectorate());
            
            logger.info("Step: Selecting Sub Directorate: {}", filterData.getSubDirectorate());
            filterUpdateOrganizationPage.selectSubDirectorate(filterData.getSubDirectorate());

            filterUpdateOrganizationPage.clickGeneratePreviewButton();
        }
    }

    public void setFilterUpdateOrganizationPage(boolean isNewProposal, String Directorate) {
        logger.info("Step: Setting filter update organization page");
        int todayDate = LocalDate.now().getDayOfMonth();

        if (!isNewProposal) {
            copyFromPreviousProposal();
        } else {
            OrganizationTestData filterData = ManageOrganizationExcelReader.getSingleData(OrgSheetName.SET_FILTER);

            filterUpdateOrganizationPage.clickEffectiveDateDropdown();
            filterUpdateOrganizationPage.selectNewOrganizationEffectiveDate(todayDate);
            filterUpdateOrganizationPage.clickCalenderApplyButton();

            logger.info("Step: Selecting Directorate: {}", Directorate);
            filterUpdateOrganizationPage.selectDirectorate(Directorate);

            filterUpdateOrganizationPage.clickGeneratePreviewButton();
        }
    }

    private void copyFromPreviousProposal(){
        logger.info("Step: Selecting Request Type: Copy From Previous Proposal");
        filterUpdateOrganizationPage.selectRequestType("Copy From Previous Proposal");
    }

    public OrganizationTestData getSheetByName(OrgSheetName sheet){
        logger.info("Get data from Manage Organization Excel");
        OrganizationTestData dataExcel = setOrganizationHelper.getTestData(sheet);
        return dataExcel;
    }

//  ####### Perform Steps Helper #######

    public void goToFlow1SetOrganization(){
        navigateToUpdateOrganization();
        setFilterUpdateOrganizationPage(true);
        filterUpdateOrganizationPage.clickNextButton();
        logger.info("Step: Navigating to Flow 1 (Set Organization)");
        CustomAssert.assertTrue(setOrganizationPage.isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");
    }

    public void goToFlow1SetOrganization(String Directorate){
        navigateToUpdateOrganization();
        setFilterUpdateOrganizationPage(true, Directorate);
        filterUpdateOrganizationPage.clickNextButton();
        CustomAssert.assertTrue(setOrganizationPage.isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");
    }

    public void goToFlow2AssignEmployee() {
        logger.info("Step: Navigating from Flow 1 Canvas to Flow 2 (Assign Employee)");
        passProposedOrgCanvas();
    }

    public void goToFlow3OrgChangesJustification(){
        logger.info("Step: Navigating from Flow 2 Canvas to Flow 3 (Organization Changes Justification)");
        passProposedOrgCanvas();
    }

    private void passProposedOrgCanvas(){
        actionElementsPage.clickNextButton();
        setOrganizationHelper.nextToAssignEmployee();
        actionElementsPage.clickYesButtonPopUp();
    }

    public void saveAsDraft(){
        logger.info("Step: Saving current changes as Draft");
        actionElementsPage.clickSaveAsDraftButton();
        actionElementsPage.clickYesButtonPopUp();
    }
}
