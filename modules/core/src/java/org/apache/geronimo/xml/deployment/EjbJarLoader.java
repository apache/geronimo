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

import org.apache.geronimo.deployment.model.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.ejb.ActivationConfigProperty;
import org.apache.geronimo.deployment.model.ejb.AssemblyDescriptor;
import org.apache.geronimo.deployment.model.ejb.CmpField;
import org.apache.geronimo.deployment.model.ejb.CmrField;
import org.apache.geronimo.deployment.model.ejb.ContainerTransaction;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.deployment.model.ejb.EjbJar;
import org.apache.geronimo.deployment.model.ejb.EjbJarDocument;
import org.apache.geronimo.deployment.model.ejb.EjbRelation;
import org.apache.geronimo.deployment.model.ejb.EjbRelationshipRole;
import org.apache.geronimo.deployment.model.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.ejb.Entity;
import org.apache.geronimo.deployment.model.ejb.ExcludeList;
import org.apache.geronimo.deployment.model.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.ejb.Method;
import org.apache.geronimo.deployment.model.ejb.MethodPermission;
import org.apache.geronimo.deployment.model.ejb.Query;
import org.apache.geronimo.deployment.model.ejb.QueryMethod;
import org.apache.geronimo.deployment.model.ejb.RelationshipRoleSource;
import org.apache.geronimo.deployment.model.ejb.Relationships;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.SecurityIdentity;
import org.apache.geronimo.deployment.model.ejb.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Knows how to load a set of POJOs from a DOM representing an ejb-jar.xml
 * deployment descriptor.
 *
 * @version $Revision: 1.12 $ $Date: 2003/11/19 00:33:59 $
 */
