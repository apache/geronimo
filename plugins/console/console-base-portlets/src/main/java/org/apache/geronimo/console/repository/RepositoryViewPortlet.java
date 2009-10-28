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

package org.apache.geronimo.console.repository;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.repository.Maven2Repository;
import org.apache.geronimo.system.resolver.ExplicitDefaultArtifactResolver;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

/**
 * @version $Rev$ $Date$
 */
public class RepositoryViewPortlet extends BasePortlet {

    private static final Logger log = LoggerFactory.getLogger(RepositoryViewPortlet.class);

    private Kernel kernel;

    private PortletContext ctx;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher helpView;

    private PortletRequestDispatcher usageView;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        ctx = portletConfig.getPortletContext();
        normalView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/normal.jsp");
        helpView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/help.jsp");
        usageView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/usage.jsp");
    }

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        String action = actionRequest.getParameter("action");
        if(action != null && action.equals("usage")) {
            // User clicked on a repository entry to view usage
            String res = actionRequest.getParameter("res");
            actionResponse.setRenderParameter("mode", "usage");
            actionResponse.setRenderParameter("res", res);
            return;
        }        
        
        if(action != null && action.equals("remove")) {
            // User clicked on a repository remove
            String res = actionRequest.getParameter("res");
            actionResponse.setRenderParameter("mode", "remove");
            actionResponse.setRenderParameter("res", res);
            Maven2Repository repo = (Maven2Repository) PortletManager.getCurrentServer(actionRequest).getRepositories()[0];
            Artifact artifact = Artifact.create(res);
            File location = repo.getLocation(artifact);
            if (location == null) {
                return;//??
            }
            if (location.isDirectory()) {
                //don't use this to uninstall plugins
                return;//??
            }
            while (true) {
                location.delete();
                location = location.getParentFile();
                File[] contents = location.listFiles();
                if (contents == null || contents.length == 0) {
                    return;
                }
            }
        }

        try {


            WriteableRepository repo = PortletManager.getCurrentServer(actionRequest).getWritableRepositories()[0];

            File uploadFile = null;
            File file = null;
            String name = null;
            String basename = null;
            String fileType = null;
            String artifact = null;
            String version = null;
            String group = null;
            String jarName = null;

            PortletFileUpload uploader = new PortletFileUpload(new DiskFileItemFactory());
            try {
                List items = uploader.parseRequest(actionRequest);
                for (Iterator i = items.iterator(); i.hasNext();) {
                    FileItem item = (FileItem) i.next();
                    if (!item.isFormField()) {
                        String fieldName = item.getFieldName().trim();
                        name = item.getName().trim();

                        if (name.length() == 0) {
                            file = null;
                        } else {
                            // IE sends full path while Firefox sends just basename
                            // in the case of "FullName" we may be able to infer the group
                            // Note, we can't use File.separatorChar because the file separator
                            // is dependent upon the client and not the server.
                            String fileChar = "\\";
                            int fileNameIndex = name.lastIndexOf(fileChar);
                            if (fileNameIndex == -1) {
                                fileChar = "/";
                                fileNameIndex = name.lastIndexOf(fileChar);
                            }
                            if (fileNameIndex != -1) {
                                basename = name.substring(fileNameIndex + 1);
                            } else {
                                basename = name;
                            }

                            // Create the temporary file to be used for import to the server
                            file = File.createTempFile("geronimo-import", "");
                            file.deleteOnExit();
                            log.debug("Writing repository import file to " + file.getAbsolutePath());
                        }

                        if ("local".equals(fieldName)) {
                            uploadFile = file;
                        }

                        if (file != null) {
                            try {
                                item.write(file);
                            } catch (Exception e) {
                                throw new PortletException(e);
                            }
                        }
                        // This is not the file itself, but one of the form fields for the URI
                    } else {
                        String fieldName = item.getFieldName().trim();
                        if ("group".equals(fieldName)) {
                            group = item.getString().trim();
                        } else if ("artifact".equals(fieldName)) {
                            artifact = item.getString().trim();
                        } else if ("version".equals(fieldName)) {
                            version = item.getString().trim();
                        } else if ("fileType".equals(fieldName)) {
                            fileType = item.getString().trim();
                        } else if ("jarName".equals(fieldName)) {
                            jarName = item.getString().trim();
                        }
                    }
                }
                if (jarName != null) {
                    ExplicitDefaultArtifactResolver instance = KernelRegistry.getSingleKernel().getGBean(ExplicitDefaultArtifactResolver.class);
                    Properties set = new Properties();
                    set.put(jarName, group + "/" + artifact + "/" + version + "/" + fileType);
                    instance.addAliases(set);
                }
                repo.copyToRepository(file, new Artifact(group, artifact, version, fileType), new FileWriteMonitor() {
                    public void writeStarted(String fileDescription, int fileSize) {
                        log.info("Copying into repository " + fileDescription + "...");
                    }

                    public void writeProgress(int bytes) {
                    }

                    public void writeComplete(int bytes) {
                        log.info("Finished.");
                    }
                });
            } catch (FileUploadException e) {
                throw new PortletException(e);
            } catch (GBeanNotFoundException e) {
                throw new PortletException(e);
            } catch (InternalKernelException e) {
                throw new PortletException(e);
            } catch (IllegalStateException e) {
                throw new PortletException(e);
            }
        } catch (PortletException e) {
            throw e;
        }
    }

    protected void doView(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        // i think generic portlet already does this
        if (WindowState.MINIMIZED.equals(request.getWindowState())) {
            return;
        }

        String mode = request.getParameter("mode");
        if(mode != null && mode.equals("usage")) {
            String res = request.getParameter("res");
            String[] parts = res.split("/");
            request.setAttribute("res", res);
            request.setAttribute("groupId", parts[0]);
            request.setAttribute("artifactId", parts[1]);
            request.setAttribute("version", parts[2]);
            request.setAttribute("type", parts[3]);        
            usageView.include(request, response);
            return;
        }

        try {
            List list = new ArrayList();
            ListableRepository[] repos = PortletManager.getCurrentServer(request).getRepositories();
            for (int i = 0; i < repos.length; i++) {
                ListableRepository repo = repos[i];
                final SortedSet artifacts = repo.list();
                for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                    String fileName = iterator.next().toString();
                    list.add(fileName);
                }
            }
            Collections.sort(list);

            request.setAttribute("org.apache.geronimo.console.repo.list", list);

        } catch (Exception e) {
            throw new PortletException(e);
        }

        normalView.include(request, response);
    }

    public void doHelp(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {
        helpView.include(request, response);
    }

    public List listing(File dir, String basepath) throws java.io.IOException {
        if (dir == null) {
            throw new IllegalArgumentException("directory argument is null");
        }

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("directory argument expected");
        }

        List listing = new ArrayList();

        List ls = Arrays.asList(dir.listFiles());
        Iterator iter = ls.iterator();

        while (iter.hasNext()) {
            File f = (File) iter.next();

            if (f.isDirectory()) {
                List listing1 = listing(f, basepath);
                listing.addAll(listing1);
            } else {
                listing.add(f.getCanonicalPath().substring(
                        basepath.length() + 1));
            }
        }
        return listing;
    }

}
