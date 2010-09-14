/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.ContainerType;
import org.apache.openejb.BeanContext;
import org.apache.openejb.spi.ApplicationServer;

/**
 * @version $Revision: 494431 $ $Date: 2007-01-09 07:18:14 -0800 (Tue, 09 Jan 2007) $
 */
public class CorbaApplicationServer implements ApplicationServer {
    public EJBObject getEJBObject(ProxyInfo proxyInfo) {
        try {
            RefGenerator refGenerator = AdapterWrapper.getRefGenerator((String) proxyInfo.getBeanContext().getDeploymentID());
            org.omg.CORBA.Object object = refGenerator.genObjectReference(proxyInfo.getPrimaryKey());
            EJBObject ejbObject = (EJBObject) PortableRemoteObject.narrow(object, EJBObject.class);
            return ejbObject;
        } catch (Throwable e) {
            throw (org.omg.CORBA.MARSHAL)new org.omg.CORBA.MARSHAL(e.getClass().getName() + " thrown while marshaling the reply: " + e.getMessage(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }

    /**
     * DMB: My vague understanding is that business interfaces aren't supported by
     * corba as they do not extend java.rmi.Remote.  But we can try.
     */
    public Object getBusinessObject(ProxyInfo proxyInfo) {
        try {
            RefGenerator refGenerator = AdapterWrapper.getRefGenerator((String) proxyInfo.getBeanContext().getDeploymentID());
            org.omg.CORBA.Object object = refGenerator.genObjectReference(proxyInfo.getPrimaryKey());
            return object;
        } catch (Throwable e) {
            throw (org.omg.CORBA.MARSHAL)new org.omg.CORBA.MARSHAL(e.getClass().getName() + " thrown while marshaling the reply: " + e.getMessage(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }

    public EJBHome getEJBHome(ProxyInfo proxyInfo) {
        try {
            RefGenerator refGenerator = AdapterWrapper.getRefGenerator((String) proxyInfo.getBeanContext().getDeploymentID());
            org.omg.CORBA.Object home = refGenerator.genHomeReference();
            EJBHome ejbHome = (EJBHome) PortableRemoteObject.narrow(home, EJBHome.class);
            return ejbHome;
        } catch (Throwable e) {
            throw (org.omg.CORBA.MARSHAL)new org.omg.CORBA.MARSHAL(e.getClass().getName() + " thrown while marshaling the reply: " + e.getMessage(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }

    public javax.ejb.Handle getHandle(ProxyInfo proxyInfo) {
        Handle handle = new CORBAHandle(getEJBObject(proxyInfo), proxyInfo.getPrimaryKey());
        return handle;
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo proxyInfo) {
        org.omg.CORBA.Object ejbHome = (org.omg.CORBA.Object) getEJBHome(proxyInfo);
        String ior = getOrb().object_to_string(ejbHome);
        HomeHandle homeHandle = new CORBAHomeHandle(ior);
        return homeHandle;
    }

    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo) {
        ContainerType componentType = proxyInfo.getBeanContainer().getContainerType();
        byte ejbType;
        switch (componentType) {
            case STATEFUL:
                ejbType = CORBAEJBMetaData.STATEFUL;
                break;
            case STATELESS:
                ejbType = CORBAEJBMetaData.STATELESS;
                break;
            case BMP_ENTITY:
            case CMP_ENTITY:
                ejbType = CORBAEJBMetaData.ENTITY;
                break;
            default:
                throw new IllegalArgumentException("Unknown component type: " + componentType);
        }

        BeanContext beanContext = proxyInfo.getBeanContext();
        CORBAEJBMetaData ejbMetaData = new CORBAEJBMetaData(getEJBHome(proxyInfo),
                ejbType,
                beanContext.getHomeInterface(),
                beanContext.getRemoteInterface(),
                beanContext.getPrimaryKeyClass());
        return ejbMetaData;
    }

    private static ORB getOrb() {
        try {
            Context context = new InitialContext();
            ORB orb = (ORB) context.lookup("java:comp/ORB");
            return orb;
        } catch (Throwable e) {
            throw (org.omg.CORBA.MARSHAL)new org.omg.CORBA.MARSHAL("Could not find ORB in jndi at java:comp/ORB", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES).initCause(e);
        }
    }
}
