package org.enoch.snark.gi;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

abstract class GraphicalInterfaceReader {

    public static final String A_TAG = "a";
    public static final String DIV_TAG = "div";
    public static final String TR_TAG = "tr";
    public static final String SPAN_TAG = "span";

    public static final String HREF_ATTRIBUTE = "href";
    public static final String TITLE_ATTRIBUTE = "title";
    public static final String ID_ATTRIBUTE = "id";
    public static final String CLASS_ATTRIBUTE = "class";

    protected final ChromeDriver wd;

    GraphicalInterfaceReader() {
        wd =(ChromeDriver) GI.getInstance().webDriver;
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
        return Long.parseLong(string.replaceAll("\\.",""));
    }
}
