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
package org.apache.geronimo.naming.deployment;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.xbeans.geronimo.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.GerMessageDestinationRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceRefType;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/18 20:58:43 $
 */
public class ENCHelper {


    public static final String[] ENC_XPATHS = {
        "ejb-ref",
        "ejb-local-ref",
        "message-destination-ref",
        "resource-env-ref",
        "resource-ref",
    };

    private final DDBean ddBean;

    private final XmlEnvRefs envRefs;

    private EJBRefDConfigBean[] ejbRefs;

    private EJBLocalRefDConfigBean[] ejbLocalRefs;
    private MessageDestinationRefDConfigBean[] messageDestinationRefs;
    private ResourceEnvRefDConfigBean[] resourceEnvRefs;
    private ResourceRefDConfigBean[] resourceRefs;

    public ENCHelper(DDBean ddBean, XmlEnvRefs envRefs) {
        this.ddBean = ddBean;
        this.envRefs = envRefs;
        DDBean[] ddEjbRefs = ddBean.getChildBean(ENC_XPATHS[0]);
        if (ddEjbRefs == null) {
            ddEjbRefs = new DDBean[0];
        }
        ejbRefs = new EJBRefDConfigBean[ddEjbRefs.length];
        GerEjbRefType[] xmlEjbRefs = envRefs.getEjbRefs();
        Map ejbRefMap = new HashMap();
        for (int i = 0; i < xmlEjbRefs.length; i++) {
            GerEjbRefType xmlEjbRef = xmlEjbRefs[i];
            ejbRefMap.put(xmlEjbRef.getEjbRefName().getStringValue(), xmlEjbRef.getUri());
            envRefs.removeEjbRef(0);
        }
        for (int i = 0; i < ddEjbRefs.length; i++) {
            DDBean ddEjbRef = ddEjbRefs[i];
            String name = ddEjbRef.getText("ejb-ref-name")[0];
            GerEjbRefType xmlEjbRef = envRefs.addNewEjbRef();
            xmlEjbRef.addNewEjbRefName().setStringValue(name);
            if (ejbRefMap.get(name) != null) {
                xmlEjbRef.setUri((String)ejbRefMap.get(name));
            }
            ejbRefs[i] = new EJBRefDConfigBean(ddEjbRef,  xmlEjbRef);
        }

        DDBean[] ddEjbLocalRefs = ddBean.getChildBean(ENC_XPATHS[1]);
        if (ddEjbLocalRefs == null) {
            ddEjbLocalRefs = new DDBean[0];
        }
        ejbLocalRefs = new EJBLocalRefDConfigBean[ddEjbLocalRefs.length];
        GerEjbLocalRefType[] xmlEjbLocalRefs = envRefs.getEjbLocalRefs();
        Map EjbLocalRefMap = new HashMap();
        for (int i = 0; i < xmlEjbLocalRefs.length; i++) {
            GerEjbLocalRefType xmlEjbLocalRef = xmlEjbLocalRefs[i];
            EjbLocalRefMap.put(xmlEjbLocalRef.getEjbRefName().getStringValue(), xmlEjbLocalRef.getUri());
            envRefs.removeEjbLocalRef(0);
        }
        for (int i = 0; i < ddEjbLocalRefs.length; i++) {
            DDBean ddEjbLocalRef = ddEjbLocalRefs[i];
            String name = ddEjbLocalRef.getText("ejb-ref-name")[0];
            GerEjbLocalRefType xmlEjbLocalRef = envRefs.addNewEjbLocalRef();
            xmlEjbLocalRef.addNewEjbRefName().setStringValue(name);
            if (EjbLocalRefMap.get(name) != null) {
                xmlEjbLocalRef.setUri((String)EjbLocalRefMap.get(name));
            }
            ejbLocalRefs[i] = new EJBLocalRefDConfigBean(ddEjbLocalRef,  xmlEjbLocalRef);
        }

        DDBean[] ddMessageDestinationRefs = ddBean.getChildBean(ENC_XPATHS[2]);
        if (ddMessageDestinationRefs == null) {
            ddMessageDestinationRefs = new DDBean[0];
        }
        messageDestinationRefs = new MessageDestinationRefDConfigBean[ddMessageDestinationRefs.length];
        GerMessageDestinationRefType[] xmlMessageDestinationRefs = envRefs.getMessageDestinationRefs();
        Map MessageDestinationRefMap = new HashMap();
        for (int i = 0; i < xmlMessageDestinationRefs.length; i++) {
            GerMessageDestinationRefType xmlMessageDestinationRef = xmlMessageDestinationRefs[i];
            MessageDestinationRefMap.put(xmlMessageDestinationRef.getMessageDestinationRefName().getStringValue(), xmlMessageDestinationRef.getUri());
            envRefs.removeMessageDestinationRef(0);
        }
        for (int i = 0; i < ddMessageDestinationRefs.length; i++) {
            DDBean ddMessageDestinationRef = ddMessageDestinationRefs[i];
            String name = ddMessageDestinationRef.getText("message-destination-ref-name")[0];
            GerMessageDestinationRefType xmlMessageDestinationRef = envRefs.addNewMessageDestinationRef();
            xmlMessageDestinationRef.addNewMessageDestinationRefName().setStringValue(name);
            if (MessageDestinationRefMap.get(name) != null) {
                xmlMessageDestinationRef.setUri((String)MessageDestinationRefMap.get(name));
            }
            messageDestinationRefs[i] = new MessageDestinationRefDConfigBean(ddMessageDestinationRef,  xmlMessageDestinationRef);
         }

        DDBean[] ddResourceEnvRefs = ddBean.getChildBean(ENC_XPATHS[3]);
        if (ddResourceEnvRefs == null) {
            ddResourceEnvRefs = new DDBean[0];
        }
        resourceEnvRefs = new ResourceEnvRefDConfigBean[ddResourceEnvRefs.length];
        GerResourceEnvRefType[] xmlResourceEnvRefs = envRefs.getResourceEnvRefs();
        Map ResourceEnvRefMap = new HashMap();
        for (int i = 0; i < xmlResourceEnvRefs.length; i++) {
            GerResourceEnvRefType xmlResourceEnvRef = xmlResourceEnvRefs[i];
            ResourceEnvRefMap.put(xmlResourceEnvRef.getResourceEnvRefName().getStringValue(), xmlResourceEnvRef.getUri());
            envRefs.removeResourceEnvRef(0);
        }
        for (int i = 0; i < ddResourceEnvRefs.length; i++) {
            DDBean ddResourceEnvRef = ddResourceEnvRefs[i];
            String name = ddResourceEnvRef.getText("resource-env-ref-name")[0];
            GerResourceEnvRefType xmlResourceEnvRef = envRefs.addNewResourceEnvRef();
            xmlResourceEnvRef.addNewResourceEnvRefName().setStringValue(name);
            if (ResourceEnvRefMap.get(name) != null) {
                xmlResourceEnvRef.setUri((String)ResourceEnvRefMap.get(name));
            }
            resourceEnvRefs[i] = new ResourceEnvRefDConfigBean(ddResourceEnvRef,  xmlResourceEnvRef);
        }

        DDBean[] ddResourceRefs = ddBean.getChildBean(ENC_XPATHS[4]);
        if (ddResourceRefs == null) {
            ddResourceRefs = new DDBean[0];
        }
        resourceRefs = new ResourceRefDConfigBean[ddResourceRefs.length];
        GerResourceRefType[] xmlResourceRefs = envRefs.getResourceRefs();
        Map ResourceRefMap = new HashMap();
        for (int i = 0; i < xmlResourceRefs.length; i++) {
            GerResourceRefType xmlResourceRef = xmlResourceRefs[i];
            ResourceRefMap.put(xmlResourceRef.getResRefName().getStringValue(), xmlResourceRef.getUri());
            envRefs.removeResourceRef(0);
        }
        for (int i = 0; i < ddResourceRefs.length; i++) {
            DDBean ddResourceRef = ddResourceRefs[i];
            String name = ddResourceRef.getText("res-ref-name")[0];
            GerResourceRefType xmlResourceRef = envRefs.addNewResourceRef();
            xmlResourceRef.addNewResRefName().setStringValue(name);
            if (ResourceRefMap.get(name) != null) {
                xmlResourceRef.setUri((String)ResourceRefMap.get(name));
            }
            resourceRefs[i] = new ResourceRefDConfigBean(ddResourceRef,  xmlResourceRef);
        }

    }

