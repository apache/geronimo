/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.plugin.j2ee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.deployment.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/25 01:08:25 $
 */
public class ENCHelper {
    public static final String[] ENC_XPATHS = {
        "ejb-ref/ejb-ref-name"
    };

    private final DDBean ddBean;
    private final Map ejbRefs = new HashMap();
    private final Map ejbLocalRefs = new HashMap();
    private final Map serviceRefs = new HashMap();
    private final Map resourceRefs = new HashMap();

    public ENCHelper(DDBean ddBean) {
        this.ddBean = ddBean;
    }
    private static final Log log = LogFactory.getLog(ENCHelper.class);
    public DConfigBean getDConfigBean(DDBean ddBean) throws ConfigurationException {
        String xpath = ddBean.getXpath();
        String name = ddBean.getText();
        log.info("Gettig config bean for " + name + " at " + xpath);
        if (xpath.endsWith("ejb-ref/ejb-ref-name")) {
            DConfigBean dcBean = (DConfigBean) ejbRefs.get(name);
            if (dcBean == null) {
                dcBean = new URIRefConfigBean(ddBean);
                ejbRefs.put(name, dcBean);
            }
            return dcBean;
        } else if (xpath.endsWith("ejb-local-ref/ejb-ref-name")) {
            DConfigBean dcBean = (DConfigBean) ejbLocalRefs.get(name);
            if (dcBean == null) {
                dcBean = new URIRefConfigBean(ddBean);
                ejbLocalRefs.put(name, dcBean);
            }
            return dcBean;
        } else if (xpath.endsWith("service-ref/service-ref-name")) {
            DConfigBean dcBean = (DConfigBean) serviceRefs.get(name);
            if (dcBean == null) {
                dcBean = new URIRefConfigBean(ddBean);
                serviceRefs.put(name, dcBean);
            }
            return dcBean;
        } else if (xpath.endsWith("resource-ref/res-ref-name")) {
            DConfigBean dcBean = (DConfigBean) resourceRefs.get(name);
            if (dcBean == null) {
                dcBean = new URIRefConfigBean(ddBean);
                resourceRefs.put(name, dcBean);
            }
            return dcBean;
        } else {
            throw new ConfigurationException("Unrecognized XPath: " + ddBean.getXpath());
        }
    }

    public void removeDConfigBean(DConfigBean dcBean) throws BeanNotFoundException {
        DDBean ddBean = dcBean.getDDBean();
        String xpath = ddBean.getXpath();
        String name = ddBean.getText();

        if (xpath.endsWith("ejb-ref/ejb-ref-name")) {
            if (ejbRefs.remove(name) == null) {
                throw new BeanNotFoundException("No DConfigBean found with name: " + name);
            }
        } else if (xpath.endsWith("ejb-local-ref/ejb-ref-name")) {
            if (ejbLocalRefs.remove(name) == null) {
                throw new BeanNotFoundException("No DConfigBean found with name: " + name);
            }
        } else if (xpath.endsWith("service-ref/service-ref-name")) {
            if (serviceRefs.remove(name) == null) {
                throw new BeanNotFoundException("No DConfigBean found with name: " + name);
            }
        } else if (xpath.endsWith("resource-ref/res-ref-name")) {
            if (resourceRefs.remove(name) == null) {
                throw new BeanNotFoundException("No DConfigBean found with name: " + name);
            }
        } else {
            throw new BeanNotFoundException("Unrecognized XPath: " + xpath);
        }
    }

    public void toXML(PrintWriter writer) throws IOException {
        for (Iterator i = ejbRefs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            writer.println("<ejb-ref>");
            writer.print("<ejb-ref-name>");
            writer.print(entry.getKey());
            writer.println("</ejb-ref-name>");
            ((DConfigBeanSupport) entry.getValue()).toXML(writer);
            writer.println("</ejb-ref>");
        }
        for (Iterator i = ejbLocalRefs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            writer.println("<ejb-local-ref>");
            writer.print("<ejb-ref-name>");
            writer.print(entry.getKey());
            writer.println("</ejb-ref-name>");
            ((DConfigBeanSupport) entry.getValue()).toXML(writer);
            writer.println("</ejb-local-ref>");
        }
        for (Iterator i = serviceRefs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            writer.println("<service-ref>");
            writer.print("<service-ref-name>");
            writer.print(entry.getKey());
            writer.println("</service-ref-name>");
            ((DConfigBeanSupport) entry.getValue()).toXML(writer);
            writer.println("</service-ref>");
        }
        for (Iterator i = resourceRefs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            writer.println("<resource-ref>");
            writer.print("<res-ref-name>");
            writer.print(entry.getKey());
            writer.println("</res-ref-name>");
            ((DConfigBeanSupport) entry.getValue()).toXML(writer);
            writer.println("</resource-ref>");
        }
    }

    public void fromXML(Element parent) {
        Map ejbRefDDBeans = mapDDBeans("ejb-ref/ejb-ref-name");
        Map ejbLocalRefDDBeans = mapDDBeans("ejb-local-ref/ejb-ref-name");
        Map serviceRefDDBeans = mapDDBeans("service-ref/service-ref-name");
        Map resourceRefDDBeans = mapDDBeans("resource-ref/res-ref-name");
        ejbRefs.clear();
        ejbLocalRefs.clear();
        serviceRefs.clear();
        resourceRefs.clear();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element == false) {
                continue;
            }
            Element child = (Element) node;
            if ("ejb-ref".equals(child.getNodeName())) {
                String name = XMLUtil.getChildContent(child, "ejb-ref-name", null, null);
                DDBean ejbBean = (DDBean) ejbRefDDBeans.get(name);
                if (ejbBean != null) {
                    ejbRefs.put(name, new URIRefConfigBean(ejbBean));
                }
            } else if ("ejb-local-ref".equals(child.getNodeName())) {
                String name = XMLUtil.getChildContent(child, "ejb-ref-name", null, null);
                DDBean ejbLocalBean = (DDBean) ejbLocalRefDDBeans.get(name);
                if (ejbLocalBean != null) {
                    ejbLocalRefs.put(name, new URIRefConfigBean(ejbLocalBean));
                }
            } else if ("service-ref".equals(child.getNodeName())) {
                String name = XMLUtil.getChildContent(child, "service-ref-name", null, null);
                DDBean serviceBean = (DDBean) serviceRefDDBeans.get(name);
                if (serviceBean != null) {
                    serviceRefs.put(name, new URIRefConfigBean(serviceBean));
                }
            } else if ("resource-ref".equals(child.getNodeName())) {
                String name = XMLUtil.getChildContent(child, "res-ref-name", null, null);
                DDBean resourceBean = (DDBean) resourceRefDDBeans.get(name);
                if (resourceBean != null) {
                    resourceRefs.put(name, new URIRefConfigBean(resourceBean));
                }
            }
        }
    }

    private Map mapDDBeans(String xpath) {
        DDBean[] ddBeans = ddBean.getChildBean(xpath);
        if (ddBeans == null) {
            return Collections.EMPTY_MAP;
        }
        Map map = new HashMap(ddBeans.length);
        for (int i = 0; i < ddBeans.length; i++) {
            DDBean ddBean = ddBeans[i];
            map.put(ddBean.getText(), ddBean);
        }
        return map;
    }
}
