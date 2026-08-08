package org.example.pageObject.frontoffice.frontOfficeSideMenu;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import org.example.enums.frontoffice.FrontOfficeMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FrontOfficeSideMenu extends BasePage {

    public FrontOfficeSideMenu(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    /**
     * Method dinamis untuk melakukan klik / ekspansi pada Main Menu menggunakan Enum
     * @param menu Enum dari FrontOfficeMenu
     */
    public void expandMainMenu(FrontOfficeMenu menu) {
        By locator = By.xpath(String.format("//a[.//span[normalize-space()='%s']]", menu.getLabel()));
        click(locator);
    }

    /**
     * Method dinamis untuk melakukan klik pada Sub-Menu menggunakan Enum
     * @param subMenu Enum dari FrontOfficeMenu (sub-menu)
     */
    public void clickSubMenu(FrontOfficeMenu subMenu) {
        By locator = By.xpath(String.format("//div[span[normalize-space(.)='%s']]", subMenu.getLabel()));
        click(locator);
    }
}
