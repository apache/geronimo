/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import java.io.File;
import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.management.ObjectName;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.ews.ws4j2ee.wsutils.GeronimoUtils;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.openejb.ContainerIndex;
import org.openejb.EJBContainer;

/**
 * Class AxisGeronimoUtils
 */
public class AxisGeronimoUtils {
    
    public static final Log log = LogFactory.getLog(GeronimoUtils.class);
    public static Object invokeEJB(
        String ejbName,
        String methodName,
        Class[] parmClasses,
        Object[] parameters)throws AxisFault{
            try {
                ContainerIndex index = ContainerIndex.getInstance();
                int length = index.length();
                System.out.println("number of continers "+length);
                for(int i = 0;i<length;i++){
                    EJBContainer contianer = index.getContainer(i);
                    if(contianer!= null){
                        String name = contianer.getEJBName();
                        System.out.println("found the ejb "+name);
                        log.debug("found the ejb "+name);
                        if(ejbName.equals(name)){
                            EJBHome statelessHome = contianer.getEJBHome();
                            Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
                            if(parmClasses!= null){
                                Object obj = stateless.getClass().getMethod(methodName,parmClasses).invoke(stateless, parameters);
                                return obj; 
                            }else{
                                Method[] methods = stateless.getClass().getMethods();
                                for(int j = 0;i< methods.length;j++){
                                    if(methods[j].getName().equals(methodName)){
                                        return methods[j].invoke(stateless, parameters);
                                    }
                                }
                                throw new NoSuchMethodException(methodName+" not found");
                            }
                        }                   
                    }else{
                        System.out.println("Continer is null");
                        log.debug("Continer is null");
                    }
                }
                throw new AxisFault("Dependancy ejb "+ejbName+" not found ");
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            } 
    
    }

    /**
     * Method startGBean
     *
     * @param objectName
     * @param gbean
     * @param kernel
     * @throws DeploymentException
     */
    public static void startGBean(ObjectName objectName, GBeanMBean gbean, Kernel kernel)
            throws DeploymentException {
        try {
            kernel.loadGBean(objectName, gbean);
            kernel.startGBean(objectName);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method stopGBean
     *
     * @param objectName
     * @param kernel
     * @throws DeploymentException
     */
    public static void stopGBean(ObjectName objectName, Kernel kernel)
            throws DeploymentException {
        try {
            kernel.unloadGBean(objectName);
            kernel.stopGBean(objectName);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method delete
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    delete(files[i]);
                }
            }
            file.delete();
        }
    }
}
