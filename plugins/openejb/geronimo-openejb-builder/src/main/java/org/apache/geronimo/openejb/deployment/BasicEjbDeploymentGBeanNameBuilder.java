/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.openejb.deployment;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SingletonBean;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class BasicEjbDeploymentGBeanNameBuilder implements EjbDeploymentGBeanNameBuilder {

    public AbstractName createEjbName(EARContext earContext, EjbModule ejbModule, EnterpriseBean enterpriseBean) {
        String ejbName = enterpriseBean.getEjbName();
        String type = null;
        if (enterpriseBean instanceof SessionBean) {
            SessionBean sessionBean = (SessionBean) enterpriseBean;
            switch (sessionBean.getSessionType()) {
                case STATELESS:
                    type = NameFactory.STATELESS_SESSION_BEAN;
                    break;
                case STATEFUL:
                    type = NameFactory.STATEFUL_SESSION_BEAN;
                    break;
                case SINGLETON:
                    type = NameFactory.SINGLETON_BEAN;
                    break;
                case MANAGED:
                    type = NameFactory.MANAGED_BEAN;
                    break;
            }
        } else if (enterpriseBean instanceof EntityBean) {
            type = NameFactory.ENTITY_BEAN;
        } else if (enterpriseBean instanceof MessageDrivenBean) {
            type = NameFactory.MESSAGE_DRIVEN_BEAN;
        }
        if (type == null) {
            throw new IllegalArgumentException("Unknown enterprise bean type XXX " + enterpriseBean.getClass().getName());
        }
        return earContext.getNaming().createChildName(ejbModule.getModuleName(), ejbName, type);
    }
    
}
