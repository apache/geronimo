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
import org.apache.geronimo.deployment.model.ejb.EjbJar;
import org.apache.geronimo.deployment.model.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.ejb.Session;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.deployment.model.ejb.SecurityIdentity;
import org.apache.geronimo.deployment.model.ejb.Entity;
import org.apache.geronimo.deployment.model.ejb.CmpField;
import org.apache.geronimo.deployment.model.ejb.Query;
import org.apache.geronimo.deployment.model.ejb.QueryMethod;
import org.apache.geronimo.deployment.model.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.ejb.ActivationConfigProperty;
import org.apache.geronimo.deployment.model.ejb.EjbJarDocument;
import org.apache.geronimo.deployment.model.ejb.Relationships;
import org.apache.geronimo.deployment.model.ejb.EjbRelation;
import org.apache.geronimo.deployment.model.ejb.EjbRelationshipRole;
import org.apache.geronimo.deployment.model.ejb.CmrField;
import org.apache.geronimo.deployment.model.ejb.RelationshipRoleSource;
import org.apache.geronimo.deployment.model.ejb.AssemblyDescriptor;
import org.apache.geronimo.deployment.model.ejb.ContainerTransaction;
import org.apache.geronimo.deployment.model.ejb.Method;
import org.apache.geronimo.deployment.model.ejb.SecurityRole;
import org.apache.geronimo.deployment.model.ejb.MethodPermission;
import org.apache.geronimo.deployment.model.ejb.ExcludeList;
import org.apache.geronimo.deployment.model.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.EJBLocalRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.j2ee.ServiceRef;
import org.apache.geronimo.deployment.model.j2ee.MessageDestination;

/**
 * Knows how to load a set of POJOs from a DOM representing an ejb-jar.xml
 * deployment descriptor.
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/05 20:18:03 $
 */
public class EjbJarLoader {
    private J2EELoader j2eeLoader = new J2EELoader();

    public EjbJarDocument load(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"ejb-jar".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not an ejb-jar instance");
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
        Element re = LoaderUtil.getChild(root, "relationships");
        if(re != null) {
            Relationships rel = new Relationships();
            j2eeLoader.loadDescribable(re, rel);
            rel.setEjbRelation(loadEjbRelations(re));
            jar.setRelationships(rel);
        }
        Element ade = LoaderUtil.getChild(root, "assembly-descriptor");
        if(ade != null) {
            AssemblyDescriptor ad = new AssemblyDescriptor();
            ad.setContainerTransaction(loadContainerTransactions(ade));
            ad.setExcludeList(loadExcludeList(LoaderUtil.getChild(ade, "exclude-list")));
            ad.setMessageDestination(j2eeLoader.loadMessageDestinations(ade, new MessageDestination[0]));
            ad.setMethodPermission(loadMethodPermissions(ade));
            ad.setSecurityRole(loadSecurityRoles(ade));
            jar.setAssemblyDescriptor(ad);
        }
        EjbJarDocument result = new EjbJarDocument();
        result.setEjbJar(jar);
        return result;
    }

    private ExcludeList loadExcludeList(Element parent) {
        if(parent == null) {
            return null;
        }
        ExcludeList list = new ExcludeList();
        j2eeLoader.loadDescribable(parent, list);
        list.setMethod(loadMethods(parent));
        return list;
    }

