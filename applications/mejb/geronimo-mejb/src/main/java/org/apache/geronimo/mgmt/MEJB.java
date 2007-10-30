/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.mgmt;

import javax.annotation.security.RolesAllowed;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.openejb.mgmt.MEJBBean;

/**
 * MEJB represents the management EJB defined by JSR 77
 * MEJBBean extends javax.ejb.Statetless and has @RemoteHome(ManagementHome.class)
 * 
 * @version $Rev$ $Date$
 */

@Stateless(name="ejb/mgmt/MEJB")  //JNDI name will be java:comp/env/ejb/mgmt/MEJB
@RolesAllowed({"mejbadmin", "mejbuser"})
@RemoteHome(javax.management.j2ee.ManagementHome.class)

public class MEJB extends MEJBBean {
    
    @Override
    @RolesAllowed("mejbadmin")
    public void setAttribute(ObjectName objName, Attribute attrib) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        super.setAttribute(objName, attrib);
    }
    
    @Override
    @RolesAllowed("mejbadmin")
    public AttributeList setAttributes(ObjectName objName, AttributeList attribList)  throws InstanceNotFoundException, ReflectionException{
        return super.setAttributes(objName, attribList);
    }

    @Override
    @RolesAllowed("mejbadmin")
    public Object invoke(ObjectName objName, String str, Object[] objects, String[] strings) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return super.invoke(objName, str, objects, strings);
    }
}
