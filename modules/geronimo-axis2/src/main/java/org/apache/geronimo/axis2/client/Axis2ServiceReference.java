package org.apache.geronimo.axis2.client;

import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.javaee.HandlerChainsType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.axis2.Axis2HandlerResolver;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.jaxws.HandlerChainsUtils;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.client.JAXWSServiceReference;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;
import java.net.URI;
import java.util.Map;

public class Axis2ServiceReference extends JAXWSServiceReference {

    private static final Log log = LogFactory.getLog(Axis2ServiceReference.class);

    public Axis2ServiceReference(String serviceClassName,
                                 String referenceClassName,
                                 URI wsdlURI,
                                 QName serviceQName,
                                 AbstractName name,
                                 String handlerChainsXML,
                                 Map<Object, EndpointInfo> seiInfoMap) {
        super(handlerChainsXML, seiInfoMap, name, serviceQName, wsdlURI, referenceClassName, serviceClassName);
        System.setProperty("javax.xml.ws.spi.Provider", "org.apache.axis2.jaxws.spi.Provider");
    }

    protected HandlerChainsType getHandlerChains() {
        try {
            return HandlerChainsUtils.toHandlerChains(this.handlerChainsXML, HandlerChainsType.class);
        } catch (JAXBException e) {
            // this should not happen
            log.warn("Failed to deserialize handler chains", e);
            return null;
        }
    }

    protected HandlerResolver getHandlerResolver(Class serviceClass) {
        JAXWSAnnotationProcessor annotationProcessor =
                new JAXWSAnnotationProcessor(new JNDIResolver(), new WebServiceContextImpl());
        Axis2HandlerResolver handlerResolver =
                new Axis2HandlerResolver(classLoader, serviceClass, getHandlerChains(), annotationProcessor);
        return handlerResolver;
    }
}
