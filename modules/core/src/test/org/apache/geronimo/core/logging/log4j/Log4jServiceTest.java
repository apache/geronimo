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

package org.apache.geronimo.core.logging.log4j;

import java.io.File;
import java.net.InetAddress;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/22 04:24:57 $
 *
 * */
public class Log4jServiceTest extends TestCase {

    private ObjectName log4jServiceName;
    private GBeanMBean log4jService;
    private ObjectName log4jSocketServerName;
    private GBeanMBean log4jSocketServer;
    private Kernel kernel;

    protected void setUp() throws Exception {
        log4jServiceName = new ObjectName("test:name=Log4jService");
        log4jService = new GBeanMBean(Log4jService.getGBeanInfo());
        log4jService.setAttribute("ConfigurationURL", new File(new File("."), "src/test-data/xml/log4j-config.xml").toURL());
        log4jService.setAttribute("RefreshPeriod", new Integer(10));
        log4jSocketServerName = new ObjectName("test:name=Log4jSocketServer");
        log4jSocketServer = new GBeanMBean(Log4jSocketServer.getGBeanInfo());
        log4jSocketServer.setAttribute("BindAddress", InetAddress.getLocalHost());
        log4jSocketServer.setAttribute("Port", new Integer(8090));
        log4jSocketServer.setAttribute("Backlog", new Integer(50));
        log4jSocketServer.setAttribute("ListenerEnabled", Boolean.TRUE);
        log4jSocketServer.setAttribute("LoggerRepositoryFactoryType", Log4jSocketServer.DefaultLoggerRepositoryFactory.class);
        kernel = new Kernel("test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
     }

    public void testLog4jService() throws Exception {
        kernel.loadGBean(log4jServiceName, log4jService);
        kernel.startGBean(log4jServiceName);
        kernel.stopGBean(log4jServiceName);
        kernel.unloadGBean(log4jServiceName);
    }

   public void testLog4jSocketServer() throws Exception {
        kernel.loadGBean(log4jSocketServerName, log4jSocketServer);
        kernel.startGBean(log4jSocketServerName);
        kernel.stopGBean(log4jSocketServerName);
        kernel.unloadGBean(log4jSocketServerName);
    }
}
