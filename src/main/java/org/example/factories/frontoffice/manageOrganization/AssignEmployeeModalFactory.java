package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.AddIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.EmployeeMovementModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.ManageIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.UnassignedEmployeeListModal;
import org.openqa.selenium.WebDriver;

public class AssignEmployeeModalFactory {
    private final WebDriver driver;
    private AddIncumbentModal addIncumbentModal;
    private EmployeeMovementModal employeeMovementModal;
    private ManageIncumbentModal manageIncumbentModal;
    private UnassignedEmployeeListModal unassignedEmployeeListModal;

    public AssignEmployeeModalFactory(WebDriver driver) {
        this.driver = driver;
    }

    public AddIncumbentModal getAddIncumbentModal() {
        if (addIncumbentModal == null) {
            addIncumbentModal = new AddIncumbentModal(driver);
        }
        return addIncumbentModal;
    }

    public EmployeeMovementModal getEmployeeMovementModal() {
        if (employeeMovementModal == null) {
            employeeMovementModal = new EmployeeMovementModal(driver);
        }
        return employeeMovementModal;
    }

    public ManageIncumbentModal getManageIncumbentModal() {
        if (manageIncumbentModal == null) {
            manageIncumbentModal = new ManageIncumbentModal(driver);
        }
        return manageIncumbentModal;
    }

    public UnassignedEmployeeListModal getUnassignedEmployeeListModal() {
        if (unassignedEmployeeListModal == null) {
            unassignedEmployeeListModal = new UnassignedEmployeeListModal(driver);
        }
        return unassignedEmployeeListModal;
    }
}
