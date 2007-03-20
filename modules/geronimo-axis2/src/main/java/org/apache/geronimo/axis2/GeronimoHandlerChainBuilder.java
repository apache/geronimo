package org.apache.geronimo.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.xbeans.javaee.HandlerChainType;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;
import java.util.Collections;
import java.util.List;

public class GeronimoHandlerChainBuilder extends AnnotationHandlerChainBuilder {
    private static final Log log = LogFactory.getLog(GeronimoHandlerChainBuilder.class);

    private ClassLoader classLoader = null;
    private javax.xml.ws.handler.PortInfo portInfo;

    public GeronimoHandlerChainBuilder(ClassLoader classloader,
                                       PortInfo portInfo) {
        this.classLoader = classloader;
        this.portInfo = portInfo;
    }

    public ClassLoader getHandlerClassLoader() {
        return this.classLoader;
    }

    protected List<Handler> buildHandlerChain(HandlerChainType hc,
                                              ClassLoader classLoader) {
        if (matchServiceName(portInfo, hc.getServiceNamePattern())
                && matchPortName(portInfo, hc.getPortNamePattern())
                && matchBinding(portInfo, hc.getProtocolBindings())) {
            return super.buildHandlerChain(hc, classLoader);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private boolean matchServiceName(PortInfo info, String namePattern) {
        return match((info == null ? null : info.getServiceName()), namePattern);
    }

    private boolean matchPortName(PortInfo info, String namePattern) {
        return match((info == null ? null : info.getPortName()), namePattern);
    }

    private boolean matchBinding(PortInfo info, List bindings) {
        return match((info == null ? null : info.getBindingID()), bindings);
    }

    private boolean match(String binding, List bindings) {
        if (binding == null) {
            return (bindings == null || bindings.isEmpty());
        } else {
            return (bindings == null || bindings.isEmpty()) ? true : bindings.contains(binding);
        }
    }

    public List<Handler> buildHandlerChainFromConfiguration(HandlerChainType hc) {
        if (null == hc) {
            return null;
        }
        return sortHandlers(buildHandlerChain(hc, getHandlerClassLoader()));
    }

    /*
     * Performs basic localName matching, namespaces are not checked!
     */
    private boolean match(QName name, String namePattern) {
        if (name == null) {
            return (namePattern == null || namePattern.equals("*"));
        } else {
            if (namePattern == null) {
                return true;
            } else {
                String localNamePattern;

                // get the local name from pattern
                int pos = namePattern.indexOf(':');
                localNamePattern = (pos == -1) ? namePattern : namePattern
                        .substring(pos + 1);
                localNamePattern = localNamePattern.trim();

                if (localNamePattern.equals("*")) {
                    // matches anything
                    return true;
                } else if (localNamePattern.endsWith("*")) {
                    // match start
                    localNamePattern = localNamePattern.substring(0,
                            localNamePattern.length() - 1);
                    return name.getLocalPart().startsWith(localNamePattern);
                } else {
                    // match exact
                    return name.getLocalPart().equals(localNamePattern);
                }
            }
        }
    }

}
