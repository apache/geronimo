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
import org.apache.geronimo.deployment.model.geronimo.ejb.Query;
import org.apache.geronimo.deployment.model.geronimo.ejb.Binding;
import org.apache.geronimo.deployment.model.geronimo.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.deployment.model.geronimo.ejb.Relationships;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbRelationshipRole;
import org.apache.geronimo.deployment.model.geronimo.ejb.RelationshipQuery;
import org.apache.geronimo.deployment.model.ejb.AssemblyDescriptor;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbRelation;

/**
 * Loads a Geronimo ejb-jar.xml file into POJOs
 *
 * @version $Revision: 1.14 $ $Date: 2003/11/19 11:07:57 $
 */
public class GeronimoEjbJarLoader {
    public static GeronimoEjbJarDocument load(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"ejb-jar".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not a Geronimo ejb-jar instance");
        }
        EjbJar jar = new EjbJar();
        jar.setVersion(root.getAttribute("version"));
        jar.setClassSpace(GeronimoJ2EELoader.loadClassSpace(root));
        jar.setSecurity(GeronimoJ2EELoader.loadSecurity(root));
        J2EELoader.loadDisplayable(root, jar);
        jar.setModuleName(LoaderUtil.getChildContent(root, "module-name"));
        String datasourceName = LoaderUtil.getChildContent(root, "datasource-name");
        if (datasourceName != null && datasourceName.length() > 0) {
            jar.setDatasourceName(datasourceName);
        }
        Element ebe = LoaderUtil.getChild(root, "enterprise-beans");
        if (ebe != null) {
            EnterpriseBeans eb = new EnterpriseBeans();
            jar.setEnterpriseBeans(eb);
            eb.setSession(loadSessions(ebe));
            eb.setEntity(loadEntities(ebe));
            eb.setMessageDriven(loadMessageDrivens(ebe));
        }
        //todo: override any Geronimo-specific relationship content
        Element re = LoaderUtil.getChild(root, "relationships");
        if (re != null) {
            Relationships rel = new Relationships();
            J2EELoader.loadDescribable(re, rel);
            rel.setEjbRelation(loadEjbRelations(re));
            jar.setRelationships(rel);
        }
        //todo: override any Geronimo-specific assembly-descriptor content
        Element ade = LoaderUtil.getChild(root, "assembly-descriptor");
        if (ade != null) {
            AssemblyDescriptor ad = new AssemblyDescriptor();
            EjbJarLoader.loadAssemblyDescriptor(ade, ad);
            jar.setAssemblyDescriptor(ad);
        }
        GeronimoEjbJarDocument result = new GeronimoEjbJarDocument();
        result.setEjbJar(jar);
        return result;
    }

    private static EjbRelation[] loadEjbRelations(Element re) {
        Element[] roots = LoaderUtil.getChildren(re, "ejb-relation");
        EjbRelation[] ejbRelations = new EjbRelation[roots.length];
        for (int i = 0; i < ejbRelations.length; i++) {
            EjbRelation ejbRelation = new EjbRelation();
            EjbJarLoader.loadEjbRelation(roots[i], ejbRelation);
            Element[] roles = LoaderUtil.getChildren(roots[i], "ejb-relationship-role");
            EjbRelationshipRole[] ejbRelationshipRoles = new EjbRelationshipRole[roles.length];
            for (int j = 0; j < roles.length; j++) {  //j == 0 or 1
                Element role = roles[j];
                EjbRelationshipRole ejbRelationshipRole = new EjbRelationshipRole();
                EjbJarLoader.loadEjbRelationshipRole(role, ejbRelationshipRole);
                ejbRelationshipRole.setQuery(loadRelationshipQuery(LoaderUtil.getChild(role, "query")));
                ejbRelationshipRole.setUpdate(loadRelationshipQuery(LoaderUtil.getChild(role, "update")));
                ejbRelationshipRoles[j] = ejbRelationshipRole;
            }
            ejbRelation.setEjbRelationshipRole(ejbRelationshipRoles);
            ejbRelations[i] = ejbRelation;
        }
        return ejbRelations;
    }

    private static RelationshipQuery loadRelationshipQuery(Element query) {
        RelationshipQuery relationshipQuery = new RelationshipQuery();
        relationshipQuery.setSql(LoaderUtil.getChildContent(query, "sql"));
        Element inputBinding = LoaderUtil.getChild(query, "input-binding");
        relationshipQuery.setInputBinding(loadBinding(inputBinding));
        Element outputBinding = LoaderUtil.getChild(query, "output-binding");
        relationshipQuery.setOutputBinding(loadBinding(outputBinding));
        return relationshipQuery;
    }

    private static MessageDriven[] loadMessageDrivens(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "message-driven");
        MessageDriven[] mdbs = new MessageDriven[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            mdbs[i] = new MessageDriven();
            loadEjb(root, mdbs[i]);
            mdbs[i].setMessageDestinationLink(LoaderUtil.getChildContent(root, "message-destination-link"));
            mdbs[i].setMessageDestinationType(LoaderUtil.getChildContent(root, "message-destination-type"));
            mdbs[i].setMessagingType(LoaderUtil.getChildContent(root, "messaging-type"));
            mdbs[i].setTransactionType(LoaderUtil.getChildContent(root, "transaction-type"));
            mdbs[i].setActivationConfig(loadActivationConfig(LoaderUtil.getChild(root, "activation-config")));
        }
        return mdbs;
    }

    private static ActivationConfig loadActivationConfig(Element root) {
        ActivationConfig activationConfig = new ActivationConfig();
        EjbJarLoader.loadActivationConfig(root, activationConfig);
        activationConfig.setActivationSpecClass(LoaderUtil.getChildContent(root, "activation-spec-class"));
        activationConfig.setResourceAdapterName(LoaderUtil.getChildContent(root, "resource-adapter-name"));
        return activationConfig;
    }

    private static Session[] loadSessions(Element ebe) {
        Element[] roots = LoaderUtil.getChildren(ebe, "session");
        Session[] sessions = new Session[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            sessions[i] = new Session();
            loadEjb(root, sessions[i]);
            sessions[i].setHome(LoaderUtil.getChildContent(root, "home"));
            sessions[i].setLocal(LoaderUtil.getChildContent(root, "local"));
            sessions[i].setLocalHome(LoaderUtil.getChildContent(root, "local-home"));
            sessions[i].setRemote(LoaderUtil.getChildContent(root, "remote"));
            sessions[i].setSecurityRoleRef(GeronimoJ2EELoader.loadSecurityRoleRefs(root));
            sessions[i].setServiceEndpoint(LoaderUtil.getChildContent(root, "service-endpoint"));
            sessions[i].setSessionType(LoaderUtil.getChildContent(root, "session-type"));
            sessions[i].setTransactionType(LoaderUtil.getChildContent(root, "transaction-type"));
        }
        return sessions;
    }

    private static Entity[] loadEntities(Element ebe) {
        Element[] roots = LoaderUtil.getChildren(ebe, "entity");
        Entity[] entities = new Entity[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            entities[i] = new Entity();
            loadEjb(root, entities[i]);
            entities[i].setHome(LoaderUtil.getChildContent(root, "home"));
            entities[i].setLocal(LoaderUtil.getChildContent(root, "local"));
            entities[i].setLocalHome(LoaderUtil.getChildContent(root, "local-home"));
            entities[i].setRemote(LoaderUtil.getChildContent(root, "remote"));
            entities[i].setSecurityRoleRef(GeronimoJ2EELoader.loadSecurityRoleRefs(root));
            entities[i].setPersistenceType(LoaderUtil.getChildContent(root, "persistence-type"));
            entities[i].setPrimKeyClass(LoaderUtil.getChildContent(root, "prim-key-class"));
            entities[i].setReentrant(LoaderUtil.getChildContent(root, "reentrant"));
            entities[i].setCmpVersion(LoaderUtil.getChildContent(root, "cmp-version"));
            entities[i].setAbstractSchemaName(LoaderUtil.getChildContent(root, "abstract-schema-name"));
            entities[i].setPrimkeyField(LoaderUtil.getChildContent(root, "primkey-field"));
            entities[i].setCmpField(EjbJarLoader.loadCmpFields(root));
            Element[] query;
            query = LoaderUtil.getChildren(root, "query");
            entities[i].setQuery(GeronimoEjbJarLoader.loadQueries(query));
            Element[] update = LoaderUtil.getChildren(root, "update");
            entities[i].setUpdate(GeronimoEjbJarLoader.loadQueries(update));
            Element[] call = LoaderUtil.getChildren(root, "call");
            entities[i].setCall(GeronimoEjbJarLoader.loadQueries(call));
        }
        return entities;
    }

    static Query[] loadQueries(Element[] roots) {
        Query[] queries = new Query[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            Query query = (Query) EjbJarLoader.loadQuery(root, new Query());
            query.setSql(LoaderUtil.getChildContent(root, "sql"));
            query.setInputBinding(loadBinding(LoaderUtil.getChild(root, "input-binding")));
            Element outputBinding = LoaderUtil.getChild(root, "output-binding");
            query.setAbstractSchemaName(LoaderUtil.getAttribute(outputBinding, "abstract-schema-name"));
            query.setOutputBinding(loadBinding(outputBinding));
            query.setMultiplicity(LoaderUtil.getAttribute(outputBinding, "multiplicity"));
            queries[i] = query;
        }
        return queries;
    }

    private static Binding[] loadBinding(Element parent) {
        if (parent == null) {
            return new Binding[0];
        }
        Element[] roots = LoaderUtil.getChildren(parent, "binding");
        Binding[] bindings = new Binding[roots.length];
        for (int i = 0; i < bindings.length; i++) {
            Element root = roots[i];
            Binding binding = new Binding();
            binding.setType(LoaderUtil.getAttribute(root, "type"));
            binding.setParam(Integer.parseInt(LoaderUtil.getAttribute(root, "param")));
            bindings[i] = binding;
        }
        return bindings;
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
        bean.setServiceRef(GeronimoJ2EELoader.loadServiceRefs(root));
    }
}
