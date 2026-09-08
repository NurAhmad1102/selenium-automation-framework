package org.example.test.backoffice.workflowEngine.approvalMatrix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.backoffice.BackOfficeMainPagesFactory;
import org.example.utils.actionUtils.ActionUtils;
import org.openqa.selenium.WebDriver;

public class ApprovalMatrixHelper {
    private final Logger logger = LogManager.getLogger(getClass());

    ActionUtils actionUtils;


    public ApprovalMatrixHelper(WebDriver driver, BackOfficeMainPagesFactory backOfficeMainPagesFactory){
        actionUtils = new ActionUtils(driver);
    }

}
