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
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.osgi.BundleDescription;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.obr.model.Repository;
import org.apache.geronimo.obr.model.Resource;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.obr.RepositoryAdmin;

@GBean
public class GeronimoOBRGBean implements GBeanLifecycle {
    
    private BundleContext bundleContext;
    private ListableRepository repository;
    private File obrFile;
    private List<URL> repositories;
    
    public GeronimoOBRGBean(@ParamReference(name = "Repository", namingType = "Repository") ListableRepository repository,
                            @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext,
                            @ParamAttribute(name = "repositoryList") String repositoryList) throws Exception {
        this.repository = repository;         
        this.bundleContext = bundleContext;
        this.obrFile = serverInfo.resolveServer("var/obr.xml");
        this.repositories = parseRepositories(repositoryList);
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
    
    private void generateOBR() throws Exception {
        
        String obrName = "Geronimo OBR Repository";

        org.apache.geronimo.kernel.repository.Repository geronimoRepository = repository;
        Set<Artifact> artifacts = repository.list();
        generateOBR(obrName, artifacts, geronimoRepository, obrFile);
    }

    public static void generateOBR(String obrName, Set<Artifact> artifacts, org.apache.geronimo.kernel.repository.Repository geronimoRepository, File obrFile) throws IOException, JAXBException {
        Repository repo = generateOBRModel(obrName, geronimoRepository, artifacts);
        marshallOBRModel(repo, obrFile);
    }

    public static void marshallOBRModel(Repository repo, File obrFile) throws JAXBException {
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

            Resource resource = builder.createResource();
            if (resource != null) {
                resource.setUri(getURL(artifact));
                if (location.isFile()) {
                    resource.setSize(location.length());
                }
                repo.getResource().add(resource);
            } else {
                System.out.println("Warning: Artifact " + artifact + " is not a bundle.");
            }
        }
        return repo;
    }

    private static String getURL(Artifact artifact) {
        return "mvn:" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" + artifact.getVersion() + ("jar".equals(artifact.getType())?  "": "/" + artifact.getType());
    }

    private void registerOBR() throws Exception {
        ServiceReference ref = bundleContext.getServiceReference(RepositoryAdmin.class.getName());
        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) bundleContext.getService(ref);
        
        repositoryAdmin.addRepository(obrFile.toURI().toURL());
        
        for (URL repository : repositories) {
            repositoryAdmin.addRepository(repository);
        }
        
        bundleContext.ungetService(ref); 
    }
    
    public void doStart() throws Exception {
        generateOBR();
        registerOBR();
    }
    
    public void doStop() throws Exception {
    }
   
    public void doFail() {
    }
}
