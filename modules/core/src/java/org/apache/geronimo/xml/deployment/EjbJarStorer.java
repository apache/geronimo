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
package org.apache.geronimo.xml.deployment;

import java.io.Writer;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.geronimo.deployment.model.ejb.EjbJarDocument;
import org.apache.geronimo.deployment.model.ejb.EjbJar;
import org.apache.geronimo.deployment.model.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.ejb.Session;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.deployment.model.ejb.SecurityIdentity;
import org.apache.geronimo.deployment.model.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.ejb.Entity;
import org.apache.geronimo.deployment.model.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.ejb.ActivationConfigProperty;
import org.apache.geronimo.deployment.model.ejb.CmpField;
import org.apache.geronimo.deployment.model.ejb.Query;
import org.apache.geronimo.deployment.model.ejb.QueryMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Stores an ejb-jar.xml DD from POJOs to a DOM tree
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/01 19:03:40 $
 */
public class EjbJarStorer {
    public static void store(EjbJarDocument jarDoc, Writer out) throws IOException {
        try {
            EjbJar jar = jarDoc.getEjbJar();
            Document doc = StorerUtil.createDocument();
            Element root = doc.createElementNS("http://java.sun.com/xml/ns/j2ee", "ejb-jar");
            root.setAttribute("xmlns", "http://java.sun.com/xml/ns/j2ee");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd");
            root.setAttribute("version", jar.getVersion());
            doc.appendChild(root);
            J2EEStorer.storeDisplayable(root, jar);
            if(jar.getEnterpriseBeans() != null && jar.getEnterpriseBeans().hasBeans()) {
                storeEjbs(StorerUtil.createChild(root, "enterprise-beans"), jar.getEnterpriseBeans());
            }
            //todo: relationships, assembly descriptor
            StorerUtil.writeXML(doc, out);
        } catch(ParserConfigurationException e) {
            throw new IOException("Unable to generate DOM document: "+e);
        } catch(TransformerException e) {
            throw new IOException("Unable to write document: "+e);
        }
    }

    static void storeEjbs(Element root, EnterpriseBeans beans) {
        Session[] session = beans.getSession();
        for(int i = 0; i < session.length; i++) {
            Element se = StorerUtil.createChild(root, "session");
            storeSessionBean(se, session[i]);
        }
        Entity[] entity = beans.getEntity();
        for(int i = 0; i < entity.length; i++) {
            Element ee = StorerUtil.createChild(root, "entity");
            storeEntityBean(ee, entity[i]);
        }
        MessageDriven[] mdb = beans.getMessageDriven();
        for(int i = 0; i < mdb.length; i++) {
            Element me = StorerUtil.createChild(root, "message-driven");
            storeMessageDrivenBean(me, mdb[i]);
        }
    }

    static void storeSessionBean(Element root, Session session) {
        storeRpcBean(root, session);
        StorerUtil.createOptionalChildText(root, "service-endpoint", session.getServiceEndpoint());
        StorerUtil.createChildText(root, "ejb-class", session.getEJBClass());
        StorerUtil.createOptionalChildText(root, "session-type", session.getSessionType());
        StorerUtil.createOptionalChildText(root, "transaction-type", session.getTransactionType());
        storeReferences(root, session);
        J2EEStorer.storeSecurityRoleRefs(root, session.getSecurityRoleRef());
        if(session.getSecurityIdentity() != null) {
            storeSecurityIdentity(StorerUtil.createChild(root, "security-identity"), session.getSecurityIdentity());
        }
    }

    static void storeEntityBean(Element root, Entity bean) {
        storeRpcBean(root, bean);
        StorerUtil.createChildText(root, "ejb-class", bean.getEJBClass());
        StorerUtil.createChildText(root, "persistence-type", bean.getPersistenceType());
        StorerUtil.createChildText(root, "prim-key-class", bean.getPrimKeyClass());
        StorerUtil.createChildText(root, "reentrant", bean.getReentrant());
        StorerUtil.createOptionalChildText(root, "cmp-version", bean.getCmpVersion());
        StorerUtil.createOptionalChildText(root, "abstract-schema-name", bean.getAbstractSchemaName());
        for(int i = 0; i < bean.getCmpField().length; i++) {
            storeCmpField(StorerUtil.createChild(root, "cmp-field"), bean.getCmpField(i));
        }
        StorerUtil.createOptionalChildText(root, "primkey-field", bean.getPrimkeyField());
        storeReferences(root, bean);
        J2EEStorer.storeSecurityRoleRefs(root, bean.getSecurityRoleRef());
        if(bean.getSecurityIdentity() != null) {
            storeSecurityIdentity(StorerUtil.createChild(root, "security-identity"), bean.getSecurityIdentity());
        }
        for(int i = 0; i < bean.getQuery().length; i++) {
            storeQuery(StorerUtil.createChild(root, "query"), bean.getQuery(i));
        }
    }

