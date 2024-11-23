package org.enoch.snark.gi;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

abstract class GraphicalInterfaceReader {

    public static final String A_TAG = "a";
    public static final String DIV_TAG = "div";
    public static final String TR_TAG = "tr";
    public static final String SPAN_TAG = "span";

    public static final String HREF_ATTRIBUTE = "href";
    public static final String TITLE_ATTRIBUTE = "data-tooltip-title";
    public static final String ID_ATTRIBUTE = "id";
    public static final String CLASS_ATTRIBUTE = "class";

    protected final ChromeDriver wd;
//    protected final WebDriverWait wait;

    GraphicalInterfaceReader() {
        wd =(ChromeDriver) GI.getInstance().getWebDriver();
//        wait = new WebDriverWait(wd, 1);
    }

    protected String getText(ChromeDriver wd) {

        return getText(wd.findElement(By.id("main???")));
    }
    protected String getText(WebElement we) {
        return null;
    }



    protected String getValue(ChromeDriver wd) {
        return getValue(wd.findElement(By.id("main???")));
    }
    protected String getValue(WebElement we) {
        return null;
    }

    /**
     *
     * @param string 6.283.777
     * @return 6283777L
     */
    protected Long toLong(String string) {
        if(StringUtils.isEmpty(string)) return 0L;
        return Long.parseLong(string.replaceAll("\\.",""));
    }

    public WebElement getIfPresentById(String id) {
        return getIfPresentById(id, 1L);
    }

    public WebElement getIfPresentById(String id, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(wd, Duration.ofSeconds(timeOutInSeconds));
        WebElement webElement = null;
        try {
            webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(id)));
        } catch (TimeoutException ignored) {}
        return webElement;
    }

    protected Long getLevel(WebElement element, String name) {
        try {
            return Long.parseLong(element.findElement(By.className(name)).findElement(By.className("level")).getText());
        } catch (NumberFormatException e) {
            return Long.parseLong(element.findElement(By.className(name)).findElement(By.className("level")).getAttribute("data-value"));
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
