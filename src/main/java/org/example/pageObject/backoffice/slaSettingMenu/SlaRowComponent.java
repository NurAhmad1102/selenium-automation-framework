package org.example.pageObject.backoffice.slaSettingMenu;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SlaRowComponent {
    private WebElement row;

    public SlaRowComponent(WebElement row) {
        this.row = row;
    }

    public List<WebElement> getCells() {
        String XPATH_ROW_TABLE_SLA_SETTING = "./td";
        return row.findElements(By.xpath(XPATH_ROW_TABLE_SLA_SETTING));
    }

}
