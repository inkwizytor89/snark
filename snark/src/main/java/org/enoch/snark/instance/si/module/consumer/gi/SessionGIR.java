package org.enoch.snark.instance.si.module.consumer.gi;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.instance.si.module.consumer.Credentials;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.text.HtmlElements.TAG_BUTTON;
import static org.enoch.snark.instance.si.module.consumer.gi.text.HtmlElements.TAG_INPUT;

public class SessionGIR extends GraphicalInterfaceReader {

    public static final String SIGN_IN_PAGE = "https://gameforge.com/pl-PL/sign-in";
    public static final String LOBBY_URL = "https://lobby.ogame.gameforge.com/pl_PL/";
    public static final String LOBBY_WITH_ACCOUNTS = LOBBY_URL+"accounts";

    public static final String LOGIN_INPUT = "QA_SignIn_Email_Input";
    public static final String LOGIN_BUTTON = "QA_SignIn_Next_Button";
    public static final String PASSWORD_INPUT = "QA_SignIn_Password_Input";
    public static final String PASSWORD_BUTTON = "QA_SignIn_SignIn_Button";
    public static final String GF_TOKEN_PRODUCTION = "gf-token-production";
    public static final String GF_COOKIE_CONSENT = "gf-cookie-consent-4449562312";
    public static final String GF_COOKIE_CONSENT_YES = "|7|1";

    SessionGIR(Wd wd) {
        super(wd);
    }

    public void applyCookies(String lobbyToken) {

        // lobby session
        wd.addCookie(GF_TOKEN_PRODUCTION, lobbyToken);
        // cookie consent
        wd.addCookie(GF_COOKIE_CONSENT, GF_COOKIE_CONSENT_YES);
        wd().get(LOBBY_WITH_ACCOUNTS);
    }

    public boolean isCurrentUrlBackToLobby() {
        SleepUtil.sleep();
        return wd().getCurrentUrl().contains("lobby") ;
    }

    public boolean isCurrentUrlEmpty() {
        SleepUtil.sleep();
        return wd().getCurrentUrl() == null || wd().getCurrentUrl().isEmpty() ;
    }

    public boolean isCurrentUrlLobbyAccount() {
        SleepUtil.sleep();
        return wd().getCurrentUrl().contains(LOBBY_WITH_ACCOUNTS);
    }

    public String signInWithRetry(Credentials credentials) {
        for (int i = 0; i < 3; i++) {
            try {
                wd().get(SIGN_IN_PAGE);

                wd.findElement(TAG_INPUT, ID_ATTRIBUTE, LOGIN_INPUT).sendKeys(credentials.login());
                wd.findElement(TAG_BUTTON, ID_ATTRIBUTE, LOGIN_BUTTON).click();
                SleepUtil.sleep();

                wd.findElement(TAG_INPUT, ID_ATTRIBUTE, PASSWORD_INPUT).sendKeys(credentials.password());
                wd.findElement(TAG_BUTTON, ID_ATTRIBUTE, PASSWORD_BUTTON).click();
                SleepUtil.secondsToSleep(20L);

                wd().get(LOBBY_WITH_ACCOUNTS);
                if (isCurrentUrlLobbyAccount()) {
                    return returnLobbyCookie();
                }
            } catch (Throwable e) {
                System.err.println(e);
            }
            SleepUtil.sleep();
        }
        return StringUtils.EMPTY;
    }

    public String returnLobbyCookie() {
        return wd().manage().getCookies().stream()
                .filter(cookie -> cookie.getName().equals(GF_TOKEN_PRODUCTION))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Missing cookie " + GF_TOKEN_PRODUCTION + " to write in cache"));
    }

    public boolean openServer() {
        try {
            String serverName = wd.getServerName();
            List<WebElement> webElements = longFetchAll(By.xpath("//div[@role='row']"));
            System.err.println("serwers count "+webElements.size());
            webElements.stream()
                    .filter(webElement -> webElement.getText().contains(serverName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not find server " + serverName))
                    .findElement(By.tagName("button")).click();

            getLongWait().until(driver -> driver.getWindowHandles().size() >= 2);

            ArrayList<String> tabs = new ArrayList<>(wd().getWindowHandles());
            wd().switchTo().window(tabs.size() == 1 ? tabs.get(0) : tabs.get(1));

            getLongWait().until(driver -> !driver.getCurrentUrl().contains("lobby"));
            return true;
        } catch (GIException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}
