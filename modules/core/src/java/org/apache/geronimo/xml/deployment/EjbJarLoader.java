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

/**
 * 
 *
 * @version $Revision: 1.1 $
 */
public class EjbJarLoader {
    public static EjbJarDocument load(Document doc) {
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
        EjbJarDocument result = new EjbJarDocument();
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
            mdbs[i].setMessageDestinationLink(LoaderUtil.getChildContent(root, "message-destination-link"));
            mdbs[i].setMessageDestinationType(LoaderUtil.getChildContent(root, "message-destination-type"));
            mdbs[i].setMessagingType(LoaderUtil.getChildContent(root, "messaging-type"));
            mdbs[i].setTransactionType(LoaderUtil.getChildContent(root, "transaction-type"));
            mdbs[i].setActivationConfig(loadActivationConfig(root));
        }
        return mdbs;
    }

    private static ActivationConfig loadActivationConfig(Element parent) {
        if(parent == null) {
            return null;
        }
        ActivationConfig config = new ActivationConfig();
        J2EELoader.loadDescribable(parent, config);
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

    private static Query[] loadQueries(Element parent) {
        Element[] roots = LoaderUtil.getChildren(parent, "query");
        Query[] queries = new Query[roots.length];
        for(int i = 0; i < roots.length; i++) {
            J2EELoader.loadDescribable(roots[i], queries[i]);
            queries[i].setEjbQl(LoaderUtil.getChildContent(roots[i], "ejb-ql"));
            queries[i].setResultTypeMapping(LoaderUtil.getChildContent(roots[i], "result-type-mapping"));
            queries[i].setQueryMethod(loadQueryMethod(roots[i]));
        }
        return queries;
    }

    private static QueryMethod loadQueryMethod(Element root) {
        if(root == null) {
            return null;
        }
        QueryMethod method = new QueryMethod();
        method.setMethodName(LoaderUtil.getChildContent(root, "method-name"));
        method.setMethodParam(LoaderUtil.getChildrenContent(LoaderUtil.getChild(root, "method-params"), "method-param"));
        return method;
    }

    private static CmpField[] loadCmpFields(Element parent) {
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
        bean.setHome(LoaderUtil.getChildContent(root, "home"));
        bean.setLocal(LoaderUtil.getChildContent(root, "local"));
        bean.setLocalHome(LoaderUtil.getChildContent(root, "local-home"));
        bean.setRemote(LoaderUtil.getChildContent(root, "remote"));
        bean.setSecurityRoleRef(J2EELoader.loadSecurityRoleRefs(root));
    }

    private static void loadEjb(Element root, Ejb bean) {
        J2EELoader.loadDisplayable(root, bean);
        bean.setEjbName(LoaderUtil.getChildContent(root, "ejb-name"));
        bean.setEjbClass(LoaderUtil.getChildContent(root, "ejb-class"));
        bean.setSecurityIdentity(loadSecurityIdentity(LoaderUtil.getChild(root, "security-identity")));
        bean.setEjbRef(J2EELoader.loadEJBRefs(root));
        bean.setEjbLocalRef(J2EELoader.loadEJBLocalRefs(root));
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
