package org.apache.geronimo.testsuite.console;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class StartSeleniumDecorator extends TestSetup {
    private Selenium selenium;

    public StartSeleniumDecorator(Test decorated) {
        super(decorated);
    }
    
    public String getName() {
        return getClass().getName();
    }
    
    protected void setUp(String url) throws Exception {
        if (url == null) {
            url = "http://localhost:" + SeleniumServer.DEFAULT_PORT;
        }
        selenium = new DefaultSelenium("localhost",
                SeleniumServer.DEFAULT_PORT, "*firefox", url);
        selenium.start();
    }
    
    /**
     * open '/' and login to the console, leaving the browser open
     * @throws Exception
     */
    void login() throws Exception {
        // after start login, then close the window
        selenium.open("/");
        assertEquals("Apache Geronimo", selenium.getTitle());
        selenium.click("link=Console");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console Login", selenium.getTitle());
        selenium.type("j_username", "system");
        selenium.type("j_password", "manager");
        selenium.click("submit");
        selenium.waitForPageToLoad("30000");
    }

    /**
     * assume the browser is open, click on logout then close the browser
     * @throws Exception
     */
    void logout() throws Exception {
        // after start login, then close the window
        assertEquals("Geronimo Console", selenium.getTitle());
        selenium.click("//a[contains(@href, '/console/logout.jsp')]");
        selenium.waitForPageToLoad("30000");
        assertEquals("Geronimo Console Login", selenium.getTitle());
        selenium.close();
    }

    protected void setUp() throws Exception {
        this.setUp("http://localhost:8080/");
    }

    protected void tearDown() throws Exception {
        selenium.stop();
    }

    public Selenium getSelenium() {
        return selenium;
    }
}
