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
package org.apache.geronimo.deployment.scanner;

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
import java.util.StringTokenizer;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.relation.RelationServiceMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.jmx.JMXUtil;
import org.apache.geronimo.management.AbstractManagedObject;

/**
 * An MBean that maintains a list of URLs and periodically invokes a Scanner
 * to search them for deployments.
 *
 *
 * @version $Revision: 1.10 $ $Date: 2003/08/21 04:32:41 $
 */
public class DeploymentScanner extends AbstractManagedObject implements DeploymentScannerMBean {
    private static final Log log = LogFactory.getLog(DeploymentScanner.class);
    private RelationServiceMBean relationService;
    private final Map scanners = new HashMap();
    private long scanInterval;
    private boolean run;
    private Thread scanThread;

    public DeploymentScanner() {
    }

    public DeploymentScanner(String initialURLs, boolean recurse) throws MalformedURLException {
        StringTokenizer tokenizer = new StringTokenizer(initialURLs, " \t\r\n,[]{}");
        while (tokenizer.hasMoreTokens()) {
            addURL(new URL(tokenizer.nextToken()), recurse);
        }
    }

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        relationService = JMXUtil.getRelationService(server);
        return super.preRegister(server, objectName);
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

    public void addURL(String url, boolean recurse) throws MalformedURLException {
        addURL(new URL(url), recurse);
    }

    public synchronized void addURL(URL url, boolean recurse) {
        if (!scanners.containsKey(url)) {
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

    public void removeURL(String url) throws MalformedURLException {
        removeURL(new URL(url));
    }

    public synchronized void removeURL(URL url) {
        scanners.remove(url);
    }

    private synchronized boolean shouldScannerThreadRun() {
        return run;
    }

    public synchronized void doStart() throws Exception {
        if (scanThread == null) {
            run = true;
            scanThread = new Thread("DeploymentScanner: ObjectName=" + objectName) {
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

    public synchronized void doStop() throws Exception {
        run = false;
        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }
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
            Map controllers = relationService.findAssociatedMBeans(objectName, "DeploymentController-DeploymentScanner", "DeploymentScanner");
            log.trace("Found " + controllers.size() + " controller(s)");
            if (!controllers.isEmpty()) {
                Set set = controllers.keySet();
                ObjectName controller = (ObjectName) set.iterator().next();
                server.invoke(controller,
                        "planDeployment",
                        new Object[]{objectName, results},
                        new String[]{"javax.management.ObjectName", "java.util.Set"});
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
