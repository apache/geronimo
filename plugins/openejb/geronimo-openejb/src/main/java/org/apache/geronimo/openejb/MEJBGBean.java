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

import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.mgmt.MEJBBean;

public class MEJBGBean implements GBeanLifecycle {
	
    public static final GBeanInfo GBEAN_INFO;
    
    private EjbContainer ejbContainer;
    
    public MEJBGBean(EjbContainer ejbContainer) {
    	this.ejbContainer = ejbContainer;
    }
    
	public void doStart() throws Exception {
		EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean("MEJB",MEJBBean.class.getName()));
        bean.setHomeAndRemote(ManagementHome.class, Management.class);
      
        ClassLoader cl = MEJBBean.class.getClassLoader();
        OpenEjbSystem openEjbSystem = ejbContainer.getOpenEjbSystem();
        //A dummy URL MEJBGBean.class.getResource( "" ).toString() to avoid the "java.net.MalformedURLException: no protocol: MEJBGBean" when startup
        EjbJarInfo ejbJarInfo = openEjbSystem.configureApplication(new EjbModule(cl, getClass().getSimpleName(), MEJBGBean.class.getResource( "" ).toString(), ejbJar, null));
        GeronimoEjbInfo ejbInfo = new GeronimoEjbInfo(ejbJarInfo);
        openEjbSystem.createApplication(ejbInfo.createAppInfo(), cl);
	}

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MEJBGBean.class);
        infoBuilder.addReference("StatelessContainer", EjbContainer.class);
        infoBuilder.setConstructor(new String[]{"StatelessContainer"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

	public void doStop() throws Exception {
		
	}
	
	public void doFail() {
		
	}

}
