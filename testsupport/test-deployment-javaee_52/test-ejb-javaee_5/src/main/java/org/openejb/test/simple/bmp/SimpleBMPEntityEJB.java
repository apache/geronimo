/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.openejb.test.simple.bmp;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class SimpleBMPEntityEJB implements EntityBean {
    private static final Integer PK = new Integer(1);
    private static String name = "SomeName";
    private static String value = "SomeValue";

    public Integer ejbCreate() {
        return PK;
    }

    public void ejbPostCreate() {
    }

    public Integer ejbFindByPrimaryKey(Integer key) throws javax.ejb.FinderException {
        if(PK.equals(key)) {
            return PK;
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        SimpleBMPEntityEJB.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        SimpleBMPEntityEJB.value = value;
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbLoad() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    public void ejbRemove() throws RemoveException, EJBException {
    }

    public void ejbStore() throws EJBException {
    }

    public void setEntityContext(EntityContext ctx) throws EJBException {
    }

    public void unsetEntityContext() throws EJBException {
    }
}
