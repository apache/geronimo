package org.apache.geronimo.axis2.builder;

import org.apache.geronimo.jaxws.builder.JAXWSServiceRefBuilder;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.javaee.PortComponentRefType;
import org.apache.geronimo.xbeans.javaee.ServiceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.naming.deployment.ServiceRefBuilder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Map;

public class Axis2ServiceRefBuilder extends JAXWSServiceRefBuilder {

    private final Axis2Builder axis2Builder;

    public Axis2ServiceRefBuilder(Environment defaultEnvironment,
                                String[] eeNamespaces,
                                Axis2Builder axis2Builder) {
        super(defaultEnvironment, eeNamespaces);
        this.axis2Builder = axis2Builder;
    }

    public Object createService(ServiceRefType serviceRef, GerServiceRefType gerServiceRef,
                                Module module, ClassLoader cl, Class serviceInterfaceClass,
                                QName serviceQName, URI wsdlURI, Class serviceReferenceType,
                                Map<Class, PortComponentRefType> portComponentRefMap) throws DeploymentException {
        return this.axis2Builder.createService(serviceInterfaceClass, serviceReferenceType, wsdlURI,
                                             serviceQName, portComponentRefMap, serviceRef.getHandlerChains(),
                                             gerServiceRef, module, cl);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(
                Axis2ServiceRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ServiceRefBuilder.class);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true,
                true);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addReference("Axis2Builder", Axis2Builder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[] { "defaultEnvironment",
                "eeNamespaces", "Axis2Builder" });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return Axis2ServiceRefBuilder.GBEAN_INFO;
    }
}
