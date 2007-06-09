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
package org.apache.geronimo.tomcat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.InstanceManager;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.security.credentialstore.CredentialStore;

/**
 * @version $Rev$ $Date$
 */
public interface TomcatContext {

    public String getContextPath();

    public void setContext(Context ctx);

    public Context getContext();

    public String getDocBase();

    public SecurityHolder getSecurityHolder();

    public String getVirtualServer();

    public ClassLoader getClassLoader();

    public UserTransaction getUserTransaction();

    public javax.naming.Context getJndiContext();

    public Kernel getKernel();

    public Set getApplicationManagedSecurityResources();

    public TrackedConnectionAssociator getTrackedConnectionAssociator();

    public Set getUnshareableResources();

    public Realm getRealm();

    public List getValveChain();

    public CatalinaCluster getCluster();

    public Manager getManager();

    public boolean isCrossContext();

    public boolean isDisableCookies();

    public Map getWebServices();

    public InstanceManager getInstanceManager();

}
