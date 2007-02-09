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

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;

/**
 * @version $Revision: 465108 $ $Date: 2006-10-17 17:23:40 -0700 (Tue, 17 Oct 2006) $
 */
public class CORBAEJBMetaData implements EJBMetaData, java.io.Serializable {

    private static final long serialVersionUID = 8085488135161906381L;

    public final static byte ENTITY = 1;
    public final static byte STATEFUL = 2;
    public final static byte STATELESS = 3;

    /**
     * The Class of the bean's home interface.
     */
    private final Class homeInterface;

    /**
     * The Class of the bean's remote interface.
     */
    private final Class remoteInterface;

    /**
     * The Class of the bean's primary key or null if the
     * bean is of a type that does not require a primary key.
     */
    private final Class primaryKeyClass;

    /**
     * The EJBHome stub/proxy for this bean deployment.
     */
    private final EJBHome ejbHome;

    /**
     * The type of bean that this MetaData implementation represents.
     *
     * @see #ENTITY
     * @see #STATEFUL
     * @see #STATELESS
     */
    private final byte ejbType;

    public CORBAEJBMetaData(EJBHome ejbHome, byte ejbType, Class homeInterface, Class remoteInterface, Class primaryKeyClass) {
        if (homeInterface == null) {
            throw new IllegalArgumentException("Home interface is null");
        }
        if (remoteInterface == null) {
            throw new IllegalArgumentException("Remote interface is null");
        }
        if (ejbType == ENTITY && primaryKeyClass == null) {
            throw new IllegalArgumentException("Entity bean must have a primary key class");
        }
        if (ejbType != ENTITY && primaryKeyClass != null) {
            throw new IllegalArgumentException("Session bean must have a primary key class");
        }
        this.ejbHome = ejbHome;
        this.ejbType = ejbType;
        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.primaryKeyClass = primaryKeyClass;
    }

    public Class getHomeInterfaceClass() {
        return homeInterface;
    }

    public Class getRemoteInterfaceClass() {
        return remoteInterface;
    }

    public Class getPrimaryKeyClass() {
        if (ejbType == ENTITY) {
            return primaryKeyClass;
        } else {
            throw new UnsupportedOperationException("Session objects are private resources and do not have primary keys");
        }
    }

    public boolean isSession() {
        return (ejbType == STATEFUL || ejbType == STATELESS);
    }

    public boolean isStatelessSession() {
        return ejbType == STATELESS;
    }

    public EJBHome getEJBHome() {
        return (EJBHome) PortableRemoteObject.narrow(ejbHome, EJBHome.class);
    }
}
