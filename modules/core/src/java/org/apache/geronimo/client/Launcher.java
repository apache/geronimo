/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.ServiceNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.jmx.JMXKernel;
import org.apache.geronimo.jmx.JMXUtil;
import org.apache.geronimo.proxy.ProxyInvocation;

/**
 * Launcher for J2EE Application Clients.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/23 22:14:20 $
 */
public class Launcher {
    static {
        // Add our default Commons Logger that support the trace level
        if (System.getProperty(LogFactoryImpl.LOG_PROPERTY) == null) {
            System.setProperty(LogFactoryImpl.LOG_PROPERTY, "org.apache.geronimo.core.log.Log4jLog");
        }
    }

    private static final Log log = LogFactory.getLog("Geronimo Client");
    private static final String USAGE = "usage: " + Launcher.class.getName() + " <client.jar> [application args]";
    private static final String[] DEPLOY_ARG_TYPES = new String[]{"java.net.URL"};

    private JMXKernel kernel;
    private MBeanServer mbServer;
    private URL clientURL;
    private String[] appArgs;
    private ObjectName controllerName;
    private ObjectName clientName;

    private Launcher(String[] args) throws IllegalArgumentException {
        parseCommandLine(args);
    }

    private void deploy() throws DeploymentException {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread("Geronimo-Shutdown", this));

        kernel = new JMXKernel("Geronimo Client");
        mbServer = kernel.getMBeanServer();
        URL mletURL;
        try {
            mletURL = new URL("file:src/conf/client.mlet");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unable to boot client.mlet");
        }
        Set bootedMBeans;
        try {
            bootedMBeans = kernel.bootMLet(mletURL);
        } catch (ServiceNotFoundException e) {
            throw new IllegalStateException("Could not load " + mletURL);
        }

        // check they all started OK and it included a controller and service planner
        ObjectName controllerPattern = JMXUtil.getObjectName("*:role=DeploymentController,*");
        ObjectName plannerPattern = JMXUtil.getObjectName("*:role=DeploymentPlanner,type=Client,*");
        boolean planner = false;
        for (Iterator i = bootedMBeans.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof ObjectInstance) {
                ObjectName mletName = ((ObjectInstance) o).getObjectName();
                if (controllerPattern.apply(mletName)) {
                    if (controllerName != null) {
                        throw new DeploymentException("Multiple DeploymentControllers specified in boot mlet");
                    }
                    controllerName = mletName;
                } else if (plannerPattern.apply(mletName)) {
                    planner = true;
                }
            } else if (o instanceof DeploymentException) {
                throw (DeploymentException) o;
            } else if (o instanceof RuntimeException) {
                throw (RuntimeException) o;
            } else if (o instanceof Error) {
                throw (Error) o;
            } else {
                throw new DeploymentException((Exception) o);
            }
        }
        if (controllerName == null) {
            throw new DeploymentException("Boot mlet did not load a DeploymentController");
        } else if (!planner) {
            throw new DeploymentException("Boot mlet did not load a DeploymentPlanner for type=Service");
        }

        log.info("Deploying Application Client from " + clientURL);
        // @todo use the deployer to create the app client rather than doing it by hand
