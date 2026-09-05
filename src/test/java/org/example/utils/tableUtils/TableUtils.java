package org.example.utils.tableUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.drivers.DriverManager;
import org.example.model.SlaDetailModel;
import org.example.model.SlaSettingModel;
import org.example.pageObject.backoffice.slaSettingMenu.SlaSetting.SlaSettingPage;
import org.example.pageObject.backoffice.slaSettingMenu.DashboardSlaSettingPage;
import org.example.pageObject.backoffice.slaSettingMenu.SlaRowComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class TableUtils {

    private static final Logger logger = LogManager.getLogger(TableUtils.class);
    private final SlaSettingPage slaSettingPage;
    private final DashboardSlaSettingPage dashboardSlaSettingPage;

    public TableUtils(SlaSettingPage slaSettingPage, DashboardSlaSettingPage dashboardSlaSettingPage) {
        this.slaSettingPage = slaSettingPage;
        this.dashboardSlaSettingPage = dashboardSlaSettingPage;
    }

    /**
     * MENU: SLA DETAIL TABLE
     * FUNGSI: validasi sla detail yg di add, apa sudah muncul di table sla detail
     * @param data List berisi data yg sla setting yg di add
     * @return kalo ada ketemu return isFound true
     */
    public boolean validateRowSlaDetails(List<String> data){
        List<WebElement> rows = slaSettingPage.getTableSlaSetting();
        boolean isFound = false;

        if(!data.isEmpty()){
            String expectedType = data.get(0);
            String expectedCondition = data.get(1);
            List<String> expectedConditionDetail = data.subList(2, data.size()-1);
            String expectedSlaDay = data.get(data.size()-1);

            for (WebElement row:rows){
                SlaRowComponent eleRow = new SlaRowComponent(row);
                List<WebElement> cells = eleRow.getCells();

                if(cells.size() >= 5){
                    String actualType = cells.get(2).getText(); // 2 si Sla Setting Name, skip si number
                    String actualCondition = cells.get(3).getText();
                    String conditions = cells.get(4).getText();
                    List<String> actualConditionDetail = Arrays.stream(conditions.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                    String[] day = cells.get(5).getText().split(" ");
                    String actualSlaDay = day[0];
                    logger.debug(
                            "Validate SLA detail row. type={}, condition={}, conditionDetail={}, slaDay={}",
                            actualType, actualCondition, actualConditionDetail, actualSlaDay);
                    if (actualType.equals(expectedType) &&
                            actualCondition.equals(expectedCondition) &&
                            actualConditionDetail.equals(expectedConditionDetail) &&
                            actualSlaDay.equals(expectedSlaDay)){
                        isFound = true;
                        break;
                    }
                }

            }
        }
        return isFound;
    }


    /**
     * MENU: SLA DETAIL TABLE
     * FUNGSI: get all ada dari Table Sla Detail
     * @return SlaDetailModel
     */
    public List<SlaDetailModel> getAllDataFromSlaDetailTable(){
        List<WebElement> rows = slaSettingPage.getTableSlaSetting();
        List<SlaDetailModel> allData = new ArrayList<>();

        WebElement checkbox = null;
        String number= " ";
        String type = " ";
        String condition = " ";
        List<String> conditionDetail = new ArrayList<>();
        String slaDay = " ";
        WebElement editButton = null;

        if(!rows.isEmpty()){
            for (WebElement row:rows){
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() == 7 ){
                    logger.debug("Processing SLA detail table row with {} cells", cells.size());

                    for(int i = 0; i < cells.size(); i++){
                        switch (i){
                            case 0:
                                checkbox = cells.get(0);
                                break;
                            case 1:
                                number = cells.get(1).getText();
                                break;
                            case 2:
                                type = cells.get(2).getText();
                                break;
                            case 3:
                                condition = cells.get(3).getText();
                                break;
                            case 4:
                                String fullText = cells.get(4).getText();
                                conditionDetail = Arrays.stream(
                                        fullText.split(","))
                                        .map(String::trim)
                                        .collect(Collectors.toList());
                                break;
                            case 5:
                                String[] day = cells.get(5).getText().split(" ");
                                slaDay = day[0];
                                break;
                            case 6:
                                editButton = cells.get(6).findElement(By.tagName("a"));
                                break;
                        }
                    }
                    SlaDetailModel model = new SlaDetailModel(checkbox, number, type, condition, conditionDetail, slaDay, editButton);
                    allData.add(model);
                }
            }

        }
        return allData;
    }


    /**
     * MENU: DASHBOARD SLA [KLIK DETAIL SLA]
     * FUNGSI: get all data from td sla setting detail [status active & inactive], tehn cekc if any empty
     * @return true kalo ada isi nya
     */
    public boolean isTableContentValid(){
        List<WebElement> rows = slaSettingPage.getTableSlaSetting();

        if(rows.isEmpty()){
            return false;
        }
        for (WebElement row: rows){
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (WebElement cell: cells){
                String cellText = cell.getText().trim();
                if(cellText.isEmpty()||cellText.equalsIgnoreCase("No Data")){
                    logger.warn("Invalid SLA detail table content found in row: {}", row.getText());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * DI DASHBOARD SLA SETTING
     * FUNGSI: buat get by status dan ambil index yg pertama
     *
     * @param status
     * @return kalo nemu retrun sla period name, kalo empty return null
     */
    public WebElement getSlaPeriodNameFromTable(String status){
            List<WebElement> elements = dashboardSlaSettingPage.getSlaByStatus(status.toUpperCase());
            logger.info("Found {} SLA setting rows with status: {}", elements.size(), status);

            if(!elements.isEmpty()){
                WebElement firstStatusIndex = elements.get(0);
                WebElement row = firstStatusIndex.findElement(By.xpath("./ancestor::tr"));
                List<WebElement> slaName = row.findElements(By.tagName("td"));
                return slaName.get(1);
            }
        return null;
    }


    /**
     * MENU: DASHBOARD SLA SETTING
     * FUNGSI: buat get semua data td di dalam satu tr based on td = slaPeriodName
     *
     * @param slaPeriodName ambil sla period yg tersedia di table sla setting
     * @return object SlaSetting (effDate, createdBy, status, buttonElements)
     */
    public SlaSettingModel getSlaSettingFromTable(String slaPeriodName){
        List<WebElement> elements = dashboardSlaSettingPage.getSlaPeriod(slaPeriodName);

        String effDate = " ";
        String createdBy = " ";
        String status = " ";
        Map<String, WebElement> buttonElements = new HashMap<>();

        for(int i = 0; i < elements.size(); i++){
            WebElement ele = elements.get(i);
            String text = ele.getText().trim();

            if(!text.isEmpty()){
                switch (i){
                    case 0:
                        effDate = text;
                        break;
                    case 1:
                        createdBy = text;
                        break;
                    case 2:
                        status = text;
                }
            } else if (i == 3){
                    buttonElements = getAvailableButtonByStatus(ele, status); //index ke 3 td isi button
            }
        }
        return new SlaSettingModel(effDate, createdBy, status, buttonElements);
    }

//    DI dashboard Sla Setting
    private Map<String, WebElement> getAvailableButtonByStatus(WebElement ele, String status){

        Map<String, WebElement> buttons = new HashMap<>();
        List<WebElement> icons = ele.findElements(By.xpath(".//span[@role='img']"));

        for(WebElement icon: icons){
            String label = icon.getAttribute("aria-label");
            boolean isDisplayed = icon.isDisplayed();

            if(isStatusActiveInactive(status)){
                if (label.equals("eye") && isDisplayed){ //eye
                    buttons.put(label, icon);
                }
            } else {
                if ((label.equals("edit") || label.equals("delete")) && isDisplayed){
                    buttons.put(label, icon);
                }
            }
        }
        return buttons;
    }

    private boolean isStatusActiveInactive(String status){
        status = status.toUpperCase();
        return status.equals("ACTIVE") || status.equals("INACTIVE");
    }


    public boolean searchCellByText(String keyword){
        String xpath = String.format("//td[contains(normalize-space(text()), '%s')]", keyword);
        try {
            return !DriverManager.getDriver().findElements(By.xpath(xpath)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

}
