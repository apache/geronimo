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

import org.apache.axis.AxisFault;
import org.apache.axis.client.AdminClient;
import org.apache.axis.client.Call;
import org.apache.axis.utils.ClassUtils;
import org.apache.axis.utils.NetworkUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.openejb.ContainerIndex;
import org.openejb.EJBContainer;

import javax.ejb.EJBHome;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Class AxisGeronimoUtils
 */
public class AxisGeronimoUtils {
    public static final int AXIS_SERVICE_PORT = 5678;
    public static HashSet startedGbeans = new HashSet();

    public static final Log log = LogFactory.getLog(AxisGeronimoUtils.class);

    public static Object invokeEJB(String ejbName,
                                   String methodName,
                                   Class[] parmClasses,
                                   Object[] parameters) throws AxisFault {
        try {
            ContainerIndex index = ContainerIndex.getInstance();
            int length = index.length();
            log.info("number of continers " + length);
            for (int i = 0; i < length; i++) {
                EJBContainer contianer = index.getContainer(i);
                if (contianer != null) {
                    String name = contianer.getEJBName();
                    log.debug("found the ejb " + name);
                    if (ejbName.equals(name)) {
                        EJBHome statelessHome = contianer.getEJBHome();
                        Object stateless = statelessHome.getClass().getMethod("create", null).invoke(statelessHome, null);
                        Method[] methods = stateless.getClass().getMethods();
                        for (int j = 0; j < methods.length; j++) {
                            if (methods[j].getName().equals(methodName)) {
                                try {
                                    return methods[j].invoke(stateless, parameters);
                                } catch (Exception e) {
                                    Class[] classes = methods[j].getParameterTypes();
                                    log.info(methodName + "(");
                                    if (parameters == null || classes == null) {
                                        log.debug("both or one is null");
                                    } else {
                                        if (parameters.length != classes.length)
                                            log.debug("parameter length do not match expected parametes");
                                        for (int k = 0; k < classes.length; k++) {
                                            Object obj = parameters[k];
                                            Class theClass = classes[k];
                                            if (theClass != obj.getClass()) {
                                                log.debug("calsses are differant");
                                            }
                                            log.debug("ejb class loader " + theClass.getClassLoader());
                                            log.debug("parameter class loader = " + obj.getClass().getClassLoader());
                                        }
                                    }
                                    throw e;
                                }
                            }
                        }
                        throw new NoSuchMethodException(methodName + " not found");
                    }
                } else {
                    log.debug("Continer is null");
                }
            }
            throw new AxisFault("Dependancy ejb " + ejbName + " not found ");
        } catch (Throwable e) {
            e.printStackTrace();
            if (e instanceof Exception)
                throw AxisFault.makeFault((Exception) e);
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
            startedGbeans.add(objectName);
            kernel.loadGBean(objectName, gbean);
            kernel.startGBean(objectName);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public static void startGBeanOnlyIfNotStarted(ObjectName objectName, GBeanMBean gbean, Kernel kernel)
            throws DeploymentException {
        try {
            if (!checkAlreadyStarted(objectName, kernel)) {
                startGBean(objectName, gbean, kernel);
                log.info("Started .. " + objectName);
            } else {
                log.info(objectName + " GBean already started");
            }
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
            if (startedGbeans.contains(objectName)) {
                kernel.stopGBean(objectName);
                kernel.unloadGBean(objectName);
                log.info("stoped .. " + objectName);
            } else {
                log.info(objectName + " was runing when axis start it "
                        + "Axis will not stop it");
            }
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

    public static ArrayList getClassFileList(ZipFile file) {
        ArrayList list = new ArrayList();
        if (file != null) {
            Enumeration entires = file.entries();
            while (entires.hasMoreElements()) {
                ZipEntry zipe = (ZipEntry) entires.nextElement();
                String name = zipe.getName();
                if (name.endsWith(".class")) {
                    int index = name.lastIndexOf('.');
                    name = name.substring(0, index);
                    name = name.replace('\\', '.');
                    list.add(name.replace('/', '.'));
                }
            }
        }
        return list;
    }

    public static boolean checkAlreadyStarted(ObjectName name, Kernel kernel) {
        Set set = kernel.listGBeans(name);
        log.info(name + " = " + set);
        if (set == null)
            return false;
        if (set.isEmpty()) {
            return false;
        }
        return true;
    }

    public static void registerClassLoader(ZipFile module, ClassLoader classloader) throws ZipException, IOException {
        ArrayList classList = AxisGeronimoUtils.getClassFileList(module);
        for (int i = 0; i < classList.size(); i++) {
            String className = (String) classList.get(i);
            ClassUtils.setClassLoader(className, classloader);
        }
    }

    /**
     * <p>add the entry to the Axis Confieration file about the web service.
     * This find the coniguration file and and update this. There are two problems.
     * Number one is service is deployed only once the Axis is restarted. And it is
     * best not to do this while the Axis is running.</p>
     *
     * @param deplydd
     * @throws DeploymentException
     */
    public static void addEntryToAxisDD(InputStream deplydd) throws DeploymentException {
        try {
            if (deplydd != null) {
                AdminClient adminClient = new AdminClient();
                URL requestUrl = getURL("/axis/services/AdminService");
                Call call = adminClient.getCall();
                call.setTargetEndpointAddress(requestUrl);
                String result = adminClient.process(null, deplydd);
            } else {
                throw new DeploymentException("the deploy.wsdd can not be found");
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentException(e);
        }
    }

    public static URL getURL(String file) throws MalformedURLException {
        URL requestUrl = new URL("http", NetworkUtils.getLocalHostname(), AXIS_SERVICE_PORT, file);
        return requestUrl;
    }

}
