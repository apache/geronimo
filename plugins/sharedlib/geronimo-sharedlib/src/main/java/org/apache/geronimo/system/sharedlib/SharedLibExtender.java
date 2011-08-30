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

package org.apache.geronimo.system.sharedlib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.hook.BundleHelper;
import org.apache.geronimo.hook.SharedLibraryRegistry;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.DependencyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@OsgiService
@GBean(j2eeType = "ShareLibExtender")
public class SharedLibExtender implements SynchronousBundleListener, SharedLibraryRegistry, GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(SharedLibExtender.class);

    private DependencyManager dependencyManager;

    private BundleContext bundleContext;

    private ConfigurationManager configurationManager;

    private Map<Artifact, List<Bundle>> configruationSharedLibBundlesMap = new ConcurrentHashMap<Artifact, List<Bundle>>();

    private Map<Long, List<Bundle>> applicationBundleIdSharedLibBundlesMap = new ConcurrentHashMap<Long, List<Bundle>>();

    private final ReentrantLock registerLock = new ReentrantLock();

    public SharedLibExtender(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
            @ParamReference(name = "DependencyManager") DependencyManager dependencyManager,
            @ParamReference(name = "ConfigurationManager", namingType = "ConfigurationManager") ConfigurationManager configurationManager) {
        this.dependencyManager = dependencyManager;
        this.bundleContext = bundleContext;
        this.configurationManager = configurationManager;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            installSharedLibs(event.getBundle());
        } else if (event.getType() == BundleEvent.STOPPED) {
            uninstallSharedLibs(event.getBundle());
        }
    }

    public List<Bundle> getDependentSharedLibBundles(Long applicationId) {
        return applicationBundleIdSharedLibBundlesMap.get(applicationId);
    }

    private void uninstallSharedLibs(Bundle appBundle) {
        unregisterDependentSharedLibBundles(appBundle.getBundleId());
    }

    @Override
    public List<Bundle> registerDependentSharedLibBundles(Long bundleId, List<Bundle> bundles) {
        return applicationBundleIdSharedLibBundlesMap.put(bundleId, bundles);
    }

    @Override
    public List<Bundle> unregisterDependentSharedLibBundles(Long bundleId) {
        return applicationBundleIdSharedLibBundlesMap.remove(bundleId);
    }

    private void installSharedLibs(Bundle appBundle) {
        Artifact artifact = dependencyManager.getArtifact(appBundle.getBundleId());
        if (artifact == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to recongnize the artifact id for the bundle " + appBundle.getSymbolicName());
            }
            return;
        }
        if (!configurationManager.isConfiguration(artifact)) {
            return;
        }
        Configuration configuration = configurationManager.getConfiguration(artifact);
        List<Bundle> dependentSharedLibBundles = new ArrayList<Bundle>();
        for (Artifact parentArtifact : configuration.getDependencyNode().getParents()) {
            List<Bundle> sharedLibBundles = configruationSharedLibBundlesMap.get(parentArtifact);
            if (sharedLibBundles != null) {
                dependentSharedLibBundles.addAll(sharedLibBundles);
            }
        }
        if (dependentSharedLibBundles.size() > 0) {
            registerDependentSharedLibBundles(appBundle.getBundleId(), dependentSharedLibBundles);
        }
    }

    public void registerSharedLibBundle(Artifact artifact, Bundle bundle) {
        registerLock.lock();
        try {
            List<Bundle> shareLibBundles = configruationSharedLibBundlesMap.get(artifact);
            if (shareLibBundles == null) {
                shareLibBundles = new LinkedList<Bundle>();
                configruationSharedLibBundlesMap.put(artifact, shareLibBundles);
            }
            shareLibBundles.add(bundle);
        } finally {
            registerLock.unlock();
        }
    }

    public boolean unregisterSharedLibBundle(Artifact artifact, Bundle bundle) {
        registerLock.lock();
        try {
            List<Bundle> shareLibBundles = configruationSharedLibBundlesMap.get(artifact);
            if (shareLibBundles == null) {
                return false;
            }
            return shareLibBundles.remove(bundle);
        } finally {
            registerLock.unlock();
        }
    }

    @Override
    public void doStart() throws Exception {
        bundleContext.addBundleListener(this);
        BundleHelper.setSharedLibraryRegistry(this);
    }

    @Override
    public void doStop() throws Exception {
        bundleContext.removeBundleListener(this);
        BundleHelper.setSharedLibraryRegistry(null);
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }
}
