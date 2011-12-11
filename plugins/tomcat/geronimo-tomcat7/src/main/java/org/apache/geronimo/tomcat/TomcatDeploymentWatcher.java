/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.tomcat;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.config.DeploymentWatcher;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.osgi.web.WebApplicationUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
@OsgiService
public class TomcatDeploymentWatcher implements DeploymentWatcher, GBeanLifecycle, SynchronousBundleListener {

    private static final Logger logger = LoggerFactory.getLogger(TomcatDeploymentWatcher.class);

    private final Map<AbstractName, File> abstractNameCleanUpDirectoryMap = new ConcurrentHashMap<AbstractName, File>();

    private final Map<Artifact, long[]> ebaArtifactApplicationContentBundleIdsMap = new ConcurrentHashMap<Artifact, long[]>();

    private final Set<Long> embeddedWABBundleIds = Collections.synchronizedSet(new HashSet<Long>());

    private final Kernel kernel;

    private final Map<Long, File> wabBundleIdCleanUpDirectoryMap = new ConcurrentHashMap<Long, File>();

    private final BundleContext bundleContext;

    private final LifecycleListener ebaLifecycleListener = new EBALifecycleListener();

    public TomcatDeploymentWatcher(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel, @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this.kernel = kernel;
        this.bundleContext = bundleContext;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        Long bundleId = event.getBundle().getBundleId();
        if (event.getType() == Bundle.UNINSTALLED && WebApplicationUtils.isWebApplicationBundle(bundle) && !embeddedWABBundleIds.contains(bundleId)) {
            File cleanUpDirectory = wabBundleIdCleanUpDirectoryMap.remove(bundleId);
            if (cleanUpDirectory != null) {
                FileUtils.recursiveDelete(cleanUpDirectory);
            }
        }
    }

    public void deleteOnUndeployed(Bundle bundle, AbstractName abName, File cleanUpDirectory) {
        if (WebApplicationUtils.isWebApplicationBundle(bundle)) {
            wabBundleIdCleanUpDirectoryMap.put(bundle.getBundleId(), cleanUpDirectory);
        } else {
            abstractNameCleanUpDirectoryMap.put(abName, cleanUpDirectory);
        }
    }

    @Override
    public void deployed(Artifact artifact) {
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        kernel.getLifecycleMonitor().addLifecycleListener(ebaLifecycleListener, new AbstractNameQuery("org.apache.geronimo.aries.ApplicationGBean"));
        bundleContext.addBundleListener(this);
    }

    @Override
    public void doStop() throws Exception {
        kernel.getLifecycleMonitor().removeLifecycleListener(ebaLifecycleListener);
        bundleContext.removeBundleListener(this);
    }

    @Override
    public void undeployed(Artifact artifact) {
        //a. EBA Bundle
        long[] applicationBundleIds = ebaArtifactApplicationContentBundleIdsMap.remove(artifact);
        if (applicationBundleIds != null) {
            for (long applicationBundleId : applicationBundleIds) {
                File cleanUpDirectory = wabBundleIdCleanUpDirectoryMap.remove(applicationBundleId);
                if (cleanUpDirectory != null) {
                    embeddedWABBundleIds.remove(applicationBundleId);
                    FileUtils.recursiveDelete(cleanUpDirectory);
                }
            }
            return;
        }
        //b. Common Application
        for (Iterator<Map.Entry<AbstractName, File>> it = abstractNameCleanUpDirectoryMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<AbstractName, File> entry = it.next();
            if (entry.getKey().getArtifact().equals(artifact)) {
                FileUtils.recursiveDelete(entry.getValue());
                it.remove();
            }
        }
    }

    private class EBALifecycleListener extends LifecycleAdapter {

        @Override
        public void running(AbstractName abName) {
            try {
                long[] applicationContentBundleIds = (long[]) kernel.getAttribute(abName, "applicationContentBundleIds");
                for (long applicationContentBundleId : applicationContentBundleIds) {
                    embeddedWABBundleIds.add(applicationContentBundleId);
                }
                ebaArtifactApplicationContentBundleIdsMap.put(abName.getArtifact(), applicationContentBundleIds);
            } catch (GBeanNotFoundException e) {
                logger.error("Unable to find application content bundle Ids from GBean " + abName, e);
            } catch (NoSuchAttributeException e) {
                logger.error("Unable to find application content bundle Ids from GBean " + abName, e);
            } catch (Exception e) {
                logger.error("Unable to find application content bundle Ids from GBean " + abName, e);
            }
        }
    }
}
