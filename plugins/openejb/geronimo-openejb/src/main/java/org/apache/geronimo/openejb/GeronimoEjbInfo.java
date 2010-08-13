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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.io.Serializable;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;

public class GeronimoEjbInfo implements Serializable {
	
    private final EjbJarInfo ejbJarInfo;
    private final JndiEncInfo globalJndiEnc;
    private final JndiEncInfo appJndiEnc;
    
    public GeronimoEjbInfo(EjbJarInfo ejbJarInfo) {
        this(ejbJarInfo, new JndiEncInfo(), new JndiEncInfo());
    }
    
    public GeronimoEjbInfo(EjbJarInfo ejbJarInfo, JndiEncInfo globalJndiEnc, JndiEncInfo appJndiEnc) {
        this.ejbJarInfo = ejbJarInfo;
        this.globalJndiEnc = globalJndiEnc;
        this.appJndiEnc = appJndiEnc;
    }
    
    public EjbJarInfo getEjbJarInfo() {
        return ejbJarInfo;
    }
    
    public AppInfo createAppInfo() {
        AppInfo appInfo = new AppInfo();
        appInfo.jarPath = ejbJarInfo.jarPath;
        appInfo.ejbJars.add(ejbJarInfo);
        merge(appInfo.appJndiEnc, appJndiEnc);
        merge(appInfo.globalJndiEnc, globalJndiEnc);
        return appInfo;
    }
    
    private static void merge(JndiEncInfo dest, JndiEncInfo source) {
        dest.envEntries.addAll(source.envEntries);
        dest.resourceRefs.addAll(source.resourceRefs);
        dest.resourceEnvRefs.addAll(source.resourceEnvRefs);
        dest.persistenceContextRefs.addAll(source.persistenceContextRefs);
        dest.persistenceUnitRefs.addAll(source.persistenceUnitRefs);
        dest.serviceRefs.addAll(source.serviceRefs);
        dest.ejbLocalReferences.addAll(source.ejbLocalReferences);
        dest.ejbReferences.addAll(source.ejbReferences);
    }
}
