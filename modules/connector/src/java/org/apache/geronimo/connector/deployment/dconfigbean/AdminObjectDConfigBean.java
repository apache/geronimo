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

package org.apache.geronimo.connector.deployment.dconfigbean;

import javax.enterprise.deploy.model.DDBean;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/18 20:57:07 $
 *
 * */
public class AdminObjectDConfigBean extends DConfigBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private AdminObjectInstance[] instances = new AdminObjectInstance[0];

    public AdminObjectDConfigBean(DDBean ddBean, GerAdminobjectType adminObject) {
        super(ddBean, adminObject, SCHEMA_TYPE_LOADER);
        String adminObjectInterface = ddBean.getText("adminobject-interface")[0];
        if (adminObject.getAdminobjectInterface() == null) {
            adminObject.addNewAdminobjectInterface().setStringValue(adminObjectInterface);
        } else {
            assert adminObjectInterface.equals(adminObject.getAdminobjectInterface().getStringValue());
        }
        String adminObjectClass = ddBean.getText("adminobject-class")[0];
        if (adminObject.getAdminobjectClass() == null) {
            adminObject.addNewAdminobjectClass().setStringValue(adminObjectClass);
        } else {
            assert adminObjectClass.equals(adminObject.getAdminobjectClass().getStringValue());
        }
        // Get initial list of instances
        GerAdminobjectInstanceType[] xmlInstances = getAdminObject().getAdminobjectInstanceArray();
        instances = new AdminObjectInstance[xmlInstances.length];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = new AdminObjectInstance();
            instances[i].initialize(xmlInstances[i], this);
        }
    }

    GerAdminobjectType getAdminObject() {
        return (GerAdminobjectType) getXmlObject();
    }

    public AdminObjectInstance[] getAdminObjectInstance() {
        return instances;
    }

    public void setAdminObjectInstance(AdminObjectInstance[] instances) {
        AdminObjectInstance[] old = getAdminObjectInstance();
        this.instances = instances;
        for (int i = 0; i < instances.length; i++) { // catch additions
            AdminObjectInstance instance = instances[i];
            if (!instance.hasParent()) {
                GerAdminobjectInstanceType xmlObject = getAdminObject().addNewAdminobjectInstance();
                instance.initialize(xmlObject, this);
            }
        }
        for (int i = 0; i < old.length; i++) { // catch removals
            AdminObjectInstance instance = old[i];
            boolean found = false;
            for (int j = 0; j < instances.length; j++) {
                if (instances[j] == instance) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // remove the XmlBean
                for (int j = 0; j < getAdminObject().getAdminobjectInstanceArray().length; j++) {
                    GerAdminobjectInstanceType test = getAdminObject().getAdminobjectInstanceArray(j);
                    if (test == instance.getAdminobjectInstance()) {
                        getAdminObject().removeAdminobjectInstance(j);
                        break;
                    }
                }
                // clean up the removed JavaBean
                instance.dispose();
            }
        }
        pcs.firePropertyChange("connectionDefinitionInstance", old, instances);
    }

}
