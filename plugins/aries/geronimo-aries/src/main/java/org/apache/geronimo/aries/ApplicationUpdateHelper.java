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
package org.apache.geronimo.aries;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.transformer.TransformerAgent;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.apache.xbean.osgi.bundle.util.HeaderParser;
import org.apache.xbean.osgi.bundle.util.HeaderParser.HeaderElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
public class ApplicationUpdateHelper {
        
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationUpdateHelper.class);
    
    private final ApplicationGBean applicationGBean;
    
    public ApplicationUpdateHelper(ApplicationGBean applicationGBean) {
        this.applicationGBean = applicationGBean;
    }
    
    public void updateBundle(Bundle targetBundle, File bundleFile) throws Exception {
        
        String applicationName = applicationGBean.getApplicationName();
        String bundleName = targetBundle.getSymbolicName();
        
        LOG.info("Updating {} bundle in {} application", bundleName, applicationName);
                
        BundleContext context = applicationGBean.getBundle().getBundleContext();

        ServiceReference reference = null;
        RefreshListener refreshListener = null;
        try {
            // stop the bundle
            targetBundle.stop();

            // update the bundle
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(bundleFile);
                targetBundle.update(fi);
            } finally {
                close(fi);
            }

            reference = context.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) context.getService(reference);
            
            Bundle[] bundles = new Bundle [] { targetBundle };
            // resolve the bundle
            if (!packageAdmin.resolveBundles(bundles)) {
                StringBuilder builder = new StringBuilder();
                builder.append("Updated ").append(bundleName).append(" bundle cannot be resolved.");
                
                // check for resolver errors
                ResolverErrorAnalyzer errorAnalyzer = new ResolverErrorAnalyzer(context);
                String resolverErrors = errorAnalyzer.getErrorsAsString(Arrays.asList(bundles));
                if (resolverErrors != null) {
                    builder.append(" ").append(resolverErrors);
                }
                
                throw new BundleException(builder.toString());
            }
            
            Set<Bundle> dependents = new HashSet<Bundle>();
            collectDependentBundles(packageAdmin, dependents, targetBundle);
            if (!dependents.isEmpty()) {
                String bundleListString = bundleCollectionToString(dependents);
                LOG.info("Update of {} bundle will cause the following bundles to be refreshed: {}", bundleName, bundleListString);
            }
            
            // install listener for package refresh
            refreshListener = new RefreshListener();
            context.addFrameworkListener(refreshListener);

            // refresh the bundle - this happens asynchronously
            packageAdmin.refreshPackages(bundles);

            // update application archive
            try {
                updateArchive(targetBundle, bundleFile);
            } catch (Exception e) {
                LOG.warn("Error updating application archive with the new contents. " +
                         "Changes made might be gone next time the application or server is restarted.", e.getMessage());
            }

            // wait for package refresh to finish
            refreshListener.waitForRefresh(10 * 1000);

            // start the bundle
            if (BundleUtils.canStart(targetBundle)) {
                targetBundle.start(Bundle.START_TRANSIENT);
            }
            
            LOG.info("Bundle {} was successfully updated in {} application", bundleName, applicationName);
            
        } catch (Exception e) {
            LOG.error("Error updating " + bundleName + " bundle in " + applicationName + " application", e);
            throw new Exception("Error updating application: " + e.getMessage());
        } finally {
            if (refreshListener != null) {
                context.removeFrameworkListener(refreshListener);
            }
            if (reference != null) {
                context.ungetService(reference);
            }
        }
    }
    
    public boolean updateBundleClasses(Bundle targetBundle, File changesFile, boolean updateArchive) throws Exception {
        if (!TransformerAgent.isRedefineClassesSupported()) {
            return false;
        }
                
        if (!hotSwapClasses(targetBundle, changesFile)) {
            return false;
        }
        
        if (updateArchive) {
            // create updated bundle jar & update archive
            File updatedBundleFile = null;
            try {
                updatedBundleFile = createUpdatedJarFile(targetBundle, changesFile);
                updateArchive(targetBundle, updatedBundleFile);
            } catch (Exception e) {
                LOG.warn("Error updating application archive with the new contents. " +
                         "Changes made might be gone next time the application or server is restarted.", e.getMessage());
            } finally {
                deleteFile(updatedBundleFile);
            }
        }
        
        return true;
    }
    
    private boolean hotSwapClasses(Bundle bundle, File changesFile) {
        String bundleName = bundle.getSymbolicName();
        
        LOG.info("Checking if class hot swap can be performed for {} bundle", bundleName);
        
        String[] classPath = getBundleClassPath(bundle);
        List<ClassDefinition> classDefinitionList = new ArrayList<ClassDefinition>();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(changesFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    throw new IllegalStateException("Non class file found: " + name);
                }
                
                // compute & load existing class
                Class<?> clazz = loadClass(bundle, classPath, name);
                
                // get the new class bytes
                InputStream in = zipFile.getInputStream(entry);
                byte[] clazzBytes = IOUtils.getBytes(in);
                
                classDefinitionList.add(new ClassDefinition(clazz, clazzBytes));
            }
        } catch (Exception e) {
            LOG.info("Hot swap cannot be performed for {} bundle: {}", bundleName, e.getMessage());
            LOG.debug("Hot swap cannot be performed for " + bundleName + " bundle", e);
            return false;
        } finally {
            close(zipFile);
        }
        
        try {
            LOG.info("Attempting to hot swap the following classes for {} bundle: {}", bundleName, classDefinitionToString(classDefinitionList));
            ClassDefinition[] classDefinitions = new ClassDefinition[classDefinitionList.size()];
            classDefinitionList.toArray(classDefinitions);
            TransformerAgent.redefine(classDefinitions);
            LOG.info("Hot swap was successfully performed for {} bundle", bundleName);
            return true;
        } catch (Exception e) {
            LOG.info("Unable to perform class hot swap for {} bundle: {}", bundleName, e.getMessage());
            LOG.debug("Error performing hot swap for " + bundleName + " bundle", e);
            return false;
        }
    }
    
    private String[] getBundleClassPath(Bundle bundle) {
        String classPathHeader = bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
        if (classPathHeader == null) {
            return null;
        }
        List<HeaderElement> classPathElements = HeaderParser.parseHeader(classPathHeader);
        String[] classPath = new String[classPathElements.size()];
        for (int i = 0; i < classPathElements.size(); i++) {
            String classPathName = classPathElements.get(i).getName();
            if (classPathName.equals(".") || classPathName.equals("/")) {
                classPathName = "";
            } else if (classPathName.endsWith("/")) {
                classPathName = classPathName.substring(0, classPathName.length() - 1);
            }
            /*
             * Could check & ignore jar class path entries but treat them as directory
             * entries for now. 
             */
            classPath[i] = classPathName;
        }
        return classPath;
    }
    
    private Class<?> loadClass(Bundle bundle, String[] classPath, String classEntryName) throws ClassNotFoundException {
        if (classPath == null) {
            String className = getClassName("", classEntryName);
            return bundle.loadClass(className);
        } else {
            List<String> classes = null;
            for (String classPathEntry : classPath) {
                String className = getClassName(classPathEntry, classEntryName);
                LOG.debug("Attempting to load {} ", className);
                try {
                    return bundle.loadClass(className);
                } catch (Throwable t) {
                    LOG.debug("Failed to load " + className, t);
                    // keep trying
                    if (classes == null) {
                        classes =  new ArrayList<String>(classPath.length);
                    }
                    classes.add(className);
                }
            }
            throw new ClassNotFoundException(classes.toString());
        }
    }
        
    private String getClassName(String classPathEntry, String classEntryName) {
        if (classPathEntry.length() > 0 && classEntryName.startsWith(classPathEntry)) {
            classEntryName = classEntryName.substring(classPathEntry.length() + 1);            
        }        
        return classEntryName.substring(0, classEntryName.length() - ".class".length()).replace('/', '.');
    }
    
    private class RefreshListener implements FrameworkListener {

        public CountDownLatch latch = new CountDownLatch(1);

        public void frameworkEvent(FrameworkEvent event) {
            if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                latch.countDown();
            }
        }

        public void waitForRefresh(int timeout) {
            try {
                latch.await(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void collectDependentBundles(PackageAdmin packageAdmin, Set<Bundle> dependents, Bundle bundle) {
        ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(bundle);
        if (exportedPackages != null) {
            for (ExportedPackage exportedPackage : exportedPackages) {
                Bundle[] importingBundles = exportedPackage.getImportingBundles();
                if (importingBundles != null) {
                    for (Bundle importingBundle : importingBundles) {
                        if (!dependents.contains(importingBundle)) {
                            dependents.add(importingBundle);
                            collectDependentBundles(packageAdmin, dependents, importingBundle);
                        }
                    }
                }
            }
        }
    }
    
    private static String bundleCollectionToString(Collection<Bundle> bundles) {
        StringBuilder builder = new StringBuilder();
        Iterator<Bundle> iterator = bundles.iterator();
        while(iterator.hasNext()) {
            Bundle bundle = iterator.next();
            builder.append(bundle.getSymbolicName());
            builder.append(" [").append(bundle.getBundleId()).append("]");
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
       return builder.toString();
    }
    
    private static String classDefinitionToString(Collection<ClassDefinition> definitions) {
        StringBuilder builder = new StringBuilder();
        Iterator<ClassDefinition> iterator = definitions.iterator();
        while(iterator.hasNext()) {
            ClassDefinition definition = iterator.next();
            builder.append(definition.getDefinitionClass().getName());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
       return builder.toString();
    }
    
    private File createUpdatedJarFile(Bundle targetBundle, File changesFile) throws IOException {
        File ebaArchive = applicationGBean.getApplicationArchive();
        
        String bundleNameInApp = getBundleNameInArchive(targetBundle);
        
        ZipFile zipFile = null;
        File updatedFile = null;
        try {
            zipFile = new ZipFile(ebaArchive);
            ZipEntry entry = zipFile.getEntry(bundleNameInApp);
            if (entry == null) {
                throw new IOException("Unable to locate " + bundleNameInApp + " in " + ebaArchive);
            }           
            updatedFile = File.createTempFile(bundleNameInApp, ".jar");
            JarInputStream jarIn = new JarInputStream(zipFile.getInputStream(entry));
            updateJarFile(jarIn, changesFile, updatedFile);            
        } catch (IOException e) {
            deleteFile(updatedFile);
            throw e;
        } finally {
            close(zipFile);
        }
        
        return updatedFile;
    }
    
    private static void updateJarFile(JarInputStream jarIn, File changesFile, File outputFile) throws IOException {
        Map<String, ZipEntry> updatedEntries = new HashMap<String, ZipEntry>();
        ZipFile zipFile = null;
        JarOutputStream jarOut = null;
        try {
            // build map of updated entries
            zipFile = new ZipFile(changesFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                updatedEntries.put(entry.getName(), entry);
            }
            
            // get manifest
            Manifest manifest = jarIn.getManifest();
            ZipEntry manifestEntry = updatedEntries.remove(JarFile.MANIFEST_NAME);
            if (manifestEntry != null) {
                manifest = new Manifest(zipFile.getInputStream(manifestEntry));
            }
            
            // update the contents of the jar file 
            jarOut = new JarOutputStream(new FileOutputStream(outputFile), manifest);
            JarEntry entry = null;
            byte[] buffer = new byte[4096];
            while ( (entry = jarIn.getNextJarEntry()) != null) {
                ZipEntry source = updatedEntries.remove(entry.getName());
                if (source == null) {
                    copyZipEntry(jarOut, entry, jarIn, buffer);
                } else {
                    InputStream in = zipFile.getInputStream(source);
                    try {
                        copyZipEntry(jarOut, source, in, buffer);
                    } finally {
                        close(in);
                    }
                }
            }

            // if updatedEntires is not empty now - we have a problem
            if (!updatedEntries.isEmpty()) {
                throw new IOException("Some jar entries were not updated: " + updatedEntries.keySet());
            }
        } finally {
            close(jarIn);
            close(zipFile);
            close(jarOut);
        }
    }
    
    private void updateArchive(Bundle bundle, File bundleFile) throws IOException {
        File ebaArchive = applicationGBean.getApplicationArchive();

        File newEbaArchive = new File(ebaArchive.getAbsoluteFile() + ".new");

        String bundleNameInApp = getBundleNameInArchive(bundle);

        LOG.debug("Updating {} application archive with new contents for {}", ebaArchive, bundleNameInApp);

        ZipFile oldZipFile = null;
        ZipOutputStream newZipFile = null;
        try {
            newZipFile = new ZipOutputStream(new FileOutputStream(newEbaArchive));
            oldZipFile = new ZipFile(ebaArchive);
            Enumeration<? extends ZipEntry> entries = oldZipFile.entries();
            byte[] buffer = new byte[4096];
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                InputStream in = null;
                if (entry.getName().equals(bundleNameInApp)) {
                    in = new FileInputStream(bundleFile);
                    LOG.debug("Updating contents of {} with {}", bundleNameInApp, bundleFile.getAbsolutePath());
                } else {
                    in = oldZipFile.getInputStream(entry);
                }
                try {
                    copyZipEntry(newZipFile, new ZipEntry(entry.getName()), in, buffer);
                } finally {
                    close(in);
                }
            }
        } catch (IOException e) {
            LOG.debug("Error updating application archive", e);
            newEbaArchive.delete();
            throw e;
        } finally {
            close(oldZipFile);
            close(newZipFile);
        }

        if (ebaArchive.delete()) {
            if (!newEbaArchive.renameTo(ebaArchive)) {
                throw new IOException("Error renaming application archive");
            } else {
                LOG.debug("Application archive was successfully updated.");
            }
        } else {
            throw new IOException("Error deleting existing application archive");
        }
    }

    private static void copyZipEntry(ZipOutputStream zipFile, ZipEntry entry, InputStream in, byte[] buffer) throws IOException {
        zipFile.putNextEntry(entry);
        try {
            int count;
            while ((count = in.read(buffer)) > 0) {
                zipFile.write(buffer, 0, count);
            }
        } finally {
            zipFile.closeEntry();
        }
    }
    
    private static String getBundleNameInArchive(Bundle bundle) {
        URI bundleLocation = URI.create(bundle.getLocation());
        String bundleNameInApp = bundleLocation.getPath();
        if (bundleNameInApp.startsWith("/")) {
            bundleNameInApp = bundleNameInApp.substring(1);
        }
        return bundleNameInApp;
    }
    

    private static void close(ZipFile thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }
    
    private static void close(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }
    
    private static void deleteFile(File file) {
        if (file != null) {
            file.delete();
        }
    }
}
