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
package org.apache.geronimo.enterprise.deploy.server.ejb;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.deployment.model.ejb.SecurityIdentity;
import org.apache.geronimo.deployment.model.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.ejb.ActivationConfigProperty;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.CmpField;
import org.apache.geronimo.deployment.model.ejb.Query;
import org.apache.geronimo.deployment.model.ejb.QueryMethod;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Entity;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JndiContextParam;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.SecurityRoleRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.RunAs;
import org.apache.geronimo.deployment.model.j2ee.Describable;
import org.apache.geronimo.deployment.model.j2ee.Description;
import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.DisplayName;
import org.apache.geronimo.deployment.model.j2ee.Icon;
import org.apache.geronimo.enterprise.deploy.server.j2ee.ContextParam;
import org.apache.geronimo.enterprise.deploy.server.j2ee.EnvEntryBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.EjbRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.EjbLocalRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.ResourceEnvRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.ResourceRefBean;
import org.apache.geronimo.enterprise.deploy.server.DConfigBeanLookup;

/**
 * Maps DConfigBeans to POJOs and vice versa.
 *
 * When converting POJOs to DConfigBeans, we ignore everything except the
 * Geronimo-specific content.  That way, we don't have to listen on changes
 * on every single element in the whole standard DD.
 *
 * When converting DConfigBeans to POJOs, we use the matching DDBeans to
 * look up all the info that isn't covered in the Geronimo DD for each
 * DConfigBean.  Note this means that the standard DD content may be out of
 * sync when loaded, but they'll be cleaned up when the DD is saved.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/06 14:35:34 $
 */
public class EjbConverter {
    private static final Log log = LogFactory.getLog(EjbConverter.class);

    public static EjbJarRoot loadDConfigBeans(EjbJar custom, DDBeanRoot standard, DConfigBeanLookup lookup) throws ConfigurationException {
        EjbJarRoot root = new EjbJarRoot(standard, lookup);
        EjbJarBean ejbJar = (EjbJarBean)root.getDConfigBean(standard.getChildBean(EjbJarRoot.EJB_JAR_XPATH)[0]);
        EnterpriseBeansBean beans = (EnterpriseBeansBean)ejbJar.getDConfigBean(ejbJar.getDDBean().getChildBean(EjbJarBean.ENTERPRISE_BEANS_XPATH)[0]);
        assignSession(beans, custom.getGeronimoEnterpriseBeans().getGeronimoSession(), beans.getDDBean().getChildBean(EnterpriseBeansBean.SESSION_XPATH));
        assignEntity(beans, custom.getGeronimoEnterpriseBeans().getGeronimoEntity(), beans.getDDBean().getChildBean(EnterpriseBeansBean.ENTITY_XPATH));
        assignMessageDriven(beans, custom.getGeronimoEnterpriseBeans().getGeronimoMessageDriven(), beans.getDDBean().getChildBean(EnterpriseBeansBean.MESSAGE_DRIVEN_XPATH));
        return root;
    }

    public static EjbJar storeDConfigBeans(EjbJarRoot root) throws ConfigurationException {
        if(root == null || root.getEjbJar() == null) {
            throw new ConfigurationException("Insufficient configuration information to save.");
        }
        EjbJar jar = new EjbJar();
        jar.setVersion(root.getEjbJar().getDDBean().getAttributeValue("version"));
        loadDescribable(root.getEjbJar().getDDBean(), jar);
        jar.setEnterpriseBeans(new EnterpriseBeans());
        EnterpriseBeansBean beans = root.getEjbJar().getEnterpriseBeans();
        if(beans == null) {
            throw new ConfigurationException("Insufficient configuration information to save.");
        }
        storeSession(jar.getGeronimoEnterpriseBeans(), beans.getSession().iterator());
        storeEntity(jar.getGeronimoEnterpriseBeans(), beans.getEntity().iterator());
        storeMessageDriven(jar.getGeronimoEnterpriseBeans(), beans.getMessageDriven().iterator());
        return jar;
    }

