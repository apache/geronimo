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
package org.apache.geronimo.enterprise.deploy.server.j2ee;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.model.j2ee.Describable;
import org.apache.geronimo.deployment.model.j2ee.Description;
import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.DisplayName;
import org.apache.geronimo.deployment.model.j2ee.Icon;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JndiContextParam;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDIEnvironmentRefs;
import org.apache.geronimo.deployment.model.geronimo.j2ee.MessageDestinationRef;

/**
 * Utilities for switching from POJOs to DConfigBeans and vice versa
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/07 17:16:36 $
 */
public class J2EEConverter {
    private static final Log log = LogFactory.getLog(J2EEConverter.class);
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

    /**
     * Saves JNDI references from DConfigBeans to POJOs.
     */
    public static void storeJndiEnvironment(JNDIEnvironmentRefs dest, JNDIRefs comp) {
        storeEnvEntries(dest, comp.getEnvEntry().iterator());
        storeEjbRefs(dest, comp.getEjbRef().iterator());
        storeEjbLocalRefs(dest, comp.getEjbLocalRef().iterator());
        storeResourceRefs(dest, comp.getResourceRef().iterator());
        storeResourceEnvRefs(dest, comp.getResourceEnvRef().iterator());
        //todo: message-destination-refs, service-refs
    }

    private static void storeEnvEntries(JNDIEnvironmentRefs dest, Iterator it) {
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

    private static void storeEjbRefs(JNDIEnvironmentRefs dest, Iterator it) {
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

    private static void storeEjbLocalRefs(JNDIEnvironmentRefs dest, Iterator it) {
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

    private static void storeResourceRefs(JNDIEnvironmentRefs dest, Iterator it) {
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

    private static void storeResourceEnvRefs(JNDIEnvironmentRefs dest, Iterator it) {
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

    /**
     * Assigns JNDI references from POJOs and J2EE DD to DConfigBeans.
     */
    public static void assignEnvironmentRefs(DConfigBean dest, JNDIEnvironmentRefs comp, DDBean standard) throws ConfigurationException {
        assignEnvEntries(dest, comp.getEnvEntry(), standard.getChildBean(JNDIRefs.ENV_ENTRY_XPATH));
        assignEjbRefs(dest, (EjbRef[])comp.getEJBRef(), standard.getChildBean(JNDIRefs.EJB_REF_XPATH));
        assignEjbLocalRefs(dest, (EjbLocalRef[])comp.getEJBLocalRef(), standard.getChildBean(JNDIRefs.EJB_LOCAL_REF_XPATH));
        assignResourceRefs(dest, (ResourceRef[])comp.getResourceRef(), standard.getChildBean(JNDIRefs.RESOURCE_REF_XPATH));
        assignResourceEnvRefs(dest, (ResourceEnvRef[])comp.getResourceEnvRef(), standard.getChildBean(JNDIRefs.RESOURCE_ENV_REF_XPATH));
        assignMessageDestinationRefs(dest, (MessageDestinationRef[])comp.getMessageDestinationRef(), standard.getChildBean(JNDIRefs.MESSAGE_DESTINATION_REF_XPATH));
        //todo: service refs
    }

    private static void assignEnvEntries(DConfigBean dest, EnvEntry[] entries, DDBean[] beans) throws ConfigurationException {
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

    private static void assignEjbRefs(DConfigBean dest, EjbRef[] refs, DDBean[] beans) throws ConfigurationException {
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

    private static void assignEjbLocalRefs(DConfigBean dest, EjbLocalRef[] refs, DDBean[] beans) throws ConfigurationException {
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

    private static void assignResourceEnvRefs(DConfigBean dest, ResourceEnvRef[] refs, DDBean[] beans) throws ConfigurationException {
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

    private static void assignResourceRefs(DConfigBean dest, ResourceRef[] refs, DDBean[] beans) throws ConfigurationException {
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

    private static void assignMessageDestinationRefs(DConfigBean dest, MessageDestinationRef[] refs, DDBean[] beans) throws ConfigurationException {
        Set found = new HashSet();
        for(int i=0; i<refs.length; i++) {
            DDBean match = null;
            for(int j = 0; j < beans.length; j++) {
                if(beans[j].getText(MessageDestinationRefBean.MESSAGE_DESTINATION_REF_NAME_XPATH)[0].equals(refs[i].getMessageDestinationRefName())) {
                    match = beans[j];
                }
            }
            if(match == null) {
                log.warn("Message Destination Reference "+refs[i].getMessageDestinationRefName()+" in old DD is no longer present; removing.");
                continue;
            }
            found.add(match);
            MessageDestinationRefBean bean = (MessageDestinationRefBean)dest.getDConfigBean(match);
            bean.setMessageDestinationRefName(refs[i].getMessageDestinationRefName());
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
            log.info("Old DD does not contain an entry for Message Destination Reference "+bean.getText(MessageDestinationRefBean.MESSAGE_DESTINATION_REF_NAME_XPATH)[0]+"; adding a default entry");
            dest.getDConfigBean(bean);
        }
    }

    public static String getText(String[] text) {
        if(text == null || text.length == 0) {
            return null;
        } else if(text.length == 1) {
            return text[0];
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static boolean isValid(String s) {
        return s != null && !s.equals("");
    }
}
