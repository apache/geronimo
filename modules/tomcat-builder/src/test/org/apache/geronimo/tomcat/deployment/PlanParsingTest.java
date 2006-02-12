package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.net.URL;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.tomcat.TomcatWebAppType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;

/**
 */
public class PlanParsingTest extends TestCase {
    private ClassLoader classLoader = this.getClass().getClassLoader();
    ObjectName tomcatContainerObjectName = JMXUtil.getObjectName("test:type=TomcatContainer");
    Kernel kernel = null;
    private TomcatModuleBuilder builder;
    private File basedir = new File(System.getProperty("basedir", "."));

    protected void setUp() throws Exception {
        builder = new TomcatModuleBuilder(null, false, tomcatContainerObjectName, null, null);
    }

    public void testResourceRef() throws Exception {
        URL resourceURL = classLoader.getResource("plans/plan1.xml");
        File resourcePlan = new File(resourceURL.getFile());
        assertTrue(resourcePlan.exists());
        TomcatWebAppType tomcatWebApp = builder.getTomcatWebApp(resourcePlan, null, true, null, null);
        assertEquals(1, tomcatWebApp.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        GerWebAppDocument tomcatWebAppDoc = GerWebAppDocument.Factory.newInstance();
        GerWebAppType tomcatWebAppType = tomcatWebAppDoc.addNewWebApp();
        EnvironmentType environmentType = tomcatWebAppType.addNewEnvironment();
        ArtifactType artifactType = environmentType.addNewConfigId();
        artifactType.setArtifactId("foo");

        environmentType.addNewClassloader();
        tomcatWebAppType.setContextPriorityClassloader(false);
        GerResourceRefType ref = tomcatWebAppType.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(tomcatWebAppType);
//        System.out.println(tomcatWebAppType.toString());
    }

}
