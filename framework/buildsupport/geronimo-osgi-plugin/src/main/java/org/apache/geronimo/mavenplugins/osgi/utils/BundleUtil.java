/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.mavenplugins.osgi.utils;

import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
import static org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION;
import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Version;

/**
 * Common functions used by the plugin.
 *
 * @version $Rev$ $Date$
 */
public final class BundleUtil {

    /**
     * Returns the name of a bundle, or null if the given file is not a bundle.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getBundleSymbolicName(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        String bundleName = null;
        if (file.isDirectory()) {
            File mf = new File(file, JarFile.MANIFEST_NAME);
            if (!mf.isFile()) {
                mf = new File(file, "../../" + JarFile.MANIFEST_NAME);
            }
            if (mf.isFile()) {
                Manifest manifest = new Manifest(new FileInputStream(mf));
                bundleName = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLICNAME);
            }
        } else {
            JarFile jar = new JarFile(file, false);
            Manifest manifest = jar.getManifest();
            bundleName = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLICNAME);
            jar.close();
        }
        if (bundleName == null) {
            return bundleName;
        }
        int sc = bundleName.indexOf(';');
        if (sc != -1) {
            bundleName = bundleName.substring(0, sc);
        }
        return bundleName;
    }

    static Manifest getManifest(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        Manifest manifest = null;
        String bundleName = null;
        if (file.isDirectory()) {
            File mf = new File(file, JarFile.MANIFEST_NAME);
            if (!mf.isFile()) {
                mf = new File(file, "../../" + JarFile.MANIFEST_NAME);
            }
            if (mf.isFile()) {
                manifest = new Manifest(new FileInputStream(mf));
                bundleName = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLICNAME);
            }
        } else {
            JarFile jar = new JarFile(file, false);
            manifest = jar.getManifest();
            bundleName = manifest.getMainAttributes().getValue(BUNDLE_SYMBOLICNAME);
            jar.close();
        }
        if (bundleName != null) {
            return manifest;
        }

        if (file.isFile()) {
            Set<File> jars = new HashSet<File>();
            jars.add(file);
            String name = file.getName();
            manifest = libraryManifest(jars, name, name, getOSGiVersion(name), null);
        }
        return manifest;
    }

    /**
     * Generate a Bundle manifest for a set of JAR files.
     *
     * @param jarFiles
     * @param name
     * @param symbolicName
     * @param version
     * @param dir
     * @return
     * @throws IllegalStateException
     */
    static Manifest libraryManifest(Set<File> jarFiles, String name, String symbolicName, String version, String dir)
        throws IllegalStateException {
        try {

            // List exported packages and bundle classpath entries
            StringBuffer classpath = new StringBuffer();
            Set<String> exportedPackages = new HashSet<String>();
            for (File jarFile : jarFiles) {
                addPackages(jarFile, exportedPackages, version);
                if (dir != null) {
                    classpath.append(dir).append("/");
                    classpath.append(jarFile.getName());
                } else {
                    classpath.append("\"external:");
                    classpath.append(jarFile.getPath().replace(File.separatorChar, '/'));
                    classpath.append("\"");
                }
                classpath.append(",");
            }

            // Generate export-package and import-package declarations
            StringBuffer exports = new StringBuffer();
            StringBuffer imports = new StringBuffer();
            Set<String> importedPackages = new HashSet<String>();
            for (String export : exportedPackages) {

                // Add export declaration
                exports.append(export);
                exports.append(',');

                // Add corresponding import declaration
                String packageName = packageName(export);
                if (!importedPackages.contains(packageName)) {
                    importedPackages.add(packageName);
                    imports.append(packageName);
                    imports.append(',');
                }
            }

            // Create a manifest
            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue("Manifest-Version", "1.0");
            attributes.putValue(BUNDLE_MANIFESTVERSION, "2");
            attributes.putValue(BUNDLE_SYMBOLICNAME, symbolicName);
            attributes.putValue(BUNDLE_NAME, name);
            attributes.putValue(BUNDLE_VERSION, version);
            attributes.putValue(DYNAMICIMPORT_PACKAGE, "*");
            if (exports.length() > 1) {
                attributes.putValue(EXPORT_PACKAGE, exports.substring(0, exports.length() - 1));
            }
            if (imports.length() > 1) {
                attributes.putValue(IMPORT_PACKAGE, imports.substring(0, imports.length() - 1));
            }
            if (classpath.length() > 1) {
                attributes.putValue(BUNDLE_CLASSPATH, classpath.substring(0, classpath.length() - 1));
            }

            return manifest;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Write a bundle manifest.
     *
     * @param manifest
     * @param out
     * @throws IOException
     */
    static void write(Manifest manifest, OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        Attributes attributes = manifest.getMainAttributes();
        write(attributes, "Manifest-Version", dos);
        write(attributes, BUNDLE_MANIFESTVERSION, dos);
        write(attributes, BUNDLE_SYMBOLICNAME, dos);
        write(attributes, BUNDLE_NAME, dos);
        write(attributes, BUNDLE_VERSION, dos);
        write(attributes, DYNAMICIMPORT_PACKAGE, dos);
        write(attributes, BUNDLE_CLASSPATH, dos);
        write(attributes, IMPORT_PACKAGE, dos);
        write(attributes, EXPORT_PACKAGE, dos);
        dos.flush();
    }

    /**
     * Add packages to be exported out of a JAR file.
     *
     * @param jarFile
     * @param packages
     * @throws IOException
     */
    private static void addPackages(File jarFile, Set<String> packages, String version) throws IOException {
        if (getBundleSymbolicName(jarFile) == null) {
            String ver = ";version=" + version;
            addAllPackages(jarFile, packages, ver);
        } else {
            addExportedPackages(jarFile, packages);
        }
    }

    /**
     * Write manifest attributes.
     *
     * @param attributes
     * @param key
     * @param dos
     * @throws IOException
     */
    private static void write(Attributes attributes, String key, DataOutputStream dos) throws IOException {
        String value = attributes.getValue(key);
        if (value == null) {
            return;
        }
        StringBuffer line = new StringBuffer();
        line.append(key);
        line.append(": ");
        line.append(new String(value.getBytes("UTF8")));
        line.append("\r\n");
        int l = line.length();
        if (l > 72) {
            for (int i = 70; i < l - 2;) {
                line.insert(i, "\r\n ");
                i += 72;
                l += 3;
            }
        }
        dos.writeBytes(line.toString());
    }

    /**
     * Strip an OSGi export, only retain the package name and version.
     *
     * @param export
     * @return
     */
    private static String stripExport(String export) {
        int sc = export.indexOf(';');
        if (sc == -1) {
            return export;
        }
        String base = export.substring(0, sc);
        int v = export.indexOf("version=");
        if (v != -1) {
            sc = export.indexOf(';', v + 1);
            if (sc != -1) {
                return base + ";" + export.substring(v, sc);
            } else {
                return base + ";" + export.substring(v);
            }
        } else {
            return base;
        }
    }

    /**
     * Add all the packages out of a JAR.
     *
     * @param jarFile
     * @param packages
     * @param version
     * @throws IOException
     */
    private static void addAllPackages(File jarFile, Set<String> packages, String version) throws IOException {
        ZipInputStream is = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (!entry.isDirectory() && entryName != null
                && entryName.length() > 0
                && !entryName.startsWith(".")
                && entryName.endsWith(".class") // Exclude resources from Export-Package
                && entryName.lastIndexOf("/") > 0
                && Character.isJavaIdentifierStart(entryName.charAt(0))) {
                String pkg = entryName.substring(0, entryName.lastIndexOf("/")).replace('/', '.');
                if (!pkg.endsWith(".enum")) {
                    packages.add(pkg + version);
                }
            }
        }
        is.close();
    }

    /**
     * Returns the name of the exported package in the given export.
     * @param export
     * @return
     */
    private static String packageName(String export) {
        int sc = export.indexOf(';');
        if (sc != -1) {
            export = export.substring(0, sc);
        }
        return export;
    }

    /**
     * Add the packages exported by a bundle.
     *
     * @param file
     * @param packages
     * @return
     * @throws IOException
     */
    private static void addExportedPackages(File file, Set<String> packages) throws IOException {
        if (!file.exists()) {
            return;
        }

        // Read the export-package declaration and get a list of the packages available in a JAR
        Set<String> existingPackages = null;
        String exports = null;
        if (file.isDirectory()) {
            File mf = new File(file, "META-INF/MANIFEST.MF");
            if (mf.isFile()) {
                Manifest manifest = new Manifest(new FileInputStream(mf));
                exports = manifest.getMainAttributes().getValue(EXPORT_PACKAGE);
            }
        } else {
            JarFile jar = new JarFile(file, false);
            Manifest manifest = jar.getManifest();
            exports = manifest.getMainAttributes().getValue(EXPORT_PACKAGE);
            jar.close();
            existingPackages = new HashSet<String>();
            addAllPackages(file, existingPackages, "");
        }
        if (exports == null) {
            return;
        }

        // Parse the export-package declaration, and extract the individual packages
        StringBuffer buffer = new StringBuffer();
        boolean q = false;
        for (int i = 0, n = exports.length(); i < n; i++) {
            char c = exports.charAt(i);
            if (c == '\"') {
                q = !q;
            }
            if (!q) {
                if (c == ',') {

                    // Add the exported package to the set, after making sure it really exists in
                    // the JAR
                    String export = buffer.toString();
                    if (existingPackages == null || existingPackages.contains(packageName(export))) {
                        packages.add(stripExport(export));
                    }
                    buffer = new StringBuffer();
                    continue;
                }
            }
            buffer.append(c);
        }
        if (buffer.length() != 0) {

            // Add the exported package to the set, after making sure it really exists in
            // the JAR
            String export = buffer.toString();
            if (existingPackages == null || existingPackages.contains(packageName(export))) {
                packages.add(stripExport(export));
            }
        }
    }

    /**
     * starting with -, then some digits, then . or - or _, then some digits again
     *
     */
    private static Pattern pattern = Pattern.compile("-(\\d)+((\\.|-|_)(\\d)+)*");

    /**
     * Returns the version number to use for the given JAR file.
     *
     * @param fileName
     * @return
     */
    static String getOSGiVersion(String fileName) {
        String name = fileName;
        int index = name.lastIndexOf('.');
        if (index != -1) {
            // Trim the extension
            name = name.substring(0, index);
        }

        Matcher matcher = pattern.matcher(name);
        String version = "0.0.0";
        if (matcher.find()) {
            version = matcher.group();
            version = version.substring(1);

        }
        return osgiVersion(version);
    }

    /**
     * Convert the maven version into OSGi version
     * @param mavenVersion
     * @return
     */
    static String osgiVersion(String mavenVersion) {
        ArtifactVersion ver = new OSGIArtifactVersion(mavenVersion);
        String qualifer = ver.getQualifier();
        if (qualifer != null) {
            StringBuffer buf = new StringBuffer(qualifer);
            for (int i = 0; i < buf.length(); i++) {
                char c = buf.charAt(i);
                if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                    // Keep as-is
                } else {
                    buf.setCharAt(i, '_');
                }
            }
            qualifer = buf.toString();
        }
        Version osgiVersion =
            new Version(ver.getMajorVersion(), ver.getMinorVersion(), ver.getIncrementalVersion(), qualifer);
        String version = osgiVersion.toString();
        return version;
    }

