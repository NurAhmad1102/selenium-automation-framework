package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateOrganizationFilterTest extends ManageOrganizationBaseTest{

    @Test
    public void validateGeneratePreviewTest(){
        logger.info("Starting test: filterUpdateOrganizationTest");
        manageOrganizationHelper().navigateToUpdateOrganization();
        manageOrganizationHelper().setFilterUpdateOrganizationPage(true);
        Assert.assertTrue(filterUpdateOrganizationPage().isOrganizationTreeDisplayed(), "Organization Tree is not generated");
    }
}
