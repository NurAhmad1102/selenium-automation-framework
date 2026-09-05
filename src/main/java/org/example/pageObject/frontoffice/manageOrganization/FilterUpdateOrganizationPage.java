package org.example.pageObject.frontoffice.manageOrganization;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class FilterUpdateOrganizationPage extends BasePage {
    public FilterUpdateOrganizationPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h2[normalize-space()='Update Organization']")
    private WebElement updateOrganizationHeader;

    @FindBy(xpath = "//label[normalize-space()='Request Type']/following-sibling::button")
    private WebElement requestTypeDropdown;

    @FindBy(id = "update-organization-effective-datepicker")
    private WebElement effectiveDateDropdown;

    @FindBy(xpath = "//button[contains(text(), 'Apply')]")
    private WebElement calenderApplyButton;

    @FindBy(xpath = "//div[label[text()[normalize-space()='Directorate']]]//button[@role='combobox']")
    private WebElement directorateDropdown;

    @FindBy(xpath = "//div[label[text()[normalize-space()='Sub Directorate']]]//button[@role='combobox']")
    private WebElement subDirectorateDropdown;

    @FindBy(xpath = "//div[label[text()[normalize-space()='Group']]]//button[@role='combobox']")
    private WebElement groupDropdown;

    @FindBy(xpath = "//div[label[text()[normalize-space()='Division']]]//button[@role='combobox']")
    private WebElement divisionDropdown;

    @FindBy(xpath = "//div[label[text()[normalize-space()='Department']]]//button[@role='combobox']")
    private WebElement departmentDropdown;

    private final By searchDropdownOption = By.xpath("//div[@role='combobox']//input[@placeholder='Search'] | //input[@placeholder='Search']");

    @FindBy(xpath = "//button[normalize-space()='Generate Preview']")
    private WebElement generatePreviewButton;

    @FindBy(css = "div.org-tree")
    private WebElement organizationTree;

    @FindBy(xpath = "//button[text()='Next']")
    private WebElement nextButton;

    public boolean isUpdateOrganizationHeaderDisplayed() {
        return isVisible(updateOrganizationHeader);
    }

    public void clickRequestTypeDropdown() {
        click(requestTypeDropdown);
    }

    public void clickEffectiveDateDropdown() {
        click(effectiveDateDropdown);
    }

    public void clickCalenderApplyButton() { click(calenderApplyButton);}

    public void clickDirectorateDropdown() {
        click(directorateDropdown);
    }

    public void clickSubDirectorateDropdown() {
        click(subDirectorateDropdown);
    }

    public void clickGroupDropdown() {
        click(groupDropdown);
    }

    public void clickDivisionDropdown() {
        click(divisionDropdown);
    }

    public void setSearchDropdownOption(String searchOption) { 
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchDropdownOption));
        typeText(searchInput, searchOption); 
    }

    public void clickDepartmentDropdown() {
        click(departmentDropdown);
    }

    public void clickGeneratePreviewButton() {
        click(generatePreviewButton);
    }

    public boolean isOrganizationTreeDisplayed() {
        return isVisible(organizationTree);
    }

    public void clickNextButton() {
        click(nextButton);
    }

    private String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        if (!text.contains("\"")) {
            return "\"" + text + "\"";
        }

        String[] parts = text.split("'", -1);
        StringBuilder literal = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[i]).append("'");
        }
        literal.append(")");
        return literal.toString();
    }

    public void selectRequestType(String option) {
        if (option == null || option.trim().isEmpty()) {
            throw new IllegalArgumentException("Option must not be blank");
        }
        clickRequestTypeDropdown();
        String optionText = xpathLiteral(option.trim());
        click(By.xpath("//li[normalize-space()=" + optionText + "]"
                + " | //div[@role='option' and normalize-space()=" + optionText + "]"
                + " | //button[normalize-space()=" + optionText + "]"));
    }

    public void selectDirectorate(String option) {
        selectFromSearchableDropdown(directorateDropdown, option);
    }

    public void selectSubDirectorate(String option) {
        selectFromSearchableDropdown(subDirectorateDropdown, option);
    }

    public void selectGroup(String option) {
        selectFromSearchableDropdown(groupDropdown, option);
    }

    public void selectDivision(String option) {
        selectFromSearchableDropdown(divisionDropdown, option);
    }

    public void selectDepartment(String option) {
        selectFromSearchableDropdown(departmentDropdown, option);
    }

    private void selectFromSearchableDropdown(WebElement dropdownElement, String option) {
        if (option == null || option.trim().isEmpty()) {
            throw new IllegalArgumentException("Option must not be blank");
        }
        logger.info("Click dropdown: {}", dropdownElement.getText());
        click(dropdownElement);

        logger.info("Search dropdown option: {}", option);
        setSearchDropdownOption(option);

        String optionXpath =
                "//div[contains(@class,'option') and normalize-space()=" + xpathLiteral(option) + "]"
                        + " | //li[normalize-space()=" + xpathLiteral(option) + "]"
                        + " | //button[normalize-space()=" + xpathLiteral(option) + "]"
                        + " | //span[normalize-space()=" + xpathLiteral(option) + "]/ancestor::*[@role='option']"
                        + " | //*[@role='option' and normalize-space()=" + xpathLiteral(option) + "]";

        WebElement optionElement = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(optionXpath))
        );

        click(optionElement);
    }

    public void selectNewOrganizationEffectiveDate(int effectiveDate) {
        String newEffectiveDate = String.format("//button[@name='day' and text()='%d' and @tabindex='0' and contains(@class, 'text-primary-500')]", effectiveDate);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(newEffectiveDate))).click();
    }
}
