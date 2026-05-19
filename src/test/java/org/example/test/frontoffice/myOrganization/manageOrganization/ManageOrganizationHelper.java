package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;

import java.time.LocalDate;

public class ManageOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final DashboardManageOrganizationPage dashboardManageOrganizationPage;
    private final FilterUpdateOrganizationPage filterUpdateOrganizationPage;
    private final SetOrganizationPage setOrganizationPage;

//    TODO: ganti data ini nanti
    String directorate = "O522 Network & Digital Banking ID";
    String sub_directorate = "1222 Branch Transformation & Network Planning ID";

    protected ManageOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.dashboardManageOrganizationPage = mainPagesFactory.manageOrganization()
                .getDashboardManageOrganizationPage();
        this.filterUpdateOrganizationPage = mainPagesFactory.manageOrganization()
                .getFilterUpdateOrganizationPage();
        this.setOrganizationPage = mainPagesFactory.manageOrganization()
                .getSetOrganizationFlow1Page();
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
            filterUpdateOrganizationPage.clickEffectiveDateDropdown();
            filterUpdateOrganizationPage.selectNewOrganizationEffectiveDate(todayDate);
            filterUpdateOrganizationPage.clickCalenderApplyButton();
            
            logger.info("Step: Selecting Directorate: {}", directorate);
            filterUpdateOrganizationPage.selectDirectorate(directorate);
            
            logger.info("Step: Selecting Sub Directorate: {}", sub_directorate);
            filterUpdateOrganizationPage.selectSubDirectorate(sub_directorate);
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
    }
}