    private MethodPermission[] loadMethodPermissions(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "method-permission");
        MethodPermission[] perms = new MethodPermission[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            perms[i] = new MethodPermission();
            j2eeLoader.loadDescribable(root, perms[i]);
            perms[i].setUnchecked(LoaderUtil.getChild(root, "unchecked") != null);
            perms[i].setRoleName(LoaderUtil.getChildContent(root, "role-name"));
            perms[i].setMethod(loadMethods(root));
        }
        return perms;
    }

    private SecurityRole[] loadSecurityRoles(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "security-role");
        SecurityRole[] roles = new SecurityRole[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            roles[i] = new SecurityRole();
            j2eeLoader.loadDescribable(root, roles[i]);
            roles[i].setRoleName(LoaderUtil.getChildContent(root, "role-name"));
        }
        return roles;
    }

    private ContainerTransaction[] loadContainerTransactions(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "container-transaction");
        ContainerTransaction[] tx = new ContainerTransaction[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            tx[i] = new ContainerTransaction();
            j2eeLoader.loadDescribable(root, tx[i]);
            tx[i].setTransAttribute(LoaderUtil.getChildContent(root, "trans-attribute"));
            tx[i].setMethod(loadMethods(root));
        }
        return tx;
    }

    private Method[] loadMethods(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "method");
        Method[] meth = new Method[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            meth[i] = new Method();
            j2eeLoader.loadDescribable(root, meth[i]);
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

    private EjbRelation[] loadEjbRelations(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "ejb-relation");
        EjbRelation[] rels = new EjbRelation[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            rels[i] = new EjbRelation();
            j2eeLoader.loadDescribable(root, rels[i]);
            rels[i].setEjbRelationName(LoaderUtil.getChildContent(root, "ejb-relation-name"));
            rels[i].setEjbRelationshipRole(loadRelationshipRoles(root));
        }
        return rels;
    }

    private EjbRelationshipRole[] loadRelationshipRoles(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "ejb-relationship-role");
        EjbRelationshipRole[] roles = new EjbRelationshipRole[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            roles[i] = new EjbRelationshipRole();
            j2eeLoader.loadDescribable(root, roles[i]);
            roles[i].setEjbRelationshipRoleName(LoaderUtil.getChildContent(root, "ejb-relationship-role-name"));
            roles[i].setMultiplicity(LoaderUtil.getChildContent(root, "multiplicity"));
            roles[i].setCascadeDelete(LoaderUtil.getChild(root, "cascade-delete") != null);
            roles[i].setRelationshipRoleSource(loadRelationshipRoleSource(LoaderUtil.getChild(root, "relationship-role-source")));
            roles[i].setCmrField(loadCmrField(LoaderUtil.getChild(root, "cmr-field")));
        }
        return roles;
    }

    private RelationshipRoleSource loadRelationshipRoleSource(Element parent) {
        if(parent == null) {
            return null;
        }
        RelationshipRoleSource source = new RelationshipRoleSource();
        j2eeLoader.loadDescribable(parent, source);
        source.setEjbName(LoaderUtil.getChildContent(parent, "ejb-name"));
        return source;
    }

    private CmrField loadCmrField(Element parent) {
        if(parent == null) {
            return null;
        }
        CmrField field = new CmrField();
        j2eeLoader.loadDescribable(parent, field);
        field.setCmrFieldName(LoaderUtil.getChildContent(parent, "cmr-field-name"));
        field.setCmrFieldType(LoaderUtil.getChildContent(parent, "cmr-field-type"));
        return field;
    }

    private MessageDriven[] loadMessageDrivens(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "message-driven");
        MessageDriven[] mdbs = new MessageDriven[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            mdbs[i] = new MessageDriven();
            loadEjb(root, mdbs[i]);
            mdbs[i].setMessageDestinationLink(LoaderUtil.getChildContent(root, "message-destination-link"));
            mdbs[i].setMessageDestinationType(LoaderUtil.getChildContent(root, "message-destination-type"));
            mdbs[i].setMessagingType(LoaderUtil.getChildContent(root, "messaging-type"));
            mdbs[i].setTransactionType(LoaderUtil.getChildContent(root, "transaction-type"));
            mdbs[i].setActivationConfig(loadActivationConfig(root));
        }
        return mdbs;
    }

    private ActivationConfig loadActivationConfig(Element parent) {
        if(parent == null) {
            return null;
        }
        ActivationConfig config = new ActivationConfig();
        j2eeLoader.loadDescribable(parent, config);
        Element[] roots = LoaderUtil.getChildren(parent, "activation-config-property");
        ActivationConfigProperty[] props = new ActivationConfigProperty[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            props[i] = new ActivationConfigProperty();
            props[i].setActivationConfigPropertyName(LoaderUtil.getChildContent(root, "activation-config-property-name"));
            props[i].setActivationConfigPropertyValue(LoaderUtil.getChildContent(root, "activation-config-property-value"));
        }
        config.setActivationConfigProperty(props);
        return config;
    }

    private Session[] loadSessions(Element ebe) {
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

    private Entity[] loadEntities(Element ebe) {
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

    private Query[] loadQueries(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "query");
        Query[] queries = new Query[roots.length];
        for(int i = 0; i < roots.length; i++) {
            j2eeLoader.loadDescribable(roots[i], queries[i]);
            queries[i].setEjbQl(LoaderUtil.getChildContent(roots[i], "ejb-ql"));
            queries[i].setResultTypeMapping(LoaderUtil.getChildContent(roots[i], "result-type-mapping"));
            queries[i].setQueryMethod(loadQueryMethod(roots[i]));
        }
        return queries;
    }

    private QueryMethod loadQueryMethod(Element root) {
        if(root == null) {
            return null;
        }
        QueryMethod method = new QueryMethod();
        method.setMethodName(LoaderUtil.getChildContent(root, "method-name"));
        method.setMethodParam(LoaderUtil.getChildrenContent(LoaderUtil.getChild(root, "method-params"), "method-param"));
        return method;
    }

    private CmpField[] loadCmpFields(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "cmp-field");
        CmpField[] fields = new CmpField[roots.length];
        for(int i = 0; i < roots.length; i++) {
            fields[i] = new CmpField();
            j2eeLoader.loadDescribable(roots[i], fields[i]);
            fields[i].setFieldName(LoaderUtil.getChildContent(roots[i], "field-name"));
        }
        return fields;
    }

    private void loadRpcBean(Element root, RpcBean bean) {
        loadEjb(root, bean);
        bean.setHome(LoaderUtil.getChildContent(root, "home"));
        bean.setLocal(LoaderUtil.getChildContent(root, "local"));
        bean.setLocalHome(LoaderUtil.getChildContent(root, "local-home"));
        bean.setRemote(LoaderUtil.getChildContent(root, "remote"));
        bean.setSecurityRoleRef(j2eeLoader.loadSecurityRoleRefs(root));
    }

    private void loadEjb(Element root, Ejb bean) {
        j2eeLoader.loadDisplayable(root, bean);
        bean.setEjbName(LoaderUtil.getChildContent(root, "ejb-name"));
        bean.setEjbClass(LoaderUtil.getChildContent(root, "ejb-class"));
        bean.setSecurityIdentity(loadSecurityIdentity(LoaderUtil.getChild(root, "security-identity")));
        bean.setEjbRef(j2eeLoader.loadEJBRefs(root, new EJBRef[0]));
        bean.setEjbLocalRef(j2eeLoader.loadEJBLocalRefs(root, new EJBLocalRef[0]));
        bean.setResourceRef(j2eeLoader.loadResourceRefs(root, new ResourceRef[0]));
        bean.setResourceEnvRef(j2eeLoader.loadResourceEnvRefs(root, new ResourceEnvRef[0]));
        bean.setMessageDestinationRef(j2eeLoader.loadMessageDestinationRefs(root, new MessageDestinationRef[0]));
        bean.setEnvEntry(j2eeLoader.loadEnvEntries(root, new EnvEntry[0]));
        bean.setServiceRef(j2eeLoader.loadServiceRefs(root, new ServiceRef[0]));
    }

    private SecurityIdentity loadSecurityIdentity(Element root) {
        if(root == null) {
            return null;
        }
        SecurityIdentity id = new SecurityIdentity();
        j2eeLoader.loadDescribable(root, id);
        if(LoaderUtil.getChild(root, "use-caller-identity") != null) {
            id.setUseCallerIdentity(true);
        } else {
            id.setUseCallerIdentity(false);
            id.setRunAs(j2eeLoader.loadRunAs(LoaderUtil.getChild(root, "run-as")));
        }
        return id;
    }
}