    private static String J2SE = "J2SE-";
    private static String JAVASE = "JavaSE-";
    private static String PROFILE_EXT = ".profile";

    public static void loadVMProfile(Properties properties) {
        Properties profileProps = findVMProfile(properties);
        String systemExports = properties.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES);
        // set the system exports property using the vm profile; only if the property is not already set
        if (systemExports == null) {
            systemExports = profileProps.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES);
            if (systemExports != null)
                properties.put(Constants.FRAMEWORK_SYSTEMPACKAGES, systemExports);
        }
        // set the org.osgi.framework.bootdelegation property according to the java profile
        String type = properties.getProperty(Constants.OSGI_JAVA_PROFILE_BOOTDELEGATION); // a null value means ignore
        String profileBootDelegation = profileProps.getProperty(Constants.FRAMEWORK_BOOTDELEGATION);
        if (Constants.OSGI_BOOTDELEGATION_OVERRIDE.equals(type)) {
            if (profileBootDelegation == null)
                properties.remove(Constants.FRAMEWORK_BOOTDELEGATION); // override with a null value
            else
                properties.put(Constants.FRAMEWORK_BOOTDELEGATION, profileBootDelegation); // override with the profile value
        } else if (Constants.OSGI_BOOTDELEGATION_NONE.equals(type))
            properties.remove(Constants.FRAMEWORK_BOOTDELEGATION); // remove the bootdelegation property in case it was set
        // set the org.osgi.framework.executionenvironment property according to the java profile
        if (properties.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT) == null) {
            // get the ee from the java profile; if no ee is defined then try the java profile name
            String ee =
                profileProps.getProperty(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, profileProps
                    .getProperty(Constants.OSGI_JAVA_PROFILE_NAME));
            if (ee != null)
                properties.put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, ee);
        }
    }

    private static Properties findVMProfile(Properties properties) {
        Properties result = new Properties();
        // Find the VM profile name using J2ME properties
        String j2meConfig = properties.getProperty(Constants.J2ME_MICROEDITION_CONFIGURATION);
        String j2meProfiles = properties.getProperty(Constants.J2ME_MICROEDITION_PROFILES);
        String vmProfile = null;
        String javaEdition = null;
        Version javaVersion = null;
        if (j2meConfig != null && j2meConfig.length() > 0 && j2meProfiles != null && j2meProfiles.length() > 0) {
            // save the vmProfile based off of the config and profile
            // use the last profile; assuming that is the highest one
            String[] j2meProfileList = ManifestElement.getArrayFromList(j2meProfiles, " "); //$NON-NLS-1$
            if (j2meProfileList != null && j2meProfileList.length > 0)
                vmProfile = j2meConfig + '_' + j2meProfileList[j2meProfileList.length - 1];
        } else {
            // No J2ME properties; use J2SE properties
            // Note that the CDC spec appears not to require VM implementations to set the
            // javax.microedition properties!!  So we will try to fall back to the 
            // java.specification.name property, but this is pretty ridiculous!!
            String javaSpecVersion = properties.getProperty("java.specification.version"); //$NON-NLS-1$
            // set the profile and EE based off of the java.specification.version
            // TODO We assume J2ME Foundation and J2SE here.  need to support other profiles J2EE ...
            if (javaSpecVersion != null) {
                StringTokenizer st = new StringTokenizer(javaSpecVersion, " _-"); //$NON-NLS-1$
                javaSpecVersion = st.nextToken();
                String javaSpecName = properties.getProperty("java.specification.name"); //$NON-NLS-1$
                if ("J2ME Foundation Specification".equals(javaSpecName)) //$NON-NLS-1$
                    vmProfile = "CDC-" + javaSpecVersion + "_Foundation-" + javaSpecVersion; //$NON-NLS-1$ //$NON-NLS-2$
                else {
                    // look for JavaSE if 1.6 or greater; otherwise look for J2SE
                    Version v16 = new Version("1.6"); //$NON-NLS-1$
                    javaEdition = J2SE;
                    try {
                        javaVersion = new Version(javaSpecVersion);
                        if (v16.compareTo(javaVersion) <= 0)
                            javaEdition = JAVASE;
                    } catch (IllegalArgumentException e) {
                        // do nothing
                    }
                    vmProfile = javaEdition + javaSpecVersion;
                }
            }
        }
        URL url = null;
        // check for the java profile property for a url
        String propJavaProfile = FrameworkProperties.getProperty(Constants.OSGI_JAVA_PROFILE);
        if (propJavaProfile != null)
            try {
                // we assume a URL
                url = new URL(propJavaProfile);
            } catch (MalformedURLException e1) {
                // try using a relative path in the system bundle
                url = findInSystemBundle(propJavaProfile);
            }
        if (url == null && vmProfile != null) {
            // look for a profile in the system bundle based on the vm profile
            String javaProfile = vmProfile + PROFILE_EXT;
            url = findInSystemBundle(javaProfile);
            if (url == null)
                url = getNextBestProfile(javaEdition, javaVersion);
        }
        if (url == null)
            // the profile url is still null then use the osgi min profile in OSGi by default
            url = findInSystemBundle("OSGi_Minimum-1.2.profile"); //$NON-NLS-1$
        if (url != null) {
            InputStream in = null;
            try {
                in = url.openStream();
                result.load(new BufferedInputStream(in));
            } catch (IOException e) {
                // TODO consider logging ...
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException ee) {
                        // do nothing
                    }
            }
        }
        // set the profile name if it does not provide one
        if (result.getProperty(Constants.OSGI_JAVA_PROFILE_NAME) == null)
            if (vmProfile != null)
                result.put(Constants.OSGI_JAVA_PROFILE_NAME, vmProfile.replace('_', '/'));
            else
                // last resort; default to the absolute minimum profile name for the framework
                result.put(Constants.OSGI_JAVA_PROFILE_NAME, "OSGi/Minimum-1.2"); //$NON-NLS-1$
        return result;
    }

    private static URL getNextBestProfile(String javaEdition, Version javaVersion) {
        if (javaVersion == null || (javaEdition != J2SE && javaEdition != JAVASE))
            return null; // we cannot automatically choose the next best profile unless this is a J2SE or JavaSE vm
        URL bestProfile = findNextBestProfile(javaEdition, javaVersion);
        if (bestProfile == null && javaEdition == JAVASE)
            // if this is a JavaSE VM then search for a lower J2SE profile
            bestProfile = findNextBestProfile(J2SE, javaVersion);
        return bestProfile;
    }

    private static URL findNextBestProfile(String javaEdition, Version javaVersion) {
        URL result = null;
        int minor = javaVersion.getMinor();
        do {
            result = findInSystemBundle(javaEdition + javaVersion.getMajor() + "." + minor + PROFILE_EXT); //$NON-NLS-1$
            minor = minor - 1;
        } while (result == null && minor > 0);
        return result;
    }

    private static URL findInSystemBundle(String entry) {
        ClassLoader loader = BundleUtil.class.getClassLoader();
        return loader == null ? ClassLoader.getSystemResource(entry) : loader.getResource(entry);
    }

}
