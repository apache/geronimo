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
package org.apache.geronimo.enterprise.deploy.provider.jar;

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
import org.apache.geronimo.deployment.model.geronimo.ejb.Ejb;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Entity;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JndiContextParam;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EJBLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.SecurityRoleRef;

/**
 * Maps DConfigBeans to POJOs and vice versa.
 *
 * @version $Revision: 1.4 $ $Date: 2003/09/05 20:18:03 $
 */
public class EjbConverter {
    private static final Log log = LogFactory.getLog(EjbConverter.class);

    public static EjbJarRoot loadDConfigBeans(EjbJar custom, DDBeanRoot standard) throws ConfigurationException {
        EjbJarRoot root = new EjbJarRoot(standard);
        EjbJarBean ejbJar = (EjbJarBean)root.getDConfigBean(standard.getChildBean(EjbJarRoot.EJB_JAR_XPATH)[0]);
        EnterpriseBeansBean beans = (EnterpriseBeansBean)ejbJar.getDConfigBean(ejbJar.getDDBean().getChildBean(EjbJarBean.ENTERPRISE_BEANS_XPATH)[0]);
        assignSession(beans, custom.getEnterpriseBeans().getSession(), beans.getDDBean().getChildBean(EnterpriseBeansBean.SESSION_XPATH));
        assignEntity(beans, custom.getEnterpriseBeans().getEntity(), beans.getDDBean().getChildBean(EnterpriseBeansBean.ENTITY_XPATH));
        assignMessageDriven(beans, custom.getEnterpriseBeans().getMessageDriven(), beans.getDDBean().getChildBean(EnterpriseBeansBean.MESSAGE_DRIVEN_XPATH));
        return root;
    }

    public static org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar storeDConfigBeans(EjbJarRoot root) throws ConfigurationException {
        EjbJar jar = new EjbJar();
        jar.setEnterpriseBeans(new EnterpriseBeans());
        if(root == null || root.getEjbJar() == null) {
            throw new ConfigurationException("Insufficient configuration information to save.");
        }
        EnterpriseBeansBean beans = root.getEjbJar().getEnterpriseBeans();
        if(beans == null) {
            throw new ConfigurationException("Insufficient configuration information to save.");
        }
        storeSession(jar.getEnterpriseBeans(), beans.getSession().iterator());
        storeEntity(jar.getEnterpriseBeans(), beans.getEntity().iterator());
        storeMessageDriven(jar.getEnterpriseBeans(), beans.getMessageDriven().iterator());
        return jar;
    }

    private static void storeEjb(Ejb dest, BaseEjbBean bean) {
        dest.setEjbName(bean.getEjbName());
        storeEnvEntries(dest, bean.getEnvEntry().iterator());
        storeEjbRefs(dest, bean.getEjbRef().iterator());
        storeEjbLocalRefs(dest, bean.getEjbLocalRef().iterator());
        storeResourceRefs(dest, bean.getResourceRef().iterator());
        storeResourceEnvRefs(dest, bean.getResourceEnvRef().iterator());
    }

    private static void storeMessageDriven(EnterpriseBeans beans, Iterator iterator) {
        List list = new ArrayList();
        while(iterator.hasNext()) {
            MessageDrivenBean bean = (MessageDrivenBean)iterator.next();
            MessageDriven md = new MessageDriven();
            storeEjb(md, bean);
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
            String standard = bean.getDDBean().getText(EnvEntryBean.ENV_ENTRY_VALUE_XPATH)[0];
            if(isValid(bean.getEnvEntryValue()) && (standard == null || !standard.equals(bean.getEnvEntryValue()))) {
                EnvEntry e = new EnvEntry();
                e.setEnvEntryName(bean.getEnvEntryName());
                e.setEnvEntryValue(bean.getEnvEntryValue());
                list.add(e);
            }
        }
        dest.setEnvEntry((EnvEntry[])list.toArray(new EnvEntry[list.size()]));
    }