public class EjbJarLoader {
    public static EjbJarDocument load(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"ejb-jar".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not an ejb-jar instance");
        }
        EjbJar jar = new EjbJar();
        jar.setVersion(root.getAttribute("version"));
        J2EELoader.loadDisplayable(root, jar);
        Element ebe = LoaderUtil.getChild(root, "enterprise-beans");
        if(ebe != null) {
            EnterpriseBeans eb = new EnterpriseBeans();
            jar.setEnterpriseBeans(eb);
            eb.setSession(loadSessions(ebe));
            eb.setEntity(loadEntities(ebe));
            eb.setMessageDriven(loadMessageDrivens(ebe));
        }
        Element re = LoaderUtil.getChild(root, "relationships");
        if(re != null) {
            Relationships rel = new Relationships();
            J2EELoader.loadDescribable(re, rel);
            rel.setEjbRelation(loadEjbRelations(re));
            jar.setRelationships(rel);
        }
        Element ade = LoaderUtil.getChild(root, "assembly-descriptor");
        if(ade != null) {
            AssemblyDescriptor ad = new AssemblyDescriptor();
            loadAssemblyDescriptor(ade, ad);
            jar.setAssemblyDescriptor(ad);
        }
        EjbJarDocument result = new EjbJarDocument();
        result.setEjbJar(jar);
        return result;
    }

    static void loadAssemblyDescriptor(Element root, AssemblyDescriptor ad) {
        ad.setContainerTransaction(loadContainerTransactions(root));
        ad.setExcludeList(loadExcludeList(LoaderUtil.getChild(root, "exclude-list")));
        ad.setMessageDestination(J2EELoader.loadMessageDestinations(root));
        ad.setMethodPermission(loadMethodPermissions(root));
        ad.setSecurityRole(J2EELoader.loadSecurityRoles(root));
    }

    private static ExcludeList loadExcludeList(Element parent) {
        if(parent == null) {
            return null;
        }
        ExcludeList list = new ExcludeList();
        J2EELoader.loadDescribable(parent, list);
        list.setMethod(loadMethods(parent));
        return list;
    }

    private static MethodPermission[] loadMethodPermissions(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "method-permission");
        MethodPermission[] perms = new MethodPermission[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            perms[i] = new MethodPermission();
            J2EELoader.loadDescribable(root, perms[i]);
            perms[i].setUnchecked(LoaderUtil.getChild(root, "unchecked") != null);
            perms[i].setRoleName(J2EELoader.loadRoleNames(root));
            perms[i].setMethod(loadMethods(root));
        }
        return perms;
    }

    private static ContainerTransaction[] loadContainerTransactions(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "container-transaction");
        ContainerTransaction[] tx = new ContainerTransaction[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            tx[i] = new ContainerTransaction();
            J2EELoader.loadDescribable(root, tx[i]);
            tx[i].setTransAttribute(LoaderUtil.getChildContent(root, "trans-attribute"));
            tx[i].setMethod(loadMethods(root));
        }
        return tx;
    }

    private static Method[] loadMethods(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "method");
        Method[] meth = new Method[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            meth[i] = new Method();
            J2EELoader.loadDescribable(root, meth[i]);
            meth[i].setEjbName(LoaderUtil.getChildContent(root, "ejb-name"));
            meth[i].setMethodIntf(LoaderUtil.getChildContent(root, "method-intf"));
            meth[i].setMethodName(LoaderUtil.getChildContent(root, "method-name"));
            Element e = LoaderUtil.getChild(root, "method-params");
            if(e != null) {
                meth[i].setMethodParam(LoaderUtil.getChildrenContent(e, "method-param"));
            }
        }
        return meth;
    }

    static EjbRelation[] loadEjbRelations(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "ejb-relation");
        EjbRelation[] rels = new EjbRelation[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            rels[i] = new EjbRelation();
            J2EELoader.loadDescribable(root, rels[i]);
            rels[i].setEjbRelationName(LoaderUtil.getChildContent(root, "ejb-relation-name"));
            rels[i].setEjbRelationshipRole(loadRelationshipRoles(root));
        }
        return rels;
    }

    private static EjbRelationshipRole[] loadRelationshipRoles(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "ejb-relationship-role");
        EjbRelationshipRole[] roles = new EjbRelationshipRole[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            roles[i] = new EjbRelationshipRole();
            J2EELoader.loadDescribable(root, roles[i]);
            roles[i].setEjbRelationshipRoleName(LoaderUtil.getChildContent(root, "ejb-relationship-role-name"));
            roles[i].setMultiplicity(LoaderUtil.getChildContent(root, "multiplicity"));
            roles[i].setCascadeDelete(LoaderUtil.getChild(root, "cascade-delete") != null);
            roles[i].setRelationshipRoleSource(loadRelationshipRoleSource(LoaderUtil.getChild(root, "relationship-role-source")));
            roles[i].setCmrField(loadCmrField(LoaderUtil.getChild(root, "cmr-field")));
        }
        return roles;
    }

    private static RelationshipRoleSource loadRelationshipRoleSource(Element parent) {
        if(parent == null) {
            return null;
        }
        RelationshipRoleSource source = new RelationshipRoleSource();
        J2EELoader.loadDescribable(parent, source);
        source.setEjbName(LoaderUtil.getChildContent(parent, "ejb-name"));
        return source;
    }

    private static CmrField loadCmrField(Element parent) {
        if(parent == null) {
            return null;
        }
        CmrField field = new CmrField();
        J2EELoader.loadDescribable(parent, field);
        field.setCmrFieldName(LoaderUtil.getChildContent(parent, "cmr-field-name"));
        field.setCmrFieldType(LoaderUtil.getChildContent(parent, "cmr-field-type"));
        return field;
    }

    private static MessageDriven[] loadMessageDrivens(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "message-driven");
        MessageDriven[] mdbs = new MessageDriven[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            mdbs[i] = new MessageDriven();
            loadEjb(root, mdbs[i]);
            loadReferencesForEjb(root, mdbs[i]);
            mdbs[i].setMessageDestinationLink(LoaderUtil.getChildContent(root, "message-destination-link"));
            mdbs[i].setMessageDestinationType(LoaderUtil.getChildContent(root, "message-destination-type"));
            mdbs[i].setMessagingType(LoaderUtil.getChildContent(root, "messaging-type"));
            mdbs[i].setTransactionType(LoaderUtil.getChildContent(root, "transaction-type"));
            mdbs[i].setActivationConfig(loadActivationConfig(LoaderUtil.getChild(root, "activation-config"), new ActivationConfig()));
        }
        return mdbs;
    }

    static ActivationConfig loadActivationConfig(Element parent, ActivationConfig activationConfig) {
        if(parent == null) {
            return null;//???
        }
        J2EELoader.loadDescribable(parent, activationConfig);
        Element[] roots = LoaderUtil.getChildren(parent, "activation-config-property");
        ActivationConfigProperty[] props = new ActivationConfigProperty[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            props[i] = new ActivationConfigProperty();
            props[i].setActivationConfigPropertyName(LoaderUtil.getChildContent(root, "activation-config-property-name"));
            props[i].setActivationConfigPropertyValue(LoaderUtil.getChildContent(root, "activation-config-property-value"));
        }
        activationConfig.setActivationConfigProperty(props);
        return activationConfig;
    }

    private static Session[] loadSessions(Element ebe) {
        Element[] roots = LoaderUtil.getChildren(ebe, "session");
        Session[] sessions = new Session[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            sessions[i] = new Session();
            loadRpcBean(root, sessions[i]);
            sessions[i].setServiceEndpoint(LoaderUtil.getChildContent(root, "service-endpoint"));
            sessions[i].setSessionType(LoaderUtil.getChildContent(root, "session-type"));
            sessions[i].setTransactionType(LoaderUtil.getChildContent(root, "transaction-type"));
        }
        return sessions;
    }

    private static Entity[] loadEntities(Element ebe) {
        Element[] roots = LoaderUtil.getChildren(ebe, "entity");
        Entity[] entities = new Entity[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            entities[i] = new Entity();
            loadRpcBean(root, entities[i]);
            entities[i].setPersistenceType(LoaderUtil.getChildContent(root, "persistence-type"));
            entities[i].setPrimKeyClass(LoaderUtil.getChildContent(root, "prim-key-class"));
            entities[i].setReentrant(LoaderUtil.getChildContent(root, "reentrant"));
            entities[i].setCmpVersion(LoaderUtil.getChildContent(root, "cmp-version"));
            entities[i].setAbstractSchemaName(LoaderUtil.getChildContent(root, "abstract-schema-name"));
            entities[i].setPrimkeyField(LoaderUtil.getChildContent(root, "primkey-field"));
            entities[i].setCmpField(loadCmpFields(root));
            entities[i].setQuery(loadQueries(root));
        }
        return entities;
    }

    static Query[] loadQueries(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "query");
        Query[] queries = new Query[roots.length];
        for(int i = 0; i < roots.length; i++) {
            queries[i] = loadQuery(roots[i], new Query());
        }
        return queries;
    }

    static Query loadQuery(Element root, Query query) {
        J2EELoader.loadDescribable(root, query);
        query.setEjbQl(LoaderUtil.getChildContent(root, "ejb-ql"));
        query.setResultTypeMapping(LoaderUtil.getChildContent(root, "result-type-mapping"));
        query.setQueryMethod(loadQueryMethod(LoaderUtil.getChild(root, "query-method")));
        return query;
    }

    private static QueryMethod loadQueryMethod(Element root) {
        if(root == null) {
            return null;
        }
        QueryMethod method = new QueryMethod();
        method.setMethodName(LoaderUtil.getChildContent(root, "method-name"));
        Element methodParams = LoaderUtil.getChild(root, "method-params");
        if (methodParams != null) {
            method.setMethodParam(LoaderUtil.getChildrenContent(methodParams, "method-param"));
        }
        return method;
    }

    static CmpField[] loadCmpFields(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "cmp-field");
        CmpField[] fields = new CmpField[roots.length];
        for(int i = 0; i < roots.length; i++) {
            fields[i] = new CmpField();
            J2EELoader.loadDescribable(roots[i], fields[i]);
            fields[i].setFieldName(LoaderUtil.getChildContent(roots[i], "field-name"));
        }
        return fields;
    }

    private static void loadRpcBean(Element root, RpcBean bean) {
        loadEjb(root, bean);
        loadReferencesForEjb(root, bean);
        bean.setHome(LoaderUtil.getChildContent(root, "home"));
        bean.setLocal(LoaderUtil.getChildContent(root, "local"));
        bean.setLocalHome(LoaderUtil.getChildContent(root, "local-home"));
        bean.setRemote(LoaderUtil.getChildContent(root, "remote"));
        bean.setSecurityRoleRef(J2EELoader.loadSecurityRoleRefs(root));
    }

    public static void loadEjb(Element root, Ejb bean) {
        J2EELoader.loadDisplayable(root, bean);
        bean.setEJBName(LoaderUtil.getChildContent(root, "ejb-name"));
        bean.setEJBClass(LoaderUtil.getChildContent(root, "ejb-class"));
        bean.setSecurityIdentity(loadSecurityIdentity(LoaderUtil.getChild(root, "security-identity")));
    }

    private static void loadReferencesForEjb(Element root, Ejb bean) {
        bean.setEJBRef(J2EELoader.loadEJBRefs(root));
        bean.setEJBLocalRef(J2EELoader.loadEJBLocalRefs(root));
        bean.setResourceRef(J2EELoader.loadResourceRefs(root));
        bean.setResourceEnvRef(J2EELoader.loadResourceEnvRefs(root));
        bean.setMessageDestinationRef(J2EELoader.loadMessageDestinationRefs(root));
        bean.setEnvEntry(J2EELoader.loadEnvEntries(root));
        bean.setServiceRef(J2EELoader.loadServiceRefs(root));
    }

    private static SecurityIdentity loadSecurityIdentity(Element root) {
        if(root == null) {
            return null;
        }
        SecurityIdentity id = new SecurityIdentity();
        J2EELoader.loadDescribable(root, id);
        if(LoaderUtil.getChild(root, "use-caller-identity") != null) {
            id.setUseCallerIdentity(true);
        } else {
            id.setUseCallerIdentity(false);
            id.setRunAs(J2EELoader.loadRunAs(LoaderUtil.getChild(root, "run-as")));
        }
        return id;
    }
}
