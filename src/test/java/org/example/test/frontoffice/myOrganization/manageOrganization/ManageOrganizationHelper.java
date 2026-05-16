package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;

public class ManageOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final DashboardManageOrganizationPage dashboardManageOrganizationPage;


    public ManageOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.dashboardManageOrganizationPage = mainPagesFactory.manageOrganization()
                .getDashboardManageOrganizationPage();
    }

    public boolean isManageOrganizationPageDisplayed() {
        logger.info("Validate Manage Organization page is displayed");
        return dashboardManageOrganizationPage.isManageOrganizationHeaderDisplayed();
    }

}