    public DConfigBean getDConfigBean(DDBean ddBean) throws ConfigurationException {
        String xpath = ddBean.getXpath();
        if (xpath.equals(ENC_XPATHS[0])) {
            String name = ddBean.getText("ejb-ref-name")[0];
            for (int i = 0; i < ejbRefs.length; i++) {
                EJBRefDConfigBean ejbRef = ejbRefs[i];
                if (ejbRef.getEjbRefName().equals(name)) {
                    return ejbRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(ENC_XPATHS[1])) {
            String name = ddBean.getText("ejb-ref-name")[0];
            for (int i = 0; i < ejbLocalRefs.length; i++) {
                EJBLocalRefDConfigBean ejbLocalRef = ejbLocalRefs[i];
                if (ejbLocalRef.getEjbRefName().equals(name)) {
                    return ejbLocalRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(ENC_XPATHS[2])) {
            String name = ddBean.getText("message-destination-ref-name")[0];
            for (int i = 0; i < messageDestinationRefs.length; i++) {
                MessageDestinationRefDConfigBean messageDestinationRef = messageDestinationRefs[i];
                if (messageDestinationRef.getMessageDestinationRefName().equals(name)) {
                    return messageDestinationRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(ENC_XPATHS[3])) {
            String name = ddBean.getText("resource-env-ref-name")[0];
            for (int i = 0; i < resourceEnvRefs.length; i++) {
                ResourceEnvRefDConfigBean resourceEnvRef = resourceEnvRefs[i];
                if (resourceEnvRef.getResourceEnvRefName().equals(name)) {
                    return resourceEnvRef;
                }
            }
            throw new ConfigurationException("no such ejb-ref-name" + name);
        } else if (xpath.equals(ENC_XPATHS[4])) {
            String name = ddBean.getText("res-ref-name")[0];
            for (int i = 0; i < resourceRefs.length; i++) {
                ResourceRefDConfigBean resourceRef = resourceRefs[i];
                if (resourceRef.getResourceRefName().equals(name)) {
                    return resourceRef;
                }
            }
            throw new ConfigurationException("no such res-ref-name" + name);
        } else {
            throw new ConfigurationException("Unrecognized XPath: " + ddBean.getXpath());
        }
    }
  /*
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

     */

    public interface XmlEnvRefs {
        GerEjbRefType[] getEjbRefs();
        GerEjbRefType addNewEjbRef();
        void removeEjbRef(int i);

        GerEjbLocalRefType[] getEjbLocalRefs();
        GerEjbLocalRefType addNewEjbLocalRef();
        void removeEjbLocalRef(int i);

        GerMessageDestinationRefType[] getMessageDestinationRefs();
        GerMessageDestinationRefType addNewMessageDestinationRef();
        void removeMessageDestinationRef(int i);

        GerResourceEnvRefType[] getResourceEnvRefs();
        GerResourceEnvRefType addNewResourceEnvRef();
        void removeResourceEnvRef(int i);

        GerResourceRefType[] getResourceRefs();
        GerResourceRefType addNewResourceRef();
        void removeResourceRef(int i);

    }
}
