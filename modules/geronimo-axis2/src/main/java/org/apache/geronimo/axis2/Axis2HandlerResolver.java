package org.apache.geronimo.axis2;

import org.apache.axis2.jaxws.javaee.HandlerChainType;
import org.apache.axis2.jaxws.javaee.HandlerChainsType;
import org.apache.geronimo.jaxws.annotations.AnnotationException;
import org.apache.geronimo.jaxws.annotations.AnnotationProcessor;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import java.util.ArrayList;
import java.util.List;

public class Axis2HandlerResolver implements HandlerResolver {

    private HandlerChainsType handlerChains;

    private ClassLoader classLoader;

    private Class serviceClass;

    private AnnotationProcessor annotationProcessor;

    public Axis2HandlerResolver(ClassLoader classLoader,
                                Class serviceClass,
                                HandlerChainsType handlerChains,
                                AnnotationProcessor annotationProcessor) {
        this.classLoader = classLoader;
        this.serviceClass = serviceClass;
        this.handlerChains = handlerChains;
        this.annotationProcessor = annotationProcessor;
    }

    public List<Handler> getHandlerChain(javax.xml.ws.handler.PortInfo portInfo) {

        GeronimoHandlerChainBuilder builder =
                new GeronimoHandlerChainBuilder(this.classLoader, portInfo);

        List<Handler> handlers = null;
        if (this.handlerChains == null) {
            handlers = builder.buildHandlerChainFromClass(this.serviceClass);
        } else {
            handlers = new ArrayList<Handler>();
            for (HandlerChainType handlerChain : this.handlerChains.getHandlerChain()) {
                handlers.addAll(builder.buildHandlerChainFromConfiguration(handlerChain));
            }
            handlers = builder.sortHandlers(handlers);
        }

        if (this.annotationProcessor != null) {
            try {
                for (Handler handler : handlers) {
                    this.annotationProcessor.processAnnotations(handler);
                    this.annotationProcessor.invokePostConstruct(handler);
                }
            } catch (AnnotationException e) {
                throw new WebServiceException("Handler annotation failed", e);
            }
        }

        return handlers;
    }

}
