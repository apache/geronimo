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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.config.Manifest;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.DelegatingBundle;
import org.apache.xbean.osgi.bundle.util.HeaderBuilder;
import org.apache.xbean.osgi.bundle.util.HeaderParser;
import org.apache.xbean.osgi.bundle.util.HeaderParser.HeaderElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class SharedLib implements GBeanLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(SharedLib.class);

    private File baseFolder;

    private String[] libDirs;

    private String[] classesDirs;

    private ServerInfo serverInfo;

    private BundleContext bundleContext;

    private AbstractName name;

    private Bundle sharedLibBundle;

    public SharedLib(@ParamAttribute(name = "baseDir") String baseDir,
            @ParamAttribute(name = "classesDirs") String[] classesDirs,
            @ParamAttribute(name = "libDirs") String[] libDirs,
            @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName name) {
        if (baseDir == null) {
            throw new IllegalArgumentException("baseDir is required to configured for the sharedLib, and the values of classesDirs and libDirs should be relative to the baseDir");
        }
        baseFolder = serverInfo.resolveServer(baseDir);
        if (!baseFolder.exists()) {
            if (!baseFolder.mkdirs()) {
                throw new IllegalArgumentException("Failed to create classes dir: " + baseDir);
            }
        }
        if (classesDirs == null && libDirs == null) {
            throw new IllegalArgumentException("At least, one of the attributes classesDirs and libDirs is required to configured.");
        }
        this.classesDirs = classesDirs;
        this.libDirs = libDirs;
        this.serverInfo = serverInfo;
        this.bundleContext = bundleContext;
        this.name = name;
    }

    @Override
    public void doStart() throws Exception {
        String bundleLocation = "reference:" + baseFolder.toURI().toURL();
        //1. Double check the share library bundle is not installed
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getLocation().equals(bundleLocation)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(" Share library bundle is found installed, it might be caused by the server was not shutdown correctly last time, it will be reinstalled");
                }
                b.uninstall();
                break;
            }
        }
        //2. Generate the MANIFEST.MF file for the share library
        Manifest manifest = new Manifest();
        manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_MANIFESTVERSION, "2"));
        Artifact configId = name.getArtifact();
        manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_SYMBOLICNAME, configId.getGroupId() + "." + configId.getArtifactId() + "." + name.getNameProperty("name")));
        String versionString = "" + configId.getVersion().getMajorVersion() + "." + configId.getVersion().getMinorVersion() + "." + configId.getVersion().getIncrementalVersion();
        if (configId.getVersion().getQualifier() != null) {
            versionString += "." + configId.getVersion().getQualifier().replaceAll("[^-_\\w]{1}", "_");
        }
        manifest.addConfiguredAttribute(new Manifest.Attribute(Constants.BUNDLE_VERSION, versionString));
        Set<String> bundleClassPaths = generateBundleClassPath();
        if (bundleClassPaths.size() > 0) {
            Manifest.Attribute bundleClassPath = new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.BUNDLE_CLASSPATH, bundleClassPaths);
            manifest.addConfiguredAttribute(bundleClassPath);
        }
        //import packages, dynamic import packages and required bundles are from the configuration bundle.
        String importPackages = (String)bundleContext.getBundle().getHeaders().get(Constants.IMPORT_PACKAGE);
        if (importPackages != null) {
            manifest.addConfiguredAttribute(new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.IMPORT_PACKAGE, importPackages));
        }
        String dynamicImportPackages = (String)bundleContext.getBundle().getHeaders().get(Constants.DYNAMICIMPORT_PACKAGE);
        if (dynamicImportPackages != null) {
            List<HeaderElement> headerElements = HeaderParser.parseHeader(dynamicImportPackages);
            if (headerElements.size() > 0) {
                manifest.addConfiguredAttribute(new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.DYNAMICIMPORT_PACKAGE, HeaderBuilder.build(headerElements)));
            }
        }
        String requiredBundles = (String)bundleContext.getBundle().getHeaders().get(Constants.REQUIRE_BUNDLE);
        if (requiredBundles != null) {
            manifest.addConfiguredAttribute(new Manifest.Attribute(Manifest.Attribute.Separator.COMMA, Constants.REQUIRE_BUNDLE, requiredBundles));
        }
        //3. Write the MANIFEST.MF file
        File metaInf = new File(baseFolder, "META-INF");
        metaInf.mkdirs();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(new File(metaInf, "MANIFEST.MF")));
            manifest.write(pw);
        } finally {
            IOUtils.close(pw);
        }
        //4. Install the bundle
        sharedLibBundle = bundleContext.installBundle(bundleLocation);
        if (BundleUtils.isResolved(sharedLibBundle)) {
            BundleUtils.resolve(sharedLibBundle);
        }
        //5. register the shared library bundle to the shared library bundle extender
        ServiceReference<?> sharedLibExtenderReference = null;
        try {
            sharedLibExtenderReference = bundleContext.getServiceReference(SharedLibExtender.class.getName());
            if (sharedLibExtenderReference != null) {
                SharedLibExtender shareLibExtender = (SharedLibExtender) bundleContext.getService(sharedLibExtenderReference);
                shareLibExtender.registerSharedLibBundle(name.getArtifact(), sharedLibBundle);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to register the share lib bundle " + sharedLibBundle.getSymbolicName() + " in the ShareLibExtender");
                }
            }
        } finally {
            if (sharedLibExtenderReference != null) {
                try {
                    bundleContext.ungetService(sharedLibExtenderReference);
                } catch (Exception e) {
                }
            }
        }
    }

    protected Set<String> generateBundleClassPath() {
        Set<String> bundleClassPaths = new LinkedHashSet<String>();
        if (classesDirs != null) {
            for (int i = 0; i < classesDirs.length; i++) {
                String classesDir = classesDirs[i];
                File dir = new File(baseFolder, classesDir);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        throw new IllegalArgumentException("Failed to create classes dir: " + dir);
                    }
                }
                if (!dir.isDirectory()) {
                    throw new IllegalArgumentException("Shared classes dir is not a directory: " + dir);
                }
                if (classesDir.startsWith("/")) {
                    classesDir = classesDir.length() == 1 ? "." : classesDir.substring(1);
                }
                bundleClassPaths.add(classesDir);
            }
        }
        if (libDirs != null) {
            for (int i = 0; i < libDirs.length; i++) {
                String libDir = libDirs[i];
                File dir = new File(baseFolder, libDir);
                if (!dir.exists()) {
                    logger.warn("share library directory " + libDir + " is not found, it will be ingored");
                    break;
                }
                if (!dir.isDirectory()) {
                    throw new IllegalArgumentException("Shared lib dir is not a directory: " + dir);
                }

                File[] files = dir.listFiles();
                for (int j = 0; j < files.length; j++) {
                    File file = files[j];
                    if (file.canRead() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
                        if (libDir.startsWith("/")) {
                            libDir = libDir.length() == 1 ? "" : libDir.substring(1);
                        }
                        if (!libDir.endsWith("/")) {
                            libDir = libDir + "/";
                        }
                        bundleClassPaths.add(libDir + file.getName());
                    }
                }
            }
        }
        return bundleClassPaths;
    }

    @Override
    public void doStop() throws Exception {
        if (sharedLibBundle == null) {
            return;
        }
        //1. remove the bundle from the parent DelegatingBundle
        Bundle configurationBundle = bundleContext.getBundle();
        if (configurationBundle instanceof DelegatingBundle) {
            DelegatingBundle delegatingBundle = (DelegatingBundle) configurationBundle;
            delegatingBundle.removeBundle(sharedLibBundle);
        }
        //2. unregister the share lib bundle to the share lib bundle extender
        ServiceReference<?> sharedLibExtenderReference = null;
        try {
            sharedLibExtenderReference = bundleContext.getServiceReference(SharedLibExtender.class.getName());
            if (sharedLibExtenderReference != null) {
                SharedLibExtender sharedLibExtender = (SharedLibExtender) bundleContext.getService(sharedLibExtenderReference);
                sharedLibExtender.unregisterSharedLibBundle(name.getArtifact(), sharedLibBundle);
            }
        } finally {
            if (sharedLibExtenderReference != null) {
                try {
                    bundleContext.ungetService(sharedLibExtenderReference);
                } catch (Exception e) {
                }
            }
        }
        //3. uninstall the share library bundle
        if (sharedLibBundle != null) {
            try {
                sharedLibBundle.uninstall();
            } catch (Exception e) {
                //Sometimes, it is unable to uninstall the shared library bundle, as it is in the shutdown progress.
            }
        }
    }

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }
}
