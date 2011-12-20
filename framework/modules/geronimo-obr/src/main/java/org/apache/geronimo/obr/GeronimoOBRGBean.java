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

package org.apache.geronimo.obr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.xbean.osgi.bundle.util.BundleDescription;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.obr.model.Repository;
import org.apache.geronimo.obr.model.Resource;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GBean
@OsgiService
public class GeronimoOBRGBean implements GBeanLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(GeronimoOBRGBean.class);

    private BundleContext bundleContext;
    private ListableRepository repository;
    private File obrFile;
    private List<URL> repositories;
    private Set<Artifact> exclusions;

    public GeronimoOBRGBean(@ParamReference(name = "Repository", namingType = "Repository") ListableRepository repository,
                            @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                            @ParamAttribute(name = "repositoryList") String repositoryList,
                            @ParamAttribute(name = "exclusions") String exclusions) throws Exception {
        this.repository = repository;
        this.bundleContext = bundleContext;
        this.obrFile = serverInfo.resolveServer("var/obr.xml");
        this.repositories = parseRepositories(repositoryList);
        this.exclusions = parseExclusions(exclusions);
    }

    private static List<URL> parseRepositories(String repositoryList) throws MalformedURLException {
        List<URL> list = new ArrayList<URL>();
        if (repositoryList != null) {
            StringTokenizer tokenizer = new StringTokenizer(repositoryList, ",");
            while (tokenizer.hasMoreElements()) {
                String token = (String) tokenizer.nextElement();
                list.add(new URL(token.trim()));
            }
        }
        return list;
    }

    private static Set<Artifact> parseExclusions(String exclusions) throws MalformedURLException {
        Set<Artifact> set = new HashSet<Artifact>();
        if (exclusions != null) {
            StringTokenizer tokenizer = new StringTokenizer(exclusions, ",");
            while (tokenizer.hasMoreElements()) {
                String token = (String) tokenizer.nextElement();
                set.add(Artifact.create(token.trim()));
            }
        }
        return set;
    }

    public void refresh() throws Exception {
        generateRepository();

        ServiceReference ref = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(ref);
        try {
            repositoryAdmin.removeRepository(obrFile.toURI().toURL().toExternalForm());
            repositoryAdmin.addRepository(obrFile.toURI().toURL());
        } finally {
            bundleContext.ungetService(ref);
        }
    }

    private void generateRepository() throws Exception {

        String obrName = "Geronimo OBR Repository";

        org.apache.geronimo.kernel.repository.Repository geronimoRepository = repository;
        Set<Artifact> artifacts = repository.list();

        // prune excluded artifacts
        for (Artifact excluded : exclusions) {
            Iterator<Artifact> iterator = artifacts.iterator();
            while (iterator.hasNext()) {
                Artifact artifact = iterator.next();
                if (excluded.matches(artifact)) {
                    LOG.debug("Exluded {} artifact from OBR", artifact);
                    iterator.remove();
                }
            }
        }

        generateOBR(obrName, artifacts, geronimoRepository, obrFile);
    }

    public static void generateOBR(String obrName, Set<Artifact> artifacts, org.apache.geronimo.kernel.repository.Repository geronimoRepository, File obrFile) throws IOException, JAXBException {
        Repository repo = generateOBRModel(obrName, geronimoRepository, artifacts);
        marshallOBRModel(repo, obrFile);
    }

    public static void marshallOBRModel(Repository repo, File obrFile) throws JAXBException {
        if (!obrFile.getParentFile().exists()) {
            obrFile.getParentFile().mkdirs();
        }
        JAXBContext context = JAXBContext.newInstance(Repository.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(repo, obrFile);
    }

    public static Repository generateOBRModel(String obrName, org.apache.geronimo.kernel.repository.Repository geronimoRepository, Set<Artifact> artifacts) throws IOException {
        Repository repo = new Repository();
        repo.setName(obrName);
        for (Artifact artifact : artifacts) {
            File location = geronimoRepository.getLocation(artifact);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Generating OBR Metadata for " + location.getAbsolutePath());
            }
            Manifest mf = null;
            if (location.isFile()) {
                JarFile file = new JarFile(location);
                try {
                    mf = file.getManifest();
                } finally {
                    try { file.close(); } catch (IOException ignore) {}
                }

            } else if (location.isDirectory()) {
                File mfFile = new File(location, JarFile.MANIFEST_NAME);
                FileInputStream in = new FileInputStream(mfFile);
                try {
                    mf = new Manifest(in);
                } finally {
                    try { in.close(); } catch (IOException ignore) {}
                }
            } else {
                continue;
            }

            if (mf == null) {
                continue;
            }

            BundleDescription desc = new BundleDescription(mf);
            ResourceBuilder builder = new ResourceBuilder(desc);

            Resource resource = null;
            try {
                resource = builder.createResource();
            } catch (RuntimeException e) {
                LOG.debug("Failed to generate OBR information for " + artifact + " artifact", e);
                continue;
            }

            if (resource != null) {
                resource.setUri(getURL(artifact));
                if (location.isFile()) {
                    resource.setSize(location.length());
                }
                repo.getResource().add(resource);
            } else {
                LOG.debug("Did not generate OBR information for {} artifact. It is not a bundle.", artifact);
            }
        }
        return repo;
    }

    private static String getURL(Artifact artifact) {
        return "mvn:" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + ("jar".equals(artifact.getType())?  "": "/" + artifact.getType());
    }

    private void registerRepositories() throws Exception {
        ServiceReference ref = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(ref);
        try {
            repositoryAdmin.addRepository(obrFile.toURI().toURL());
            for (URL repository : repositories) {
                repositoryAdmin.addRepository(repository);
            }
        } finally {
            bundleContext.ungetService(ref);
        }
    }

    private void unregisterRepositories() throws Exception {
        ServiceReference ref = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(ref);
        try {
            repositoryAdmin.removeRepository(obrFile.toURI().toURL().toExternalForm());
            for (URL repository : repositories) {
                repositoryAdmin.removeRepository(repository.toExternalForm());
            }
        } finally {
            bundleContext.ungetService(ref);
        }
    }

    public void doStart() throws Exception {
        generateRepository();
        registerRepositories();
    }

    public void doStop() throws Exception {
        unregisterRepositories();
    }

    public void doFail() {
    }
}
