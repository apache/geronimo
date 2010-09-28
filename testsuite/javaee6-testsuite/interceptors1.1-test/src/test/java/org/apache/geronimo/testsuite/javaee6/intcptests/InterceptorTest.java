/**
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.apache.geronimo.testsuite.javaee6.intcptests;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.Assert;
import org.testng.annotations.*;

public class InterceptorTest extends SeleniumTestSupport {

    @Test
    public void testInterceptorA() throws Exception {
        selenium.open("/interceptor/");
        selenium.type("NumberValue", "1.1");
        selenium.click("operation");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("Valid", selenium.getText("//*[@id=\"intcpt1\"]"));
        Assert.assertEquals("Invalid", selenium.getText("//*[@id=\"intcpt2\"]"));
        String sysmi1 = selenium.getText("//*[@id=\"sys1\"]");
        long s1 = Long.valueOf(sysmi1);
        String sysmi2 = selenium.getText("//*[@id=\"sys2\"]");
        long s2 = Long.valueOf(sysmi2);
        boolean real = (s2 - s1 > 0) ? true : false;
        Assert.assertEquals(true, real);

    }

    @Test
    public void testInterceptorB() throws Exception {
        selenium.open("/interceptor/");
        selenium.type("NumberValue", "-0.9");
        selenium.click("operation");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("Invalid", selenium.getText("//*[@id=\"intcpt1\"]"));
        Assert.assertEquals("Valid", selenium.getText("//*[@id=\"intcpt2\"]"));
        String sysmi1 = selenium.getText("//*[@id=\"sys1\"]");
        long s1 = Long.valueOf(sysmi1);
        String sysmi2 = selenium.getText("//*[@id=\"sys2\"]");
        long s2 = Long.valueOf(sysmi2);
        boolean real = (s2 - s1 > 0) ? true : false;
        Assert.assertEquals(true, real);

    }

    @Test
    public void testTimeoutInterceptor() throws Exception {
        selenium.open("/interceptor/");
        selenium.click("checkAroundTimeout");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("@AroundTimeout invoked!", selenium.getText("//*[@id=\"outputRes\"]"));
    }

    @Test
    public void testInterceptorBinding() throws Exception {
        selenium.open("/interceptor/");
        selenium.click("checkIntcptBind");
        selenium.waitForPageToLoad("30000");
        Assert.assertEquals("invoke @Interceptor, @InterceptorBinding!", selenium.getText("//*[@id=\"intcptBind\"]"));
    }
}
