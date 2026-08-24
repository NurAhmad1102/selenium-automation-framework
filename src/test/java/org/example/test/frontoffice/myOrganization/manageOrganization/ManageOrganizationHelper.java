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

    private void copyFromPreviousProposal(){
        logger.info("Step: Selecting Request Type: Copy From Previous Proposal");
        filterUpdateOrganizationPage.selectRequestType("Copy From Previous Proposal");
    }

//  ####### Perform Steps Helper #######

    public void goToFlow1(){
        navigateToUpdateOrganization();
        setFilterUpdateOrganizationPage(true);
        filterUpdateOrganizationPage.clickNextButton();
        CustomAssert.assertTrue(setOrganizationPage.isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");
    }

    public void nextToAssignEmployee() {
        logger.info("Step: Navigating from Flow 1 Canvas to Flow 2 (Assign Employee)");
        actionElementsPage.clickNextButton();
        setOrganizationHelper.nextToAssignEmployee();
        actionElementsPage.clickYesButtonPopUp();
    }
}
