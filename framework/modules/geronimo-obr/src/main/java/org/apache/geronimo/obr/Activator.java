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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.geronimo.kernel.osgi.BundleDescription;
import org.apache.geronimo.obr.model.Resource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.obr.Repository;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Resolver;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator {

    public void start(final BundleContext context) throws Exception {
        ServiceTracker tracker = new ServiceTracker(context, RepositoryAdmin.class.getName(), 
                new ServiceTrackerCustomizer() {

                    public Object addingService(ServiceReference reference) {
                        RepositoryAdmin repositoryAdmin = (RepositoryAdmin) context.getService(reference);
                        
//                        System.out.println("GOT REPO");
//                        File f = new File("/home/gawor/.m2/repository/repository.xml");
//                        try {
//                            repositoryAdmin.addRepository(f.toURI().toURL());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        
                        for (Repository repo : repositoryAdmin.listRepositories()) {
                            System.out.println(repo.getName() + " " + repo.getURL());
                        }
                        
                        org.apache.geronimo.obr.model.Repository r = new org.apache.geronimo.obr.model.Repository();
                        r.setName("foo");
                        
                        for (Bundle b : context.getBundles()) {
                            BundleDescription desc = new BundleDescription(b.getHeaders());
                            ResourceBuilder builder = new ResourceBuilder(desc);
                                                        
                            Resource resource = builder.createResource();
                            if (resource != null) {
                                r.getResource().add(resource);
                            }
                            
                            /*
                            List<BundleDescription.Package> imports = desc.getImportPackage();
                            for (BundleDescription.Package importPackage : imports) {
                                String filter = "(symbolicname=" + desc.getSymbolicName() + ")";
                                Resource[] resources = repositoryAdmin.discoverResources(filter);
                                if (resources != null && resources.length > 0) {
                                    System.out.println("Results found for: " + filter);
                                    for (Resource r : resources) {
                                        //System.out.println(r.getId() + " " + r.getSymbolicName() + " " + r.getURL());
                                        Resolver resolver = repositoryAdmin.resolver();
                                        resolver.add(r);
                                        if (resolver.resolve()) {
                                            System.out.println("Resolved");
                                        } else {
                                            System.out.println("Unresolved: " + resolver.getReason(r));
                                        }
                                    }
                                } else {
                                    System.out.println("Results not found for: " + filter);
                                }
                            }
                            */
                        }
                        
//                        try {
//                            JAXBContext context = JAXBContext.newInstance(org.apache.geronimo.obr.model.Repository.class);
//                            Marshaller marshaller = context.createMarshaller();
//                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//                            marshaller.marshal(r, new File("/home/gawor/repo.xml"));
//
//
//                            Unmarshaller unmarshaller = context.createUnmarshaller();
//                            org.apache.geronimo.obr.model.Repository rr = (org.apache.geronimo.obr.model.Repository) unmarshaller.unmarshal(f);
//                            for (Resource rrr : rr.getResource()) {
//                                System.out.println(rrr.getId() + " " + rrr.getPresentationname() + " " + rrr.getSymbolicname() + " " + rrr.getDescription());
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        return reference;
                    }

                    public void modifiedService(ServiceReference arg0, Object arg1) {
                    }

                    public void removedService(ServiceReference arg0, Object arg1) {
                    }
            
        });
        
        tracker.open();
                       
    }

    private <T> T getService(BundleContext context, Class<T> name) {
        ServiceReference ref = context.getServiceReference(name.getName());
        return (ref == null) ? null : (T) context.getService(ref);
    }
    
    public void stop(BundleContext context) throws Exception {
    }
   
}
