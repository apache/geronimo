package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.tomcat.TomcatWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.tomcat.TomcatWebAppType;

/**
 */
public class PlanParsingTest extends TestCase {
    ObjectName tomcatContainerObjectName = JMXUtil.getObjectName("test:type=TomcatContainer");
    private TomcatModuleBuilder builder = new TomcatModuleBuilder(null, tomcatContainerObjectName, null, null);
    private File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        TomcatWebAppType jettyWebApp = builder.getTomcatWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, jettyWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        TomcatWebAppDocument tomcatWebAppDoc = TomcatWebAppDocument.Factory.newInstance();
        TomcatWebAppType tomcatWebAppType = tomcatWebAppDoc.addNewWebApp();
        tomcatWebAppType.setConfigId("configId");
        tomcatWebAppType.setParentId("parentId");
        tomcatWebAppType.setContextPriorityClassloader(false);
        GerResourceRefType ref = tomcatWebAppType.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(tomcatWebAppType);
        System.out.println(tomcatWebAppType.toString());
    }

    public void testParseSpecDD() {

    }
}
