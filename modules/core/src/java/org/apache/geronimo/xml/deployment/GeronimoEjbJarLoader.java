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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.geronimo.deployment.model.geronimo.ejb.GeronimoEjbJarDocument;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Entity;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.ejb.Ejb;

/**
 * Loads a Geronimo ejb-jar.xml file into POJOs
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/17 01:47:14 $
 */
public class GeronimoEjbJarLoader {
    public static GeronimoEjbJarDocument load(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"ejb-jar".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not a Geronimo ejb-jar instance");
        }
        EjbJar jar = new EjbJar();
        Element ebe = LoaderUtil.getChild(root, "enterprise-beans");
        if(ebe != null) {
            EnterpriseBeans eb = new EnterpriseBeans();
            jar.setEnterpriseBeans(eb);
            eb.setSession(loadSessions(ebe));
            eb.setEntity(loadEntities(ebe));
            eb.setMessageDriven(loadMessageDrivens(ebe));
        }
        GeronimoEjbJarDocument result = new GeronimoEjbJarDocument();
        result.setEjbJar(jar);
        return result;
    }

    private static MessageDriven[] loadMessageDrivens(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "message-driven");
        MessageDriven[] mdbs = new MessageDriven[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            mdbs[i] = new MessageDriven();
            loadEjb(root, mdbs[i]);
        }
        return mdbs;
    }

    private static Session[] loadSessions(Element ebe) {
        Element[] roots = LoaderUtil.getChildren(ebe, "session");
        Session[] sessions = new Session[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            sessions[i] = new Session();
            loadEjb(root, sessions[i]);
            sessions[i].setSecurityRoleRef(J2EELoader.loadSecurityRoleRefs(root));
            sessions[i].setJndiName(LoaderUtil.getChildContent(root, "jndi-name"));
        }
        return sessions;
    }

    private static Entity[] loadEntities(Element ebe) {
        Element[] roots = LoaderUtil.getChildren(ebe, "entity");
        Entity[] entities = new Entity[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            entities[i] = new Entity();
            loadEjb(root, entities[i]);
            entities[i].setSecurityRoleRef(J2EELoader.loadSecurityRoleRefs(root));
            entities[i].setJndiName(LoaderUtil.getChildContent(root, "jndi-name"));
        }
        return entities;
    }

    private static void loadEjb(Element root, Ejb bean) {
        EjbJarLoader.loadEjb(root, bean);
        // the rest is loaded by a separate method in EjbJarLoader
        bean.setEJBRef(GeronimoJ2EELoader.loadEJBRefs(root));
        bean.setEJBLocalRef(GeronimoJ2EELoader.loadEJBLocalRefs(root));
        bean.setResourceRef(GeronimoJ2EELoader.loadResourceRefs(root));
        bean.setResourceEnvRef(GeronimoJ2EELoader.loadResourceEnvRefs(root));
        bean.setMessageDestinationRef(GeronimoJ2EELoader.loadMessageDestinationRefs(root));
        bean.setEnvEntry(J2EELoader.loadEnvEntries(root));
        bean.setServiceRef(J2EELoader.loadServiceRefs(root));
    }
}
