package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ErrorDAO;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.text.HtmlElements.TAG_BUTTON;
import static org.enoch.snark.gi.text.HtmlElements.TAG_INPUT;
import static org.enoch.snark.instance.config.Config.*;

public class SessionGIR extends GraphicalInterfaceReader {

    public static final String LOBBY_URL = "https://lobby.ogame.gameforge.com/";
    public static final String LOBBY_WITH_ACCOUNTS = LOBBY_URL+"pl_PL/accounts";

    public static final String LOGIN_INPUT = "QA_SignIn_Email_Input";
    public static final String LOGIN_BUTTON = "QA_SignIn_Next_Button";
    public static final String PASSWORD_INPUT = "QA_SignIn_Password_Input";
    public static final String PASSWORD_BUTTON = "QA_SignIn_SignIn_Button";

    public void signIn() {
        while(true) {
            try {
                GI gi = GI.getInstance();
                wd.get("https://gameforge.com/pl-PL/sign-in");
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
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public void openServer() {
        ArrayList<String> tabs = new ArrayList<>(wd.getWindowHandles());
        wd.switchTo().window(tabs.get(0));

        wd.get(LOBBY_WITH_ACCOUNTS);
        SleepUtil.secondsToSleep(2L);

        String serverName = Instance.getMainConfigMap().getConfig(SERVER);
        wd.findElements(By.xpath("//div[@role='row']")).stream()
                .filter(webElement -> webElement.getText().contains(serverName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find server " + serverName))
                .findElement(By.tagName("button")).click();
        SleepUtil.sleep(TimeUnit.SECONDS,4);

        tabs = new ArrayList<>(wd.getWindowHandles());
        wd.switchTo().window(tabs.size() == 1 ? tabs.get(0) : tabs.get(1));
    }
}
