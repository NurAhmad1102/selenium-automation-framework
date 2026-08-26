package org.example.pageObject.frontoffice.frontOfficeSideMenu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import org.example.enums.frontoffice.FrontOfficeMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class FrontOfficeSideMenu extends BasePage {
    private final Logger logger = LogManager.getLogger(getClass());

    public FrontOfficeSideMenu(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    /**
     * Method dinamis untuk melakukan klik / ekspansi pada Main Menu menggunakan Enum
     * @param menu Enum dari FrontOfficeMenu
     */
    public void expandMainMenu(FrontOfficeMenu menu) {
        By locator = By.xpath(
                String.format(
                        "//nav[contains(@class,'ui-sidebar-menu-list-theme')]" +
                                "//a[contains(@class,'ui-sidebar-item-theme')]" +
                                "[.//span[normalize-space()='%s']]",
                        menu.getLabel()
                ));
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

    public void closeModalIfVisible() {
        // cari button icon x ('lucide-x')
        String closeBtnXPath = "//button[.//svg[contains(@class, 'lucide-x')]]";

        try {
            // cek list tidak kosong dan elemen muncul
            List<WebElement> elements = driver.findElements(By.id(closeBtnXPath));

            if (elements.isEmpty() || !elements.get(0).isDisplayed()) {
                logger.debug("No Pop-up detected. Skip process...");
            } else {
                logger.debug("Pop-up detected. Try to close it...");
                elements.get(0).click();
            }

        } catch (Exception e) {
            logger.debug("Failed: Cannot interact with pop-up. " + e.getMessage());
        }
    }
}