//        try {
//            mbServer.invoke(controllerName, "deploy", new Object[]{clientURL}, DEPLOY_ARG_TYPES);
//        } catch (InstanceNotFoundException e) {
//            throw new DeploymentException(e);
//        } catch (MBeanException e) {
//            throw new DeploymentException(e);
//        } catch (ReflectionException e) {
//            throw new DeploymentException(e);
//        }

        AppClientContainer clientMBean = new AppClientContainer();
        clientName = JMXUtil.getObjectName("geronimo.client:url=" + ObjectName.quote(clientURL.toString()));
        try {
            mbServer.registerMBean(clientMBean, clientName);
        } catch (MBeanRegistrationException e) {
            Exception targetException = e.getTargetException();
            if (targetException instanceof DeploymentException) {
                throw (DeploymentException) targetException;
            }
            throw new DeploymentException(targetException);
        } catch (InstanceAlreadyExistsException e) {
            throw new DeploymentException(e);
        } catch (NotCompliantMBeanException e) {
            throw new DeploymentException(e);
        }

        String mainClassName;
        try {
            Manifest manifest;
            if (clientURL.toString().endsWith("/")) {
                // unpacked
                URL manifestURL = new URL(clientURL, "META-INF/MANIFEST.MF");
                InputStream is = manifestURL.openStream();
                manifest = new Manifest(is);
                is.close();
            } else {
                URL jarURL = new URL("jar:" + clientURL + "!/");
                JarURLConnection jarConn = (JarURLConnection) jarURL.openConnection();
                manifest = jarConn.getManifest();
            }
            Attributes attrs = manifest.getMainAttributes();
            mainClassName = (String) attrs.get(Attributes.Name.MAIN_CLASS);
            if (mainClassName == null) {
                throw new DeploymentException("No Main-Class defined in manifest for " + clientURL);
            }
        } catch (IOException e) {
            throw new DeploymentException("Unable to get Main-Class from manifest for " + clientURL, e);
        }

        try {
            AttributeList attrs = new AttributeList(2);
            attrs.add(new Attribute("MainClassName", mainClassName));
            attrs.add(new Attribute("ClientURL", clientURL));
            mbServer.setAttributes(clientName, attrs);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        try {
            mbServer.invoke(clientName, "start", null, null);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private void undeploy() {
        if (kernel != null) {
            if (controllerName != null) {
                MBeanServer mbServer = kernel.getMBeanServer();
                try {
                    mbServer.invoke(clientName, "stop", null, null);
                } catch (Exception e) {
                    log.error("Error stopping client", e);
                }

//                try {
//                    mbServer.invoke(controllerName, "undeploy", new Object[]{clientURL}, DEPLOY_ARG_TYPES);
//                } catch (InstanceNotFoundException e) {
//                    log.error("Error undeploying client", e);
//                } catch (MBeanException e) {
//                    log.error("Error undeploying client", e);
//                } catch (ReflectionException e) {
//                    log.error("Error undeploying client", e);
//                }
            }
            kernel.release();
        }
    }

    public void run() {
        try {
            ProxyInvocation invocation = new ProxyInvocation();
            ProxyInvocation.putArguments(invocation, new Object[] { appArgs });
            mbServer.invoke(clientName, "invoke", new Object[]{invocation}, new String[]{"org.apache.geronimo.common.Invocation"});
        } catch (InstanceNotFoundException e) {
            IllegalStateException ex = new IllegalStateException("Unable to invoke app client");
            ex.initCause(e);
            throw ex;
        } catch (MBeanException e) {
            log.error("Application threw Exception", e.getCause());
            return;
        } catch (ReflectionException e) {
            IllegalStateException ex = new IllegalStateException("Unable to invoke app client");
            ex.initCause(e);
            throw ex;
        }
    }

    public static void main(String[] args) {
        Launcher launcher;
        try {
            launcher = new Launcher(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        try {
            launcher.deploy();
        } catch (DeploymentException e) {
            e.printStackTrace();
            System.exit(2);
            return;
        }

        launcher.run();
    }

    private void parseCommandLine(String[] args) throws IllegalArgumentException {
        if (args.length < 1) {
            throw new IllegalArgumentException(USAGE);
        }
        try {
            clientURL = new URL(args[0]);
        } catch (MalformedURLException e) {
            // wasn't a valid URL - see if it is a filename
            File file = new File(args[0]);
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + file.getPath());
            }
            try {
                clientURL = file.toURL();
            } catch (MalformedURLException e1) {
                throw new IllegalArgumentException("Could not convert to URL: " + args[0]);
            }
        }
        appArgs = new String[args.length - 1];
        System.arraycopy(args, 1, appArgs, 0, args.length - 1);
    }

    private static class ShutdownThread extends Thread {
        private final Launcher launcher;

        private ShutdownThread(String name, Launcher launcher) {
            super(name);
            this.launcher = launcher;
        }

        public void run() {
            launcher.undeploy();
        }
    }
}
