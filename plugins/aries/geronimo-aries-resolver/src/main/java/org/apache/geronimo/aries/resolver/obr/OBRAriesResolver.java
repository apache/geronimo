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

package org.apache.geronimo.aries.resolver.obr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.aries.application.ApplicationMetadata;
import org.apache.aries.application.Content;
import org.apache.aries.application.VersionRange;
import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationResolver;
import org.apache.aries.application.management.BundleInfo;
import org.apache.aries.application.management.ResolveConstraint;
import org.apache.aries.application.management.ResolverException;
import org.apache.aries.application.utils.manifest.ManifestHeaderProcessor;
import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.apache.geronimo.aries.resolver.internal.MessageUtil;
import org.apache.geronimo.aries.resolver.internal.ModellingConstants;
import org.apache.geronimo.aries.resolver.obr.generator.RepositoryDescriptorGenerator;
import org.apache.geronimo.aries.resolver.obr.impl.ApplicationResourceImpl;
import org.apache.geronimo.aries.resolver.obr.impl.OBRBundleInfo;
import org.apache.geronimo.aries.resolver.obr.impl.ResourceWrapper;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @version $Rev: 23025 $ $Date: 2011-08-23 15:04:55 -0400 (Tue, 23 Aug 2011)
 *          $
 */
public class OBRAriesResolver implements AriesApplicationResolver {
    private static Logger log = LoggerFactory.getLogger(OBRAriesResolver.class);

    private final static String LOG_ENTRY = "Method entry: {}, args {}";
    private final static String LOG_EXIT = "Method exit: {}, returning {}";

    private final RepositoryAdmin repositoryAdmin;
    private boolean returnOptionalResources = true;
    private boolean resolveFragments = false;

    public OBRAriesResolver(RepositoryAdmin repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
    }

    public void setReturnOptionalResources(boolean optional) {
        this.returnOptionalResources = optional;
    }

    public boolean getReturnOptionalResources() {
        return returnOptionalResources;
    }

    public void setResolveFragments(boolean resolveFragments) {
        this.resolveFragments = resolveFragments;
    }

    public boolean getResolveFragments() {
        return resolveFragments;
    }

    public Set<BundleInfo> resolve(AriesApplication app, ResolveConstraint... constraints) throws ResolverException {
        log.trace("resolving {}", app);
        DataModelHelper helper = repositoryAdmin.getHelper();

        ApplicationMetadata appMeta = app.getApplicationMetadata();

        String appName = appMeta.getApplicationSymbolicName();
        Version appVersion = appMeta.getApplicationVersion();
        List<Content> appContent = appMeta.getApplicationContents();

        Repository appRepo;

        try {
            Document doc = RepositoryDescriptorGenerator.generateRepositoryDescriptor(appName + "_" + appVersion, app
                    .getBundleInfo());

            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(bytesOut));

            appRepo = helper.readRepository(new InputStreamReader(new ByteArrayInputStream(bytesOut.toByteArray())));
        } catch (Exception e) {
            throw new ResolverException(e);
        }

        List<Repository> resolveRepos = new ArrayList<Repository>();

        // add system repository
        resolveRepos.add(repositoryAdmin.getSystemRepository());

        // add local repository
        resolveRepos.add(getLocalRepository(repositoryAdmin));

        // add application repository
        resolveRepos.add(appRepo);

        // add user-defined repositories
        Repository[] repos = repositoryAdmin.listRepositories();
        for (Repository r : repos) {
            resolveRepos.add(r);
        }

        Resolver obrResolver = repositoryAdmin.resolver(resolveRepos.toArray(new Repository[resolveRepos.size()]));
        // add a resource describing the requirements of the application metadata.
        obrResolver.add(createApplicationResource(helper, appName, appVersion, appContent));
        
        log.debug("Resolving application resources");
        boolean resolved = obrResolver.resolve();
        log.debug("Resolution result: {}", resolved);
        
