/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.aries.resolver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.Content;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationResolver;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.management.ResolveConstraint;
import org.apache.aries.application.management.ResolverException;
import org.apache.aries.application.utils.manifest.ManifestHeaderProcessor;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.apache.geronimo.aries.resolver.generator.RepositoryDescriptorGenerator;
import org.apache.geronimo.aries.resolver.impl.ApplicationResourceImpl;
import org.apache.geronimo.aries.resolver.impl.OBRBundleInfo;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoOBRResolver implements AriesApplicationResolver {
    private static Logger log = LoggerFactory.getLogger(GeronimoOBRResolver.class);

    private final RepositoryAdmin repositoryAdmin;
    private boolean includeOptional = true;

    public GeronimoOBRResolver(RepositoryAdmin repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
    }

    public void setIncludeOptional(boolean includeOptional) {
        this.includeOptional = includeOptional;
    }

    public Set<BundleInfo> resolve(AriesApplication app, ResolveConstraint... constraints)
            throws ResolverException {
        log.trace("resolving {}", app);

        ApplicationMetadata appMeta = app.getApplicationMetadata();

        String appName = appMeta.getApplicationSymbolicName();
        Version appVersion = appMeta.getApplicationVersion();
        List<Content> appContent = appMeta.getApplicationContents();

        Repository appRepository = null;

        try {
            Document doc = RepositoryDescriptorGenerator.generateRepositoryDescriptor(appName + "_"
                                                                                      + appVersion,
                    app.getBundleInfo());

            File f = File.createTempFile(appName + "_" + appVersion, "repository.xml");
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc),
                    new StreamResult(f));

            appRepository = repositoryAdmin.repository(f.toURI().toURL());

            f.delete();
        } catch (Exception e) {
            throw new ResolverException(e);
        }

        List<Repository> appRepositories = new ArrayList<Repository>();
        
        // add system & local repositories
        appRepositories.add(repositoryAdmin.getSystemRepository());
        appRepositories.add(repositoryAdmin.getLocalRepository());
        
        // add application repository
        appRepositories.add(appRepository);
        
        // add user repositories
        Repository[] userRepositories = repositoryAdmin.listRepositories();
        for (Repository userRepository : userRepositories) {
            appRepositories.add(userRepository);
        }
        
        Resolver obrResolver = repositoryAdmin.resolver(appRepositories.toArray(new Repository[appRepositories.size()]));

        // add a resource describing the requirements of the application metadata.
        try {
            obrResolver.add(new ApplicationResourceImpl(repositoryAdmin, appName, appVersion, appContent));
        } catch (InvalidSyntaxException e) {
            throw new ResolverException(e); 
        }

        if (obrResolver.resolve()) {
            Set<BundleInfo> result = new HashSet<BundleInfo>();
            for (Resource resource : obrResolver.getRequiredResources()) {
                BundleInfo bundleInfo = toBundleInfo(resource, false);
                result.add(bundleInfo);
            }
            if (includeOptional) {
                for (Resource resource : obrResolver.getOptionalResources()) {
                    BundleInfo bundleInfo = toBundleInfo(resource, true);
                    result.add(bundleInfo);
                }
            }
            return result;
        } else {
            throw new ResolverException("Could not resolve requirements: "
                                        + getUnsatisfiedRequirements(obrResolver));
        }

    }

    public BundleInfo getBundleInfo(String bundleSymbolicName, Version bundleVersion) {
        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put(Resource.VERSION, bundleVersion.toString());
        String filterString = ManifestHeaderProcessor.generateFilter(Resource.SYMBOLIC_NAME,
                bundleSymbolicName, attribs);
        Resource[] resources;
        try {
            resources = repositoryAdmin.discoverResources(filterString);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        if (resources != null && resources.length > 0) {
            return toBundleInfo(resources[0], false);
        } else {
            return null;
        }
    }

    private String getUnsatisfiedRequirements(Resolver resolver) {
        Reason[] reasons = resolver.getUnsatisfiedRequirements();
        if (reasons != null) {
            StringBuilder sb = new StringBuilder();
            for (int reqIdx = 0; reqIdx < reasons.length; reqIdx++) {
                sb.append("   " + reasons[reqIdx].getRequirement().getFilter()).append("\n");
                Resource resource = reasons[reqIdx].getResource();
                if (resource != null) {
                    sb.append("      " + resource.getPresentationName()).append("\n");
                }
            }
            return sb.toString();
        }
        return null;
    }

    private BundleInfo toBundleInfo(Resource resource, boolean optional) {
        Map<String, String> directives = null;
        if (optional) {
            directives = new HashMap<String, String>();
            directives.put("resolution", "optional");
        }
        String location = resource.getURI();        
        return new OBRBundleInfo(resource.getSymbolicName(), 
                                 resource.getVersion(), 
                                 location, 
                                 null,
                                 null, 
                                 null, 
                                 null, 
                                 null, 
                                 null, 
                                 directives, 
                                 null);
    }
}