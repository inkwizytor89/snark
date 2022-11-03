package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.exception.GIException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

abstract class GraphicalInterfaceReader {

    protected final ChromeDriver wd;

    GraphicalInterfaceReader() {
        wd =(ChromeDriver) GI.getInstance().webDriver;
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
