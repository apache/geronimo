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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Entity;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.GeronimoEjbJarDocument;

/**
 * Knows how to store geronimo-ejb-jar.xml POJOs to a DOM
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/01 19:03:40 $
 */
public class GeronimoEjbJarStorer {
    public static void store(GeronimoEjbJarDocument jarDoc, Writer out) throws IOException {
        try {
            EjbJar jar = jarDoc.getEjbJar();
            Document doc = StorerUtil.createDocument();
            Element root = doc.createElementNS("http://java.sun.com/xml/ns/j2ee", "ejb-jar");
            root.setAttribute("xmlns", "http://java.sun.com/xml/ns/j2ee");
            root.setAttribute("xmlns:ger", "http://geronimo.apache.org/xml/schema/j2ee");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://java.sun.com/xml/ns/j2ee http://geronimo.apache.org/xml/schema/1.0/j2ee14/geronimo-ejb-jar.xsd");
            root.setAttribute("version", jar.getVersion());
            doc.appendChild(root);
            J2EEStorer.storeDisplayable(root, jar);
            if(jar.getEnterpriseBeans() != null && jar.getEnterpriseBeans().hasBeans()) {
                storeEjbs(StorerUtil.createChild(root, "enterprise-beans"), jar.getGeronimoEnterpriseBeans());
            }
            //todo: there will probably be Geronimo-specific content for relationships, assembly descriptor
            StorerUtil.writeXML(doc, out);
        } catch(ParserConfigurationException e) {
            throw new IOException("Unable to generate DOM document: "+e);
        } catch(TransformerException e) {
            throw new IOException("Unable to write document: "+e);
        }
    }

    static void storeEjbs(Element root, EnterpriseBeans beans) {
        Session[] session = beans.getGeronimoSession();
        for(int i = 0; i < session.length; i++) {
            Element se = StorerUtil.createChild(root, "session");
            storeSessionBean(se, session[i]);
        }
        Entity[] entity = beans.getGeronimoEntity();
        for(int i = 0; i < entity.length; i++) {
            Element ee = StorerUtil.createChild(root, "entity");
            storeEntityBean(ee, entity[i]);
        }
        MessageDriven[] mdb = beans.getGeronimoMessageDriven();
        for(int i = 0; i < mdb.length; i++) {
            Element me = StorerUtil.createChild(root, "message-driven");
            storeMessageDrivenBean(me, mdb[i]);
        }
    }

    static void storeSessionBean(Element root, Session bean) {
        J2EEStorer.storeDisplayable(root, bean);
        StorerUtil.createChildText(root, "ejb-name", bean.getEJBName());
        StorerUtil.createOptionalChildText(root, "home", bean.getHome());
        StorerUtil.createOptionalChildText(root, "remote", bean.getRemote());
        StorerUtil.createOptionalChildText(root, "local-home", bean.getLocalHome());
        StorerUtil.createOptionalChildText(root, "local", bean.getLocal());
        StorerUtil.createOptionalChildText(root, "service-endpoint", bean.getServiceEndpoint());
        StorerUtil.createChildText(root, "ejb-class", bean.getEJBClass());
        StorerUtil.createOptionalChildText(root, "session-type", bean.getSessionType());
        StorerUtil.createOptionalChildText(root, "transaction-type", bean.getTransactionType());
        GeronimoJ2EEStorer.storeJNDIEnvironmentRefs(root, bean);
        J2EEStorer.storeSecurityRoleRefs(root, bean.getSecurityRoleRef());
        if(bean.getSecurityIdentity() != null) {
            EjbJarStorer.storeSecurityIdentity(StorerUtil.createChild(root, "security-identity"), bean.getSecurityIdentity());
        }
        StorerUtil.createOptionalChildText(root, "jndi-name", bean.getJndiName());
    }

    static void storeEntityBean(Element root, Entity bean) {
        J2EEStorer.storeDisplayable(root, bean);
        StorerUtil.createChildText(root, "ejb-name", bean.getEJBName());
        StorerUtil.createOptionalChildText(root, "home", bean.getHome());
        StorerUtil.createOptionalChildText(root, "remote", bean.getRemote());
        StorerUtil.createOptionalChildText(root, "local-home", bean.getLocalHome());
        StorerUtil.createOptionalChildText(root, "local", bean.getLocal());
        StorerUtil.createChildText(root, "ejb-class", bean.getEJBClass());
        StorerUtil.createChildText(root, "persistence-type", bean.getPersistenceType());
        StorerUtil.createChildText(root, "prim-key-class", bean.getPrimKeyClass());
        StorerUtil.createChildText(root, "reentrant", bean.getReentrant());
        StorerUtil.createOptionalChildText(root, "cmp-version", bean.getCmpVersion());
        StorerUtil.createOptionalChildText(root, "abstract-schema-name", bean.getAbstractSchemaName());
        for(int i = 0; i < bean.getCmpField().length; i++) {
            EjbJarStorer.storeCmpField(StorerUtil.createChild(root, "cmp-field"), bean.getCmpField(i));
        }
        StorerUtil.createOptionalChildText(root, "primkey-field", bean.getPrimkeyField());
        GeronimoJ2EEStorer.storeJNDIEnvironmentRefs(root, bean);
        J2EEStorer.storeSecurityRoleRefs(root, bean.getSecurityRoleRef());
        if(bean.getSecurityIdentity() != null) {
            EjbJarStorer.storeSecurityIdentity(StorerUtil.createChild(root, "security-identity"), bean.getSecurityIdentity());
        }
        for(int i = 0; i < bean.getQuery().length; i++) {
            EjbJarStorer.storeQuery(StorerUtil.createChild(root, "query"), bean.getQuery(i));
        }
        StorerUtil.createOptionalChildText(root, "jndi-name", bean.getJndiName());
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
            EjbJarStorer.storeActivationConfig(StorerUtil.createChild(root, "activation-config"), bean.getActivationConfig());
        }
        GeronimoJ2EEStorer.storeJNDIEnvironmentRefs(root, bean);
        if(bean.getSecurityIdentity() != null) {
            EjbJarStorer.storeSecurityIdentity(StorerUtil.createChild(root, "security-identity"), bean.getSecurityIdentity());
        }
    }
}
