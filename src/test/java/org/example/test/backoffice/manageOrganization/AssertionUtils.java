package org.example.test.backoffice.manageOrganization;

import org.example.model.SlaSettingModel;
import org.example.utils.CustomSoftAssert;

public class AssertionUtils {

    public static void assertSlaSetting(SlaSettingModel actual,String expectedDate ,String expectedUser, String expectedStatus){
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertEquals(actual.getSlaStartDate(), expectedDate, "Start date in table mismatch");
        softAssert.assertEquals(actual.getSlaCreatedBy(), expectedUser, "Created by in table mismatch");
        softAssert.assertEquals(actual.getSlaStatus(), expectedStatus, "Status in table mismatch");
        softAssert.assertAll();
    }

}
