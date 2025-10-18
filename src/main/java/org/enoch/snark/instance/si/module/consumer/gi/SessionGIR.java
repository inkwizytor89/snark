package org.enoch.snark.instance.si.module.consumer.gi;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.instance.si.module.consumer.Credentials;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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

    SessionGIR(GI gi) {
        super(gi);
    }

    public void manageDriver() {
        wd().manage().window().maximize();
        wd().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
//        wd().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
//        wd().manage().timeouts().scriptTimeout(Duration.ofSeconds(2));
    }

    public void applyCookies(String lobbyToken) {

        // lobby session
        addCookie(GF_TOKEN_PRODUCTION, lobbyToken);
        // cookie consent
        addCookie(GF_COOKIE_CONSENT, GF_COOKIE_CONSENT_YES);
        wd().get(LOBBY_WITH_ACCOUNTS);
    }

    private void addCookie(String key, String value) {
        if(value != null)
            wd().manage().addCookie(new Cookie(key, value,".gameforge.com", "/", null));
        SleepUtil.pause();
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

                gi.findElement(TAG_INPUT, ID_ATTRIBUTE, LOGIN_INPUT).sendKeys(credentials.login());
                gi.findElement(TAG_BUTTON, ID_ATTRIBUTE, LOGIN_BUTTON).click();
                SleepUtil.sleep();

                gi.findElement(TAG_INPUT, ID_ATTRIBUTE, PASSWORD_INPUT).sendKeys(credentials.password());
                gi.findElement(TAG_BUTTON, ID_ATTRIBUTE, PASSWORD_BUTTON).click();
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
        for (int i = 0; i < 3; i++) {
            try {
                SleepUtil.secondsToSleep(3L);

                String serverName = gi.map().getConfig(ThreadMap.SERVER);
                wd().findElements(By.xpath("//div[@role='row']")).stream()
                        .filter(webElement -> webElement.getText().contains(serverName))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Could not find server " + serverName))
                        .findElement(By.tagName("button")).click();
                SleepUtil.sleep(TimeUnit.SECONDS, 4);

                ArrayList<String> tabs = new ArrayList<>(wd().getWindowHandles());
                wd().switchTo().window(tabs.size() == 1 ? tabs.get(0) : tabs.get(1));
                return true;
            } catch (GIException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            SleepUtil.sleep();
        }
        return false;
    }
}
