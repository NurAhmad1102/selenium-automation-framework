package org.example.utils;

import org.example.model.SlaSettingModel;
import org.testng.asserts.SoftAssert;

public class AssertionUtils {

    public static void assertSlaSetting(SlaSettingModel actual,String expectedDate ,String expectedUser, String expectedStatus){
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(actual.getSlaStartDate(), expectedDate, "Start date in table mismatch");
        softAssert.assertEquals(actual.getSlaCreatedBy(), expectedUser, "Created by in table mismatch");
        softAssert.assertEquals(actual.getSlaStatus(), expectedStatus, "Status in table mismatch");
        softAssert.assertAll();
    }

}
