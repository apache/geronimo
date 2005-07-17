/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;

/**
 * @version $Rev$ $Date$
 */
public interface TomcatContext {

    public String getPath();

    public void setContext(Context ctx);

    public Context getContext();

    public String getDocBase();
    
    public SecurityHolder getSecurityHolder();
    
    public String getVirtualServer();
    
    public ClassLoader getWebClassLoader();
    
    public Map getComponentContext();

    public Kernel getKernel();
    
    public TransactionContextManager getTransactionContextManager();
    
    public Set getApplicationManagedSecurityResources();

    public TrackedConnectionAssociator getTrackedConnectionAssociator();

    public Set getUnshareableResources();
    
    public Realm getRealm();
    
    public List getValveChain();    
    
    public Map getWebServices();
}