    private static void storeEjb(Ejb dest, BaseEjbBean bean) {
        loadDisplayable(bean.getDDBean(), dest);
        dest.setEJBName(bean.getEjbName());
        dest.setEJBClass(getText(bean.getDDBean().getText("ejb-class")));
        DDBean list[] = bean.getDDBean().getChildBean("security-identity");
        if(list.length == 1) {
            DDBean id = list[0];
            SecurityIdentity sid = new SecurityIdentity();
            loadDescribable(id, sid);
            list = id.getChildBean("use-caller-identity");
            if(list != null && list.length == 1) {
                sid.setUseCallerIdentity(true);
            } else {
                sid.setUseCallerIdentity(false);
                list = id.getChildBean("run-as");
                if(list != null && list.length == 1) {
                    RunAs as = new RunAs();
                    loadDescribable(list[0], as);
                    as.setRoleName(getText(list[0].getText("role-name")));
                    sid.setRunAs(as);
                }
            }
            dest.setSecurityIdentity(sid);
        }
        storeEnvEntries(dest, bean.getEnvEntry().iterator());
        storeEjbRefs(dest, bean.getEjbRef().iterator());
        storeEjbLocalRefs(dest, bean.getEjbLocalRef().iterator());
        storeResourceRefs(dest, bean.getResourceRef().iterator());
        storeResourceEnvRefs(dest, bean.getResourceEnvRef().iterator());
        //todo: message-destination-refs, service-refs
    }

    public static void loadDescribable(DDBean base, Describable desc) {
        DDBean[] ds = base.getChildBean("description");
        Description[] out = new Description[ds.length];
        for(int i=0; i<ds.length; i++) {
            Description d = new Description();
            d.setLang(ds[i].getAttributeValue("lang"));
            d.setContent(ds[i].getText());
            out[i] = d;
        }
        desc.setDescription(out);
    }

    public static void loadDisplayable(DDBean base, Displayable disp) {
        loadDescribable(base, disp);
        DDBean[] ds = base.getChildBean("display-name");
        DisplayName[] out = new DisplayName[ds.length];
        for(int i=0; i<ds.length; i++) {
            DisplayName d = new DisplayName();
            d.setLang(ds[i].getAttributeValue("lang"));
            d.setContent(ds[i].getText());
            out[i] = d;
        }
        disp.setDisplayName(out);
        ds = base.getChildBean("icon");
        Icon[] is = new Icon[ds.length];
        for(int i=0; i<ds.length; i++) {
            Icon ic = new Icon();
            ic.setLang(ds[i].getAttributeValue("lang"));
            ic.setLargeIcon(getText(ds[i].getText("large-icon")));
            ic.setSmallIcon(getText(ds[i].getText("small-icon")));
            is[i] = ic;
        }
        disp.setIcon(is);
    }

