package org.example.test.backoffice.workflowEngine.approvalMatrix;

import org.example.base.BackOfficeBaseTest;
import org.example.test.backoffice.workflowEngine.helper.Helper;
import org.example.utils.account.AccountAllocator;
import org.example.utils.account.UserRole;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AddApprovalMatrix extends BackOfficeBaseTest {

    @Override
    protected String getRole() {
        return UserRole.HR_OD.getValue();
    }
}
