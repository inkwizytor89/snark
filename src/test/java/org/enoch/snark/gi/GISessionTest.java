package org.enoch.snark.gi;

import org.enoch.snark.instance.Instance;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GISessionTest {

    private GISession session;
    private Instance instance;

    @Before
    public void setUp() {
//        instance = AbstractSeleniumTest.creatTestInstance();
    }

    @Test
    public void login_when_openSession() {
        session = instance.session;

        session.open();

        assertEquals(true, session.isLoggedIn());
        session.close();
    }

    @Test
    public void logout_when_closeSession() {
        session = instance.session;
        session.open();

        session.close();
        assertEquals(false, session.isLoggedIn());
    }
}