    private static void storeEjbRefs(Ejb dest, Iterator it) {
        List outer = new ArrayList();
        while(it.hasNext()) {
            EjbRefBean bean = (EjbRefBean)it.next();
            if(isValid(bean.getJndiName())) {
                EJBRef ref = new EJBRef();
                ref.setEJBRefName(bean.getEjbRefName());
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
        }
        dest.setEjbRef((EJBRef[])outer.toArray(new EJBRef[outer.size()]));
    }

    private static void storeEjbLocalRefs(Ejb dest, Iterator it) {
        List outer = new ArrayList();
        while(it.hasNext()) {
            EjbLocalRefBean bean = (EjbLocalRefBean)it.next();
            if(isValid(bean.getJndiName())) {
                EJBLocalRef ref = new EJBLocalRef();
                ref.setEJBRefName(bean.getEjbRefName());
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
        }
        dest.setEjbLocalRef((EJBLocalRef[])outer.toArray(new EJBLocalRef[outer.size()]));
    }

    private static void storeResourceRefs(Ejb dest, Iterator it) {
        List outer = new ArrayList();
        while(it.hasNext()) {
            ResourceRefBean bean = (ResourceRefBean)it.next();
            if(isValid(bean.getJndiName())) {
                ResourceRef ref = new ResourceRef();
                ref.setResRefName(bean.getResRefName());
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
        }
        dest.setResourceRef((ResourceRef[])outer.toArray(new ResourceRef[outer.size()]));
    }

    private static void storeResourceEnvRefs(Ejb dest, Iterator it) {
        List list = new ArrayList();
        while(it.hasNext()) {
            ResourceEnvRefBean bean = (ResourceEnvRefBean)it.next();
            if(isValid(bean.getJndiName())) {
                ResourceEnvRef ref = new ResourceEnvRef();
                ref.setResourceEnvRefName(bean.getResourceEnvRefName());
                ref.setJndiName(bean.getJndiName());
                list.add(ref);
            }
        }
        dest.setResourceEnvRef((ResourceEnvRef[])list.toArray(new ResourceEnvRef[list.size()]));
    }

    private static void storeSecurityRoleRefs(Session dest, Iterator it) {
        List list = new ArrayList();
        while(it.hasNext()) {
            SecurityRoleRefBean bean = (SecurityRoleRefBean)it.next();
            String standard = bean.getDDBean().getText(SecurityRoleRefBean.ROLE_LINK_XPATH)[0];
            if(isValid(bean.getRoleLink()) && (standard == null || !standard.equals(bean.getRoleLink()))) {
                SecurityRoleRef ref = new SecurityRoleRef();
                ref.setRoleName(bean.getRoleName());
                ref.setRoleLink(bean.getRoleLink());
                list.add(ref);
            }
        }
        dest.setSecurityRoleRef((SecurityRoleRef[])list.toArray(new SecurityRoleRef[list.size()]));
    }

    private static void storeSecurityRoleRefs(Entity dest, Iterator it) {
        List list = new ArrayList();
        while(it.hasNext()) {
            SecurityRoleRefBean bean = (SecurityRoleRefBean)it.next();
            String standard = bean.getDDBean().getText(SecurityRoleRefBean.ROLE_LINK_XPATH)[0];
            if(isValid(bean.getRoleLink()) && (standard == null || !standard.equals(bean.getRoleLink()))) {
                SecurityRoleRef ref = new SecurityRoleRef();
                ref.setRoleName(bean.getRoleName());
                ref.setRoleLink(bean.getRoleLink());
                list.add(ref);
            }
        }
        dest.setSecurityRoleRef((SecurityRoleRef[])list.toArray(new SecurityRoleRef[list.size()]));
    }

    private static void assignMessageDriven(EnterpriseBeansBean root, MessageDriven[] messageDriven, DDBean[] childBean) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<messageDriven.length; i++) {
            DDBean match = null;
            for(int j = 0; j < childBean.length; j++) {
                String[] names = childBean[j].getText(BaseEjbBean.EJB_NAME_XPATH);
                if(names.length == 1 && names[0].equals(messageDriven[i].getEjbName())) {
                    match = childBean[j];
                    break;
                }
            }
            if(match == null) {
                log.warn("Message-Driven EJB "+messageDriven[i].getEjbName()+" in old DD is no longer present; removing.");
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
                if(names.length == 1 && names[0].equals(sessions[i].getEjbName())) {
                    match = childBean[j];
                    break;
                }
            }
            if(match == null) {
                log.warn("Session EJB "+sessions[i].getEjbName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            SessionBean bean = (SessionBean)root.getDConfigBean(match);
            assignEjb(bean, sessions[i], match);
            //session-specific content
            bean.setJndiName(bean.getJndiName());
            assignSecurityRoleRefs(bean, sessions[i].getSecurityRoleRef(), match.getChildBean(EntityBean.SECURITY_ROLE_REF_XPATH));
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
                if(names.length == 1 && names[0].equals(entities[i].getEjbName())) {
                    match = childBean[j];
                    break;
                }
            }
            if(match == null) {
                log.warn("Entity EJB "+entities[i].getEjbName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            EntityBean bean = (EntityBean)root.getDConfigBean(match);
            assignEjb(bean, entities[i], match);
            //entity-specific content
            bean.setJndiName(bean.getJndiName());
            assignSecurityRoleRefs(bean, entities[i].getSecurityRoleRef(), match.getChildBean(EntityBean.SECURITY_ROLE_REF_XPATH));
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
        dest.setEjbName(bean.getEjbName());
        assignEnvEntries(dest, bean.getEnvEntry(), standard.getChildBean(BaseEjbBean.ENV_ENTRY_XPATH));
        assignEjbRefs(dest, bean.getEjbRef(), standard.getChildBean(BaseEjbBean.EJB_REF_XPATH));
        assignEjbLocalRefs(dest, bean.getEjbLocalRef(), standard.getChildBean(BaseEjbBean.EJB_LOCAL_REF_XPATH));
        assignResourceRefs(dest, bean.getResourceRef(), standard.getChildBean(BaseEjbBean.RESOURCE_REF_XPATH));
        assignResourceEnvRefs(dest, bean.getResourceEnvRef(), standard.getChildBean(BaseEjbBean.RESOURCE_ENV_REF_XPATH));
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
            log.debug("Set env entry value for "+bean.hashCode()+" to "+bean.getEnvEntryValue()+" for "+match.hashCode());
            log.debug("Try reload: "+dest.getDConfigBean(match).hashCode()+" value is "+((EnvEntryBean)dest.getDConfigBean(match)).getEnvEntryValue());
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

    private static void assignEjbRefs(BaseEjbBean dest, EJBRef[] refs, DDBean[] beans) throws ConfigurationException {
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

    private static void assignEjbLocalRefs(BaseEjbBean dest, EJBLocalRef[] refs, DDBean[] beans) throws ConfigurationException {
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