    static void storeQuery(Element parent, Query query) {
        J2EEStorer.storeDescribable(parent, query);
        storeQueryMethod(StorerUtil.createChild(parent, "query-method"), query.getQueryMethod());
        StorerUtil.createChildText(parent, "result-type-mapping", query.getResultTypeMapping());
        StorerUtil.createChildText(parent, "ejb-ql", query.getEjbQl());
    }

    static void storeQueryMethod(Element parent, QueryMethod method) {
        StorerUtil.createChildText(parent, "method-name", method.getMethodName());
        Element e = StorerUtil.createChild(parent, "method-params");
        for(int i = 0; i < method.getMethodParam().length; i++) {
            StorerUtil.createChildText(e, "method-param", method.getMethodParam(i));
        }
    }

    static void storeCmpField(Element parent, CmpField field) {
        J2EEStorer.storeDescribable(parent, field);
        StorerUtil.createChildText(parent, "field-name", field.getFieldName());
    }

    static void storeMessageDrivenBean(Element root, MessageDriven bean) {
        J2EEStorer.storeDisplayable(root, bean);
        StorerUtil.createChildText(root, "ejb-name", bean.getEJBName());
        StorerUtil.createChildText(root, "ejb-class", bean.getEJBClass());
        StorerUtil.createOptionalChildText(root, "messaging-type", bean.getMessagingType());
        StorerUtil.createChildText(root, "transaction-type", bean.getTransactionType());
        StorerUtil.createOptionalChildText(root, "message-destination-type", bean.getMessageDestinationType());
        StorerUtil.createOptionalChildText(root, "message-destination-link", bean.getMessageDestinationLink());
        if(bean.getActivationConfig() != null) {
            storeActivationConfig(StorerUtil.createChild(root, "activation-config"), bean.getActivationConfig());
        }
        storeReferences(root, bean);
        if(bean.getSecurityIdentity() != null) {
            storeSecurityIdentity(StorerUtil.createChild(root, "security-identity"), bean.getSecurityIdentity());
        }
    }

    static void storeActivationConfig(Element parent, ActivationConfig config) {
        J2EEStorer.storeDescribable(parent, config);
        for(int i = 0; i < config.getActivationConfigProperty().length; i++) {
            storeActivationConfigProperty(StorerUtil.createChild(parent, "activation-config-property"), config.getActivationConfigProperty(i));
        }
    }

    static void storeActivationConfigProperty(Element parent, ActivationConfigProperty property) {
        StorerUtil.createChildText(parent, "activation-config-property-name", property.getActivationConfigPropertyName());
        StorerUtil.createChildText(parent, "activation-config-property-value", property.getActivationConfigPropertyValue());
    }

    static void storeRpcBean(Element root, RpcBean bean) {
        J2EEStorer.storeDisplayable(root, bean);
        StorerUtil.createChildText(root, "ejb-name", bean.getEJBName());
        StorerUtil.createOptionalChildText(root, "home", bean.getHome());
        StorerUtil.createOptionalChildText(root, "remote", bean.getRemote());
        StorerUtil.createOptionalChildText(root, "local-home", bean.getLocalHome());
        StorerUtil.createOptionalChildText(root, "local", bean.getLocal());
    }

    static void storeReferences(Element root, Ejb bean) {
        J2EEStorer.storeEnvEntries(root, bean.getEnvEntry());
        J2EEStorer.storeEJBRefs(root, bean.getEJBRef());
        J2EEStorer.storeEJBLocalRefs(root, bean.getEJBLocalRef());
        J2EEStorer.storeServiceRefs(root, bean.getServiceRef());
        J2EEStorer.storeResourceRefs(root, bean.getResourceRef());
        J2EEStorer.storeResourceEnvRefs(root, bean.getResourceEnvRef());
        J2EEStorer.storeMessageDestinationRefs(root, bean.getMessageDestinationRef());
    }

    static void storeSecurityIdentity(Element root, SecurityIdentity identity) {
        J2EEStorer.storeDescribable(root, identity);
        if(identity.isUseCallerIdentity()) {
            StorerUtil.createChild(root, "use-caller-identity");
        } else if(identity.getRunAs() != null) {
            J2EEStorer.storeRunAs(StorerUtil.createChild(root, "run-as"), identity.getRunAs());
        }
    }
}
