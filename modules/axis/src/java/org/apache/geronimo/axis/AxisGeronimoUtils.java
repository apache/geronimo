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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ejb.EJBHome;
import javax.management.ObjectName;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.openejb.ContainerIndex;
import org.openejb.EJBContainer;

/**
 * Class AxisGeronimoUtils
 */
public class AxisGeronimoUtils {
    
    public static final Log log = LogFactory.getLog(AxisGeronimoUtils.class);
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
                            Method[] methods = stateless.getClass().getMethods();
                            for(int j = 0;j< methods.length;j++){
                                if(methods[j].getName().equals(methodName)){
                                    try{
                                        return methods[j].invoke(stateless, parameters);
                                    }catch(Exception e){
                                        Class[] classes = methods[j].getParameterTypes();
                                        System.out.print(methodName+"("); 
                                        if(parameters == null || classes== null){
                                            System.out.println("both or one is null");
                                        }else{
                                            if(parameters.length != classes.length)
                                                System.out.println("parameter length do not match expected parametes");
                                            for(int k = 0;k<classes.length;k++){
                                                Object obj = parameters[k];
                                                Class theClass = classes[k];
                                                if(theClass != obj.getClass()){
                                                    System.out.println("calsses are differant");
                                                }
                                                System.out.println("ejb class loader "+theClass.getClassLoader());                                                        
                                                System.out.println("parameter class loader = "+obj.getClass().getClassLoader());
                                            }
                                        }
                                        throw e;                         
                                    }

                                }
                            }
                            throw new NoSuchMethodException(methodName+" not found");
                        }           
                    }else{
                        System.out.println("Continer is null");
                        log.debug("Continer is null");
                    }
                }
                throw new AxisFault("Dependancy ejb "+ejbName+" not found ");
            } catch (Throwable e) {
                e.printStackTrace();
                if(e instanceof Exception)
                    throw AxisFault.makeFault((Exception)e);
                else
                    throw AxisFault.makeFault(new Exception(e));    
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
    
    public static ArrayList getClassFileList(ZipFile file){
        ArrayList list = new ArrayList();
        if(file != null){
            Enumeration entires = file.entries();
            while(entires.hasMoreElements()){
                ZipEntry zipe = (ZipEntry)entires.nextElement();
                String name = zipe.getName();
                if(name.endsWith(".class")){
                    int index = name.lastIndexOf('.');
                    list.add(name.substring(0,index).replace('/','.'));
               } 
           }
        }
        return list;    
    }
}
