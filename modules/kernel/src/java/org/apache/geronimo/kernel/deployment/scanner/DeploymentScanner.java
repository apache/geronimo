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
package org.apache.geronimo.kernel.deployment.scanner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.relation.RelationServiceMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.AbstractManagedObject;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.deployment.DeploymentController;

/**
 * An MBean that maintains a list of URLs and periodically invokes a Scanner
 * to search them for deployments.
 *
 * @version $Revision: 1.3 $ $Date: 2003/11/16 00:52:22 $
 */
public class DeploymentScanner implements GeronimoMBeanTarget {

    private static final Log log = LogFactory.getLog(DeploymentScanner.class);

    private GeronimoMBeanContext context;

    private final Map scanners = new HashMap();
    private long scanInterval;
    private boolean run;
    private Thread scanThread;
    private DeploymentController deploymentController;

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(DeploymentScanner.class.getName());
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ScanInterval",
                true, true,
                "Milliseconds between deployment scans"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("WatchedURLs",
                true, false,
                "Set of scanned URLs, without recursive information"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("addURL",
                new GeronimoParameterInfo[]{new GeronimoParameterInfo("URL", URL.class.getName(), "URL to scan"),
                                            new GeronimoParameterInfo("Recurse", "boolean", "Should subdirectories be scanned")},
                1,
                "Start scanning a single URL"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("removeURL",
                new GeronimoParameterInfo[]{new GeronimoParameterInfo("URL", URL.class.getName(), "URL to scan")},
                1,
                "Stop scanning a single URL"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("scanNow",
                new GeronimoParameterInfo[]{},
                1,
                "Scan all URLs now"));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("DeploymentController",
                DeploymentController.class.getName(),
                ObjectName.getInstance("geronimo.deployment:role=DeploymentController"),
                true));
        return mbeanInfo;
    }

    public void setDeploymentController(DeploymentController deploymentController) {
        this.deploymentController = deploymentController;
    }

    public DeploymentScanner() {
    }

    public DeploymentScanner(final URL[] urls, final long scanInterval) {
        for (int i = 0; i < urls.length; i++) {
            addURL(urls[i], true);
        }
        setScanInterval(scanInterval);
    }


    public synchronized long getScanInterval() {
        return scanInterval;
    }

    public synchronized void setScanInterval(long scanInterval) {
        this.scanInterval = scanInterval;
    }

    public synchronized Set getWatchedURLs() {
        return Collections.unmodifiableSet(new HashSet(scanners.keySet()));
    }

    public synchronized void addURL(URL url, boolean recurse) {
        if (!scanners.containsKey(url)) {
            log.debug("Watching URL: " + url);
            Scanner scanner = getScannerForURL(url, recurse);
            scanners.put(url, scanner);
        }
    }

    private Scanner getScannerForURL(URL url, boolean recurse) {
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            return new FileSystemScanner(new File(url.getFile()), recurse);
        } else if ("http".equals(protocol) || "https".equals(protocol)) {
            return new WebDAVScanner(url, recurse);
        } else {
            throw new IllegalArgumentException("Unknown protocol " + protocol);
        }
    }

    public synchronized void removeURL(URL url) {
        scanners.remove(url);
    }

    private synchronized boolean shouldScannerThreadRun() {
        return run;
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    public boolean canStart() {
        return true;
    }

    public synchronized void doStart() {
        if (scanThread == null) {
            run = true;
            scanThread = new Thread("DeploymentScanner: ObjectName=" + context.getObjectName()) {
                public void run() {
                    while (shouldScannerThreadRun()) {
                        scanNow();
                        try {
                            Thread.sleep(getScanInterval());
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
            scanThread.start();
        }
    }

    public boolean canStop() {
        return true;
    }

    public synchronized void doStop() {
        run = false;
        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }
    }

    public void doFail() {
    }

    public void scanNow() {
        boolean logTrace = log.isTraceEnabled();

        Set results = new HashSet();
        Set scannersCopy;
        synchronized (this) {
            scannersCopy = new HashSet(scanners.entrySet());
        }
        for (Iterator i = scannersCopy.iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            URL url = (URL) entry.getKey();
            if (logTrace) {
                log.trace("Starting scan of " + url);
            }
            Scanner scanner = (Scanner) entry.getValue();
            try {
                Set result = scanner.scan();
                if (logTrace) {
                    log.trace("Finished scan of " + url + ", " + result.size() + " deployment(s) found");
                }
                results.addAll(result);
            } catch (IOException e) {
                log.error("Error scanning url " + url, e);
            }
        }

        try {
            deploymentController.planDeployment(context.getObjectName(), results);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