    private static String getText(String[] text) {
        if(text == null || text.length == 0) {
            return null;
        } else if(text.length == 1) {
            return text[0];
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static void storeMessageDriven(EnterpriseBeans beans, Iterator iterator) {
        List list = new ArrayList();
        while(iterator.hasNext()) {
            MessageDrivenBean bean = (MessageDrivenBean)iterator.next();
            MessageDriven md = new MessageDriven();
            storeEjb(md, bean);
            DDBean ddb = bean.getDDBean();
            md.setMessagingType(getText(ddb.getText("messaging-type")));
            md.setTransactionType(getText(ddb.getText("transaction-type")));
            md.setMessageDestinationType(getText(ddb.getText("message-destination-type")));
            md.setMessageDestinationLink(getText(ddb.getText("message-destination-link")));
            DDBean[] foo = ddb.getChildBean("activation-config");
            if(foo != null && foo.length == 1) {
                ActivationConfig config = new ActivationConfig();
                loadDescribable(foo[0], config);
                foo = foo[0].getChildBean("activation-config-property");
                ActivationConfigProperty[] props = new ActivationConfigProperty[foo.length];
                for(int i=0; i<foo.length; i++) {
                    ActivationConfigProperty prop = new ActivationConfigProperty();
                    prop.setActivationConfigPropertyName(getText(foo[i].getText("activation-config-property-name")));
                    prop.setActivationConfigPropertyValue(getText(foo[i].getText("activation-config-property-value")));
                    props[i] = prop;
                }
                config.setActivationConfigProperty(props);
                md.setActivationConfig(config);
            }
            list.add(md);
        }
        beans.setMessageDriven((MessageDriven[])list.toArray(new MessageDriven[list.size()]));
    }

    private static void storeEntity(EnterpriseBeans beans, Iterator iterator) {
        List list = new ArrayList();
        while(iterator.hasNext()) {
            EntityBean bean = (EntityBean)iterator.next();
            Entity e = new Entity();
            storeEjb(e, bean);
            DDBean ddb = bean.getDDBean();
            e.setHome(getText(ddb.getText("home")));
            e.setRemote(getText(ddb.getText("remote")));
            e.setLocalHome(getText(ddb.getText("local-home")));
            e.setLocal(getText(ddb.getText("local")));
            e.setPersistenceType(getText(ddb.getText("persistence-type")));
            e.setPrimKeyClass(getText(ddb.getText("prim-key-class")));
            e.setReentrant(getText(ddb.getText("reentrant")));
            e.setCmpVersion(getText(ddb.getText("cmp-version")));
            e.setAbstractSchemaName(getText(ddb.getText("abstract-schema-name")));
            e.setPrimkeyField(getText(ddb.getText("primkey-field")));
            DDBean[] foo = ddb.getChildBean("cmp-field");
            CmpField[] fields = new CmpField[foo.length];
            for(int i = 0; i < foo.length; i++) {
                CmpField field = new CmpField();
                loadDescribable(foo[i], field);
                field.setFieldName(getText(foo[i].getText("field-name")));
                fields[i] = field;
            }
            e.setCmpField(fields);
            foo = ddb.getChildBean("query");
            Query[] queries = new Query[foo.length];
            for(int i = 0; i < foo.length; i++) {
                Query query = new Query();
                loadDescribable(foo[i], query);
                query.setEjbQl(getText(foo[i].getText("ejb-ql")));
                query.setResultTypeMapping(getText(foo[i].getText("result-type-mapping")));
                DDBean[] bar = foo[i].getChildBean("query-method");
                if(bar.length == 1) {
                    QueryMethod method = new QueryMethod();
                    method.setMethodName(getText(bar[0].getText("method-name")));
                    bar = bar[0].getChildBean("method-params/method-param");
                    String[] params = new String[bar.length];
                    for(int j = 0; j < bar.length; j++) {
                        params[j] = bar[j].getText();
                    }
                    method.setMethodParam(params);
                    query.setQueryMethod(method);
                }
                queries[i] = query;
            }
            e.setQuery(queries);
            e.setJndiName(bean.getJndiName());
            storeSecurityRoleRefs(e, bean.getSecurityRoleRef().iterator());
            list.add(e);
        }
        beans.setEntity((Entity[])list.toArray(new Entity[list.size()]));
    }

    private static void storeSession(EnterpriseBeans beans, Iterator iterator) {
        List list = new ArrayList();
        while(iterator.hasNext()) {
            SessionBean bean = (SessionBean)iterator.next();
            Session s = new Session();
            storeEjb(s, bean);
            DDBean ddb = bean.getDDBean();
            s.setHome(getText(ddb.getText("home")));
            s.setRemote(getText(ddb.getText("remote")));
            s.setLocalHome(getText(ddb.getText("local-home")));
            s.setLocal(getText(ddb.getText("local")));
            s.setServiceEndpoint(getText(ddb.getText("service-endpoint")));
            s.setSessionType(getText(ddb.getText("session-type")));
            s.setTransactionType(getText(ddb.getText("transaction-type")));
            s.setJndiName(bean.getJndiName());
            storeSecurityRoleRefs(s, bean.getSecurityRoleRef().iterator());
            list.add(s);
        }
        beans.setSession((Session[])list.toArray(new Session[list.size()]));
    }

    private static void storeEnvEntries(Ejb dest, Iterator it) {
        List list = new ArrayList();
        while(it.hasNext()) {
            EnvEntryBean bean = (EnvEntryBean)it.next();
            DDBean ddb = bean.getDDBean();
            String standard = getText(ddb.getText(EnvEntryBean.ENV_ENTRY_VALUE_XPATH));
            EnvEntry e = new EnvEntry();
            loadDescribable(ddb, e);
            e.setEnvEntryName(bean.getEnvEntryName());
            e.setEnvEntryType(getText(ddb.getText("env-entry-type")));
            e.setEnvEntryValue(bean.getEnvEntryValue() == null ? standard : bean.getEnvEntryValue());
            list.add(e);
        }
        dest.setEnvEntry((EnvEntry[])list.toArray(new EnvEntry[list.size()]));
    }

    private static void storeEjbRefs(Ejb dest, Iterator it) {
        List outer = new ArrayList();
        while(it.hasNext()) {
            EjbRefBean bean = (EjbRefBean)it.next();
            EjbRef ref = new EjbRef();
            DDBean ddb = bean.getDDBean();
            loadDescribable(ddb, ref);
            ref.setEJBRefName(bean.getEjbRefName());
            ref.setEJBRefType(getText(ddb.getText("ejb-ref-type")));
            ref.setHome(getText(ddb.getText("home")));
            ref.setRemote(getText(ddb.getText("remote")));
            ref.setEJBLink(getText(ddb.getText("ejb-link")));
            if(ref.getEJBLink() == null) {
                ref.setJndiName(bean.getJndiName());
                ContextParam[] params = bean.getContextParam();
                List list = new ArrayList();
                for(int i=0; i<params.length; i++) {
                    if(isValid(params[i].getParamName()) && isValid(params[i].getParamValue())) {
                        JndiContextParam jcp = new JndiContextParam();
                        jcp.setParamName(params[i].getParamName());
                        jcp.setParamValue(params[i].getParamValue());
                        list.add(jcp);
                    }
                }
                if(list.size() > 0) {
                    ref.setJndiContextParam((JndiContextParam[])list.toArray(new JndiContextParam[list.size()]));
                }
            }
            outer.add(ref);
        }
        dest.setEJBRef((EjbRef[])outer.toArray(new EjbRef[outer.size()]));
    }

    private static void storeEjbLocalRefs(Ejb dest, Iterator it) {
        List outer = new ArrayList();
        while(it.hasNext()) {
            EjbLocalRefBean bean = (EjbLocalRefBean)it.next();
            EjbLocalRef ref = new EjbLocalRef();
            DDBean ddb = bean.getDDBean();
            loadDescribable(ddb, ref);
            ref.setEJBRefName(bean.getEjbRefName());
            ref.setEJBRefType(getText(ddb.getText("ejb-ref-type")));
            ref.setLocalHome(getText(ddb.getText("local-home")));
            ref.setLocal(getText(ddb.getText("local")));
            ref.setEJBLink(getText(ddb.getText("ejb-link")));
            if(ref.getEJBLink() == null) {
                ref.setJndiName(bean.getJndiName());
                ContextParam[] params = bean.getContextParam();
                List list = new ArrayList();
                for(int i=0; i<params.length; i++) {
                    if(isValid(params[i].getParamName()) && isValid(params[i].getParamValue())) {
                        JndiContextParam jcp = new JndiContextParam();
                        jcp.setParamName(params[i].getParamName());
                        jcp.setParamValue(params[i].getParamValue());
                        list.add(jcp);
                    }
                }
                if(list.size() > 0) {
                    ref.setJndiContextParam((JndiContextParam[])list.toArray(new JndiContextParam[list.size()]));
                }
            }
            outer.add(ref);
        }
        dest.setEJBLocalRef((EjbLocalRef[])outer.toArray(new EjbLocalRef[outer.size()]));
    }

    private static void storeResourceRefs(Ejb dest, Iterator it) {
        List outer = new ArrayList();
        while(it.hasNext()) {
            ResourceRefBean bean = (ResourceRefBean)it.next();
            ResourceRef ref = new ResourceRef();
            DDBean ddb = bean.getDDBean();
            loadDescribable(ddb, ref);
            ref.setResRefName(bean.getResRefName());
            ref.setResType(getText(ddb.getText("res-type")));
            ref.setResAuth(getText(ddb.getText("res-auth")));
            ref.setResSharingScope(getText(ddb.getText("res-sharing-scope")));
            ref.setJndiName(bean.getJndiName());
            ContextParam[] params = bean.getContextParam();
            List list = new ArrayList();
            for(int i=0; i<params.length; i++) {
                if(isValid(params[i].getParamName()) && isValid(params[i].getParamValue())) {
                    JndiContextParam jcp = new JndiContextParam();
                    jcp.setParamName(params[i].getParamName());
                    jcp.setParamValue(params[i].getParamValue());
                    list.add(jcp);
                }
            }
            if(list.size() > 0) {
                ref.setJndiContextParam((JndiContextParam[])list.toArray(new JndiContextParam[list.size()]));
            }
            outer.add(ref);
        }
        dest.setResourceRef((ResourceRef[])outer.toArray(new ResourceRef[outer.size()]));
    }

    private static void storeResourceEnvRefs(Ejb dest, Iterator it) {
        List list = new ArrayList();
        while(it.hasNext()) {
            ResourceEnvRefBean bean = (ResourceEnvRefBean)it.next();
            ResourceEnvRef ref = new ResourceEnvRef();
            DDBean ddb = bean.getDDBean();
            loadDescribable(ddb, ref);
            ref.setResourceEnvRefName(bean.getResourceEnvRefName());
            ref.setResourceEnvRefType(getText(ddb.getText("resource-env-ref-type")));
            ref.setJndiName(bean.getJndiName());
            list.add(ref);
        }
        dest.setResourceEnvRef((ResourceEnvRef[])list.toArray(new ResourceEnvRef[list.size()]));
    }

    private static void storeSecurityRoleRefs(RpcBean dest, Iterator it) {
        List list = new ArrayList();
        while(it.hasNext()) {
            SecurityRoleRefBean bean = (SecurityRoleRefBean)it.next();
            DDBean ddb = bean.getDDBean();
            String standard = getText(ddb.getText(SecurityRoleRefBean.ROLE_LINK_XPATH));
            SecurityRoleRef ref = new SecurityRoleRef();
            ref.setRoleName(bean.getRoleName());
            ref.setRoleLink(bean.getRoleLink() == null ? standard : bean.getRoleLink());
            list.add(ref);
        }
        dest.setSecurityRoleRef((SecurityRoleRef[])list.toArray(new SecurityRoleRef[list.size()]));
    }

    private static void assignMessageDriven(EnterpriseBeansBean root, MessageDriven[] messageDriven, DDBean[] childBean) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<messageDriven.length; i++) {
            DDBean match = null;
            for(int j = 0; j < childBean.length; j++) {
                String[] names = childBean[j].getText(BaseEjbBean.EJB_NAME_XPATH);
                if(names.length == 1 && names[0].equals(messageDriven[i].getEJBName())) {
                    match = childBean[j];
                    break;
                }
            }
            if(match == null) {
                log.warn("Message-Driven EJB "+messageDriven[i].getEJBName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            MessageDrivenBean bean = (MessageDrivenBean)root.getDConfigBean(match);
            assignEjb(bean, messageDriven[i], match);
            //todo: any message-driven-specific content
        }
        for(int i = 0; i < childBean.length; i++) {
            DDBean bean = childBean[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Message-Driven EJB "+bean.getText(BaseEjbBean.EJB_NAME_XPATH)[0]+"; adding a default entry");
            root.getDConfigBean(bean);
        }
    }

    private static void assignSession(EnterpriseBeansBean root, Session[] sessions, DDBean[] childBean) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<sessions.length; i++) {
            DDBean match = null;
            for(int j = 0; j < childBean.length; j++) {
                String[] names = childBean[j].getText(BaseEjbBean.EJB_NAME_XPATH);
                if(names.length == 1 && names[0].equals(sessions[i].getEJBName())) {
                    match = childBean[j];
                    break;
                }
            }
            if(match == null) {
                log.warn("Session EJB "+sessions[i].getEJBName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            SessionBean bean = (SessionBean)root.getDConfigBean(match);
            assignEjb(bean, sessions[i], match);
            //session-specific content
            bean.setJndiName(sessions[i].getJndiName());
            assignSecurityRoleRefs(bean, sessions[i].getGeronimoSecurityRoleRef(), match.getChildBean(EntityBean.SECURITY_ROLE_REF_XPATH));
        }
        for(int i = 0; i < childBean.length; i++) {
            DDBean bean = childBean[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Session EJB "+bean.getText(BaseEjbBean.EJB_NAME_XPATH)[0]+"; adding a default entry");
            root.getDConfigBean(bean);
        }
    }

    private static void assignEntity(EnterpriseBeansBean root, Entity[] entities, DDBean[] childBean) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<entities.length; i++) {
            DDBean match = null;
            for(int j = 0; j < childBean.length; j++) {
                String[] names = childBean[j].getText(BaseEjbBean.EJB_NAME_XPATH);
                if(names.length == 1 && names[0].equals(entities[i].getEJBName())) {
                    match = childBean[j];
                    break;
                }
            }
            if(match == null) {
                log.warn("Entity EJB "+entities[i].getEJBName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            EntityBean bean = (EntityBean)root.getDConfigBean(match);
            assignEjb(bean, entities[i], match);
            //entity-specific content
            bean.setJndiName(entities[i].getJndiName());
            assignSecurityRoleRefs(bean, entities[i].getGeronimoSecurityRoleRef(), match.getChildBean(EntityBean.SECURITY_ROLE_REF_XPATH));
        }
        for(int i = 0; i < childBean.length; i++) {
            DDBean bean = childBean[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Entity EJB "+bean.getText(BaseEjbBean.EJB_NAME_XPATH)[0]+"; adding a default entry");
            root.getDConfigBean(bean);
        }
    }

    private static void assignEjb(BaseEjbBean dest, Ejb bean, DDBean standard) throws ConfigurationException {
        dest.setEjbName(bean.getEJBName());
        assignEnvEntries(dest, bean.getEnvEntry(), standard.getChildBean(BaseEjbBean.ENV_ENTRY_XPATH));
        assignEjbRefs(dest, (EjbRef[])bean.getEJBRef(), standard.getChildBean(BaseEjbBean.EJB_REF_XPATH));
        assignEjbLocalRefs(dest, (EjbLocalRef[])bean.getEJBLocalRef(), standard.getChildBean(BaseEjbBean.EJB_LOCAL_REF_XPATH));
        assignResourceRefs(dest, (ResourceRef[])bean.getResourceRef(), standard.getChildBean(BaseEjbBean.RESOURCE_REF_XPATH));
        assignResourceEnvRefs(dest, (ResourceEnvRef[])bean.getResourceEnvRef(), standard.getChildBean(BaseEjbBean.RESOURCE_ENV_REF_XPATH));
        //todo: message destination refs, service refs
    }

    private static void assignEnvEntries(BaseEjbBean dest, EnvEntry[] entries, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<entries.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(EnvEntryBean.ENV_ENTRY_NAME_XPATH)[0].equals(entries[i].getEnvEntryName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("Env Entry "+entries[i].getEnvEntryName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            EnvEntryBean bean = (EnvEntryBean)dest.getDConfigBean(match);
            bean.setEnvEntryName(entries[i].getEnvEntryName());
            bean.setEnvEntryValue(entries[i].getEnvEntryValue());
        }
        for(int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Env Entry "+bean.getText(EnvEntryBean.ENV_ENTRY_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    private static void assignEjbRefs(BaseEjbBean dest, EjbRef[] refs, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<refs.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(EjbRefBean.EJB_REF_NAME_XPATH)[0].equals(refs[i].getEJBRefName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("EJB Reference "+refs[i].getEJBRefName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            EjbRefBean bean = (EjbRefBean)dest.getDConfigBean(match);
            bean.setEjbRefName(refs[i].getEJBRefName());
            bean.setJndiName(refs[i].getJndiName());
            JndiContextParam[] params = refs[i].getJndiContextParam();
            ContextParam[] cp = new ContextParam[params.length];
            for(int j=0; j<params.length; j++) {
                cp[j] = new ContextParam();
                cp[j].setParamName(params[j].getParamName());
                cp[j].setParamValue(params[j].getParamValue());
            }
            bean.setContextParam(cp);
        }
        for(int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for EJB Reference "+bean.getText(EjbRefBean.EJB_REF_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    private static void assignEjbLocalRefs(BaseEjbBean dest, EjbLocalRef[] refs, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<refs.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(EjbLocalRefBean.EJB_REF_NAME_XPATH)[0].equals(refs[i].getEJBRefName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("EJB Reference "+refs[i].getEJBRefName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            EjbLocalRefBean bean = (EjbLocalRefBean)dest.getDConfigBean(match);
            bean.setEjbRefName(refs[i].getEJBRefName());
            bean.setJndiName(refs[i].getJndiName());
            JndiContextParam[] params = refs[i].getJndiContextParam();
            ContextParam[] cp = new ContextParam[params.length];
            for(int j=0; j<params.length; j++) {
                cp[j] = new ContextParam();
                cp[j].setParamName(params[j].getParamName());
                cp[j].setParamValue(params[j].getParamValue());
            }
            bean.setContextParam(cp);
        }
        for(int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for EJB Reference "+bean.getText(EjbLocalRefBean.EJB_REF_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    private static void assignResourceEnvRefs(BaseEjbBean dest, ResourceEnvRef[] refs, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<refs.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(ResourceEnvRefBean.RESOURCE_ENV_REF_NAME_XPATH)[0].equals(refs[i].getResourceEnvRefName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("Resource Env Reference "+refs[i].getResourceEnvRefName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            ResourceEnvRefBean bean = (ResourceEnvRefBean)dest.getDConfigBean(match);
            bean.setResourceEnvRefName(refs[i].getResourceEnvRefName());
            bean.setJndiName(refs[i].getJndiName());
        }
        for(int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Resource Env Reference "+bean.getText(ResourceEnvRefBean.RESOURCE_ENV_REF_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    private static void assignResourceRefs(BaseEjbBean dest, ResourceRef[] refs, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<refs.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(ResourceRefBean.RES_REF_NAME_XPATH)[0].equals(refs[i].getResRefName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("Resource Reference "+refs[i].getResRefName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            ResourceRefBean bean = (ResourceRefBean)dest.getDConfigBean(match);
            bean.setResRefName(refs[i].getResRefName());
            bean.setJndiName(refs[i].getJndiName());
            JndiContextParam[] params = refs[i].getJndiContextParam();
            ContextParam[] cp = new ContextParam[params.length];
            for(int j=0; j<params.length; j++) {
                cp[j] = new ContextParam();
                cp[j].setParamName(params[j].getParamName());
                cp[j].setParamValue(params[j].getParamValue());
            }
            bean.setContextParam(cp);
        }
        for(int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Resource Reference "+bean.getText(ResourceRefBean.RES_REF_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    private static void assignSecurityRoleRefs(BaseEjbBean dest, SecurityRoleRef[] refs, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<refs.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(SecurityRoleRefBean.ROLE_NAME_XPATH)[0].equals(refs[i].getRoleName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("Security Role Reference "+refs[i].getRoleName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            SecurityRoleRefBean bean = (SecurityRoleRefBean)dest.getDConfigBean(match);
            bean.setRoleName(refs[i].getRoleName());
            bean.setRoleLink(refs[i].getRoleLink());
        }
        for(int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(found.contains(bean)) {
                continue;
            }
            log.info("Old DD does not contain an entry for Security Role Reference "+bean.getText(SecurityRoleRefBean.ROLE_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    private static boolean isValid(String s) {
        return s != null && !s.equals("");
    }
}
