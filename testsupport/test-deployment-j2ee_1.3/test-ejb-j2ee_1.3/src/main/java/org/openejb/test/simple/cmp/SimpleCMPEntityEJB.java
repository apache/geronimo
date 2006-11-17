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

package org.openejb.test.simple.cmp;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.CreateException;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public abstract class SimpleCMPEntityEJB implements EntityBean {
    public SimpleCMPEntityEJB() {
    }

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract String getFirstName();

    public abstract void setFirstName(String firstName);

    public abstract String getLastName();

    public abstract void setLastName(String lastName);

    public Integer ejbCreate(Integer id) throws CreateException {
        setId(id);
        return null;
    }

    public void ejbPostCreate(Integer id) {
    }

    public void ejbActivate() {
    }

    public void ejbLoad() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public void ejbStore() {
    }

    public void setEntityContext(EntityContext ctx) {
    }

    public void unsetEntityContext() {
    }

}