        if (resolved) {
            Set<BundleInfo> fragmentResult = null;
            if (resolveFragments) {
                fragmentResult = resolveFragments(obrResolver, resolveRepos, appName);
            }

            Set<BundleInfo> result = new HashSet<BundleInfo>();
            toBundleInfo(result, obrResolver.getRequiredResources(), false);
            if (returnOptionalResources) {
                toBundleInfo(result, obrResolver.getOptionalResources(), true);
            }
            if (fragmentResult != null) {
                result.addAll(fragmentResult);
            }                        
            return result;
        } else {
            throw createResolveException(appName, obrResolver);
        }
    }

    private Set<BundleInfo> resolveFragments(Resolver obrResolver, List<Repository> resolveRepos, String appName) throws ResolverException {
        log.debug("Searching for fragments");
        
        Set<Resource> requiredFragments = new HashSet<Resource>();
        Set<Resource> optionalFragments = new HashSet<Resource>();
        
        for (Resource resource : obrResolver.getRequiredResources()) {
            Resource fragmentResource = findFragmentResource(resource);
            if (fragmentResource != null) {
                requiredFragments.add(fragmentResource);
            }
        }
        if (returnOptionalResources) {
            for (Resource resource : obrResolver.getOptionalResources()) {
                Resource fragmentResource = findFragmentResource(resource);
                if (fragmentResource != null) {
                    optionalFragments.add(fragmentResource);
                }
            }
        }
        
        if (!requiredFragments.isEmpty() || !optionalFragments.isEmpty()) {
            Resolver fragmentResolver = repositoryAdmin.resolver(resolveRepos.toArray(new Repository[resolveRepos.size()]));
            for (Resource fragment : requiredFragments) {
                fragmentResolver.add(fragment);
            }
            for (Resource fragment : optionalFragments) {
                fragmentResolver.add(fragment);
            } 
            
            log.debug("Resolving fragment resources, required={}, optional={}", requiredFragments, optionalFragments);
            boolean resolved = fragmentResolver.resolve();
            log.debug("Resolution result: {}", resolved);
            if (resolved) {
                Set<BundleInfo> result = new HashSet<BundleInfo>();
                toBundleInfo(result, requiredFragments, false);
                toBundleInfo(result, fragmentResolver.getRequiredResources(), false);
                if (returnOptionalResources) {
                    toBundleInfo(result, optionalFragments, true);
                    toBundleInfo(result, fragmentResolver.getOptionalResources(), true);
                }
                return result;
            } else {
                throw createResolveException(appName, fragmentResolver);
            }
        } else {
            log.debug("No fragments found");
            return null;
        }
    }
    
    private ResolverException createResolveException(String appName, Resolver obrResolver) {
        Reason[] reasons = obrResolver.getUnsatisfiedRequirements();
        // let's refine the list by removing the indirect unsatisfied
        // bundles that are caused by unsatisfied packages or other bundles
        Map<String, Set<String>> refinedReqs = refineUnsatisfiedRequirements(obrResolver, reasons);

        StringBuffer reqList = new StringBuffer();
        Map<String, String> unsatisfiedRequirements = extractConsumableMessageInfo(refinedReqs);
        for (String reason : unsatisfiedRequirements.keySet()) {
            reqList.append('\n');
            reqList.append(reason);
        }

        ResolverException re = new ResolverException(MessageUtil.getMessage("RESOLVER_UNABLE_TO_RESOLVE",
                new Object[] { appName, reqList }));
        List<String> list = new ArrayList<String>();
        list.addAll(unsatisfiedRequirements.keySet());
        re.setUnsatisfiedRequirements(list);

        return re;
    }

    private void toBundleInfo(Set<BundleInfo> result, Resource[] resources, boolean optional) {
        for (Resource resource : resources) {
            BundleInfo bundleInfo = toBundleInfo(resource, optional);
            result.add(bundleInfo);
        }
    }

    private void toBundleInfo(Set<BundleInfo> result, Collection<Resource> resources, boolean optional) {
        for (Resource resource : resources) {
            BundleInfo bundleInfo = toBundleInfo(resource, optional);
            result.add(bundleInfo);
        }
    }
  
    private Repository getLocalRepository(RepositoryAdmin admin) {
        Repository localRepository = repositoryAdmin.getLocalRepository();

        Resource[] resources = localRepository.getResources();

        Resource[] newResources = new Resource[resources.length];
        for (int i = 0; i < resources.length; i++) {
            newResources[i] = new ResourceWrapper(resources[i]);
        }

        return repositoryAdmin.getHelper().repository(newResources);
    }

    private Resource createApplicationResource(DataModelHelper helper, String appName, Version appVersion,
            List<Content> appContent) {
        return new ApplicationResourceImpl(appName, appVersion, appContent);
    }

    public BundleInfo getBundleInfo(String bundleSymbolicName, Version bundleVersion) {
        Map<String, String> attribs = new HashMap<String, String>();
        // bundleVersion is an exact version - so ensure right version filter is
        // generated
        VersionRange range = ManifestHeaderProcessor.parseVersionRange(bundleVersion.toString(), true);
        attribs.put(Resource.VERSION, range.toString());
        String filterString = ManifestHeaderProcessor.generateFilter(Resource.SYMBOLIC_NAME, bundleSymbolicName,
                attribs);
        Resource[] resources;
        try {
            resources = repositoryAdmin.discoverResources(filterString);
            if (resources != null && resources.length > 0) {
                return toBundleInfo(resources[0], false);
            } else {
                return null;
            }
        } catch (InvalidSyntaxException e) {
            log.error("Invalid filter", e);
            return null;
        }
    }

    private BundleInfo toBundleInfo(Resource resource, boolean optional) {
        Map<String, String> directives = null;
        if (optional) {
            directives = new HashMap<String, String>();
            directives.put(Constants.RESOLUTION_DIRECTIVE, Constants.RESOLUTION_OPTIONAL);
        }
        String location = resource.getURI();
        return new OBRBundleInfo(resource.getSymbolicName(), resource.getVersion(), location, null, null, null, null,
                null, null, directives, null);
    }

    /**
     * Refine the unsatisfied requirements ready for later human comsumption
     * 
     * @param resolver
     *            The resolver to be used to refine the requirements
     * @param reasons
     *            The reasons
     * @return A map of the unsatifiedRequirement to the set of bundles that
     *         have that requirement unsatisfied (values associated with the
     *         keys can be null)
     */
    private Map<String, Set<String>> refineUnsatisfiedRequirements(Resolver resolver, Reason[] reasons) {
        log.debug(LOG_ENTRY, "refineUnsatisfiedRequirements", new Object[] { resolver, Arrays.toString(reasons) });

        Map<Requirement, Set<String>> req_resources = new HashMap<Requirement, Set<String>>();
        // add the reasons to the map, use the requirement as the key, the
        // resources required the requirement as the values
        Set<Resource> resources = new HashSet<Resource>();
        for (Reason reason : reasons) {
            resources.add(reason.getResource());
            Requirement key = reason.getRequirement();
            String value = reason.getResource().getSymbolicName() + "_" + reason.getResource().getVersion().toString();
            Set<String> values = req_resources.get(key);
            if (values == null) {
                values = new HashSet<String>();
            }
            values.add(value);
            req_resources.put(key, values);
        }

        // remove the requirements that can be satisifed by the resources. It is
        // listed because the resources are not satisfied by other requirements.
        // For an instance, the unsatisfied reasons are [package a, required by
        // bundle aa], [package b, required by bundle bb] and [package c,
        // required by bundle cc],
        // If the bundle aa exports the package a and c. In our error message,
        // we only want to display package a is needed by bundle aa.
        // Go through each requirement and find out whether the requirement can
        // be satisfied by the reasons.
        Set<Capability> caps = new HashSet<Capability>();
        for (Resource res : resources) {
            if ((res != null) && (res.getCapabilities() != null)) {
                List<Capability> capList = Arrays.asList(res.getCapabilities());
                if (capList != null) {
                    caps.addAll(capList);
                }
            }
        }

        Iterator<Map.Entry<Requirement, Set<String>>> iterator = req_resources.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Requirement, Set<String>> entry = iterator.next();
            Requirement req = entry.getKey();
            for (Capability cap : caps) {
                if (req.isSatisfied(cap)) { // remove the key from the map
                    iterator.remove();
                    break;
                }
            }
        }
        // Now the map only contains the necessary missing requirements

        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Map.Entry<Requirement, Set<String>> req_res : req_resources.entrySet()) {
            result.put(req_res.getKey().getFilter(), req_res.getValue());
        }

        log.debug(LOG_EXIT, "refineUnsatisfiedRequirements", new Object[] { result });

        return result;
    }

    private static final Set<String> SPECIAL_FILTER_ATTRS = Collections.unmodifiableSet(new HashSet<String>(Arrays
            .asList(ModellingConstants.OBR_PACKAGE, ModellingConstants.OBR_SYMBOLIC_NAME,
                    ModellingConstants.OBR_SERVICE, Constants.VERSION_ATTRIBUTE)));

    /**
     * Turn a requirement into a human readable String for debug.
     * 
     * @param filter
     *            The filter that is failing
     * @param bundlesFailing
     *            For problems with a bundle, the set of bundles that have a
     *            problem
     * @return human readable form
     */
    private Map<String, String> extractConsumableMessageInfo(Map<String, Set<String>> refinedReqs) {
        log.debug(LOG_ENTRY, "extractConsumableMessageInfo", refinedReqs);

        Map<String, String> unsatisfiedRequirements = new HashMap<String, String>();

        for (Map.Entry<String, Set<String>> filterEntry : refinedReqs.entrySet()) {

            String filter = filterEntry.getKey();
            Set<String> bundlesFailing = filterEntry.getValue();

            log.debug("unable to satisfy the filter , filter = " + filter + "required by "
                    + Arrays.toString(bundlesFailing.toArray()));

            Map<String, String> attrs = ManifestHeaderProcessor.parseFilter(filter);
            Map<String, String> customAttrs = new HashMap<String, String>();
            for (Map.Entry<String, String> e : attrs.entrySet()) {
                if (!SPECIAL_FILTER_ATTRS.contains(e.getKey())) {
                    customAttrs.put(e.getKey(), e.getValue());
                }
            }

            StringBuilder msgKey = new StringBuilder();
            List<Object> inserts = new ArrayList<Object>();

            final String type;
            boolean unknownType = false;
            if (attrs.containsKey(ModellingConstants.OBR_PACKAGE)) {
                type = ModellingConstants.OBR_PACKAGE;
                msgKey.append("RESOLVER_UNABLE_TO_RESOLVE_PACKAGE");
                inserts.add(attrs.get(ModellingConstants.OBR_PACKAGE));
            } else if (attrs.containsKey(ModellingConstants.OBR_SYMBOLIC_NAME)) {
                type = ModellingConstants.OBR_SYMBOLIC_NAME;
                msgKey.append("RESOLVER_UNABLE_TO_RESOLVE_BUNDLE");
                inserts.add(attrs.get(ModellingConstants.OBR_SYMBOLIC_NAME));
            } else if (attrs.containsKey(ModellingConstants.OBR_SERVICE)) {
                type = ModellingConstants.OBR_SERVICE;
                msgKey.append("RESOLVER_UNABLE_TO_RESOLVE_SERVICE");
                // No insert for service name as the name must be "*" to match
                // any
                // Service capability
            } else {
                type = ModellingConstants.OBR_UNKNOWN;
                unknownType = true;
                msgKey.append("RESOLVER_UNABLE_TO_RESOLVE_FILTER");
                inserts.add(filter);
            }

            if (bundlesFailing != null && bundlesFailing.size() != 0) {
                msgKey.append("_REQUIRED_BY_BUNDLE");
                if (bundlesFailing.size() == 1)
                    inserts.add(bundlesFailing.iterator().next()); // Just take
                // the string
                // if there's only one
                // of them
                else
                    inserts.add(bundlesFailing.toString()); // Add the whole set
                // if there
                // isn't exactly one
            }
            if (!unknownType && !customAttrs.isEmpty()) {
                msgKey.append("_WITH_ATTRS");
                inserts.add(customAttrs);
            }

            if (!unknownType && attrs.containsKey(Constants.VERSION_ATTRIBUTE)) {
                msgKey.append("_WITH_VERSION");
                VersionRange vr = ManifestHeaderProcessor.parseVersionRange(attrs.get(Constants.VERSION_ATTRIBUTE));
                inserts.add(vr.getMinimumVersion());

                if (!!!vr.isExactVersion()) {
                    msgKey.append(vr.isMinimumExclusive() ? "_LOWEX" : "_LOW");
                    if (vr.getMaximumVersion() != null) {
                        msgKey.append(vr.isMaximumExclusive() ? "_UPEX" : "_UP");
                        inserts.add(vr.getMaximumVersion());
                    }
                }
            }

            String msgKeyStr = msgKey.toString();

            String msg = MessageUtil.getMessage(msgKeyStr, inserts.toArray());
            unsatisfiedRequirements.put(msg, type);
        }

        log.debug(LOG_EXIT, "extractConsumableMessageInfo", unsatisfiedRequirements);

        return unsatisfiedRequirements;
    }
    
    
    private Resource findFragmentResource(Resource hostResource) {
        Capability hostCapability = getHostCapability(hostResource.getCapabilities());       
        if (hostCapability == null || Capability.FRAGMENT.equals(hostCapability.getName())) {
            // no capabilities or it's a fragment
            return null;
        }
        
        log.debug("Searching for fragments for {}", hostResource);
        
        DataModelHelper helper = repositoryAdmin.getHelper();
        
        String filter = "(&(host=" + hostResource.getSymbolicName() + "))";
        Requirement fragmentRequirement = helper.requirement(Capability.FRAGMENT, filter);
        Resource[] fragmentResources = repositoryAdmin.discoverResources(new Requirement[] { fragmentRequirement });
        if (fragmentResources != null && fragmentResources.length > 0) {

            if (log.isDebugEnabled()) {
                log.debug("Fragments found for {}: {}", new Object[] { hostResource, Arrays.asList(fragmentResources) } );                
            }
            
            List<Resource> candidateFragments = new ArrayList<Resource>();

            // trim out fragments that do not match the host
            for (Resource fragmentResource : fragmentResources) {
                Requirement fragmentHostRequirement = getFragmentHostRequirement(fragmentResource.getRequirements());
                if (fragmentHostRequirement == null) {
                    log.debug("Ignoring {} fragment. No host requirement found.", fragmentResource);
                    continue;
                }
                if (fragmentHostRequirement.isSatisfied(hostCapability)) {
                    candidateFragments.add(fragmentResource);
                }
            }

            int candiates = candidateFragments.size();
            if (candiates == 0) {
                log.debug("No matching fragments found for {}", hostResource);
                return null;
            } else if (candiates == 1) {
                Resource fragmentResource = candidateFragments.get(0);
                log.debug("Single matching fragment found for {}: {}", new Object[] { hostResource, fragmentResource }); 
                return fragmentResource;
            } else {
                Collections.sort(candidateFragments, new Comparator<Resource>() {
                    @Override
                    public int compare(Resource object1, Resource object2) {
                        Version version1 = object1.getVersion();
                        Version version2 = object2.getVersion();
                        return version2.compareTo(version1);
                    }
                });
                Resource fragmentResource = candidateFragments.get(0);
                log.debug("Multiple matching fragments found for {}. Fragment selected: {}", new Object[] { hostResource, fragmentResource }); 
                return fragmentResource;
            }

        } else {
            log.debug("No fragments found for {}", hostResource);
        }

        return null;
    }
    
    /*
     * Returns "fragment" or "bundle" capability. 
     */
    private Capability getHostCapability(Capability[] capabilities) {
        Capability bundleCapability = null;
        if (capabilities != null) {
            for (Capability capability : capabilities) {
                if (Capability.BUNDLE.equals(capability.getName()) && bundleCapability == null) {
                    bundleCapability = capability;
                } else if (Capability.FRAGMENT.equals(capability.getName())) {
                    return capability;
                }
            }
        }
        return bundleCapability;
    }
    
    private Requirement getFragmentHostRequirement(Requirement[] requirements) {
        if (requirements != null) {
            for (Requirement requirement : requirements) {
                if (Capability.BUNDLE.equals(requirement.getName()) && requirement.isExtend()) {
                    return requirement;
                }
            }
        }
        return null;
    }
}
