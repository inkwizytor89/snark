package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ErrorDAO;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.gi.macro.GIUrl;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.macro.UrlComponent.OVERVIEW;
import static org.enoch.snark.gi.text.HtmlElements.TAG_BUTTON;
import static org.enoch.snark.gi.text.HtmlElements.TAG_INPUT;
import static org.enoch.snark.instance.config.Config.*;

public class SessionGIR extends GraphicalInterfaceReader {

    public static final String SIGN_IN_PAGE = "https://gameforge.com/pl-PL/sign-in";
    public static final String LOBBY_URL = "https://lobby.ogame.gameforge.com/pl_PL/";
    public static final String LOBBY_WITH_ACCOUNTS = LOBBY_URL+"accounts";

    public static final String LOGIN_INPUT = "QA_SignIn_Email_Input";
    public static final String LOGIN_BUTTON = "QA_SignIn_Next_Button";
    public static final String PASSWORD_INPUT = "QA_SignIn_Password_Input";
    public static final String PASSWORD_BUTTON = "QA_SignIn_SignIn_Button";

    public void manageDriver() {
        wd.manage().window().maximize();
        wd.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
//        wd.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
//        wd.manage().timeouts().scriptTimeout(Duration.ofSeconds(2));
    }

    public Set<Cookie> loadCookies() {
        openServer();
        return wd.manage().getCookies();
    }

    public void applyCookies(Set<Cookie> cookies) {
        wd.get(SIGN_IN_PAGE);
        cookies.forEach(cookie -> wd.manage().addCookie(cookie));
        GIUrl.openComponent(OVERVIEW, null);
    }

    public boolean isCurrentUrlBackToLobby() {
        return GI.getInstance().getWebDriver().getCurrentUrl().contains(LOBBY_URL);
    }

    public void signIn() {
        while(true) try {
            GI gi = GI.getInstance();
            wd.get(SIGN_IN_PAGE);
            Cookie cookie = new Cookie("gf-cookie-consent-4449562312", "|7|1",".gameforge.com",
                    "/", null);
            wd.manage().addCookie(cookie);

            gi.findElement(TAG_INPUT, ID_ATTRIBUTE, LOGIN_INPUT).sendKeys(Instance.getMainConfigMap().getConfig(LOGIN));
            gi.findElement(TAG_BUTTON, ID_ATTRIBUTE, LOGIN_BUTTON).click();
            SleepUtil.sleep();

            gi.findElement(TAG_INPUT, ID_ATTRIBUTE, PASSWORD_INPUT).sendKeys(Instance.getMainConfigMap().getConfig(PASSWORD));
            gi.findElement(TAG_BUTTON, ID_ATTRIBUTE, PASSWORD_BUTTON).click();
            SleepUtil.secondsToSleep(6L);
            break;
        } catch (GIException e) {
            System.err.println(e.getMessage());
            ErrorDAO.getInstance().save(e);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }

    public void openServer() {
        while(true) try {
            ArrayList<String> tabs = new ArrayList<>(wd.getWindowHandles());
            wd.switchTo().window(tabs.get(0));

            wd.get(LOBBY_WITH_ACCOUNTS);
            if(wd.getCurrentUrl().equals(LOBBY_URL)) {
                signIn();
            }
            SleepUtil.secondsToSleep(3L);

            String serverName = Instance.getMainConfigMap().getConfig(SERVER);
            wd.findElements(By.xpath("//div[@role='row']")).stream()
                    .filter(webElement -> webElement.getText().contains(serverName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not find server " + serverName))
                    .findElement(By.tagName("button")).click();
            SleepUtil.sleep(TimeUnit.SECONDS,4);

            tabs = new ArrayList<>(wd.getWindowHandles());
            wd.switchTo().window(tabs.size() == 1 ? tabs.get(0) : tabs.get(1));
            break;
        } catch (GIException e) {
            System.err.println(e.getMessage());
            ErrorDAO.getInstance().save(e);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }
}
