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


package org.apache.geronimo.system.util;

import java.sql.Driver;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class JDBCDriverRegistrationGBean implements GBeanLifecycle {

    private final Class ddClass;
    private final Driver instance;

    public JDBCDriverRegistrationGBean(String className, ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ddClass = classLoader.loadClass("org.apache.geronimo.jdbc.DelegatingDriver");
        Method m = ddClass.getDeclaredMethod("registerDriver", new Class[] {Driver.class});
        Class clazz = classLoader.loadClass(className);
        instance = (Driver) clazz.newInstance();
        //This is definitely enough to make the dd register itself with DriverManager
        m.invoke(null, new Object[] {instance});
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        Method m = ddClass.getDeclaredMethod("unregisterDriver", new Class[] {Driver.class});
        m.invoke(null, new Object[] {instance});
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //ignore
        }
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(JDBCDriverRegistrationGBean.class, "GBean");
        infoBuilder.addAttribute("driverClassName", String.class, true);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[]{"driverClassName", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
