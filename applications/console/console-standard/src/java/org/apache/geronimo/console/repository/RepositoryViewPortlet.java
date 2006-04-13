/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.WriteableRepository;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RepositoryViewPortlet extends BasePortlet {

    private final static Log log = LogFactory.getLog(RepositoryViewPortlet.class);

    private Kernel kernel;

    private PortletContext ctx;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher helpView;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        ctx = portletConfig.getPortletContext();
        normalView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/normal.jsp");
        helpView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/help.jsp");
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        try {



            List list = new ArrayList();
            WriteableRepository repo = PortletManager.getWritableRepositories(actionRequest)[0];

            File uploadFile = null;
            File file = null;
            String name = null;
            String basename = null;
            String fileType = null;
            String artifact = null;
            String version = null;
            String group = null;

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
                            }
                            else {
                               basename = name;
                            }

                            // Create the temporary file to be used for import to the server
                            file = File.createTempFile("geronimo-import", "");
                            file.deleteOnExit();
                            log.debug("Writing repository import file to "+file.getAbsolutePath());
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
                        }
                    }
                }

                String uri = group + "/" + artifact + "/" + version + "/" + fileType;

                repo.copyToRepository(file, new URI(uri), new FileWriteMonitor() {
                    public void writeStarted(String fileDescription) {
                        System.out.print("Copying into repository "+fileDescription+"...");
                        System.out.flush();
                    }

                    public void writeProgress(int bytes) {
                    }

                    public void writeComplete(int bytes) {
                        System.out.println(" Finished.");
                    }
                });
            } catch (FileUploadException e) {
                throw new PortletException(e);
            } catch (URISyntaxException e) {
                throw new IOException("Unable to save to repository URI: "+e.getMessage());
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

        try {
            List list = new ArrayList();
            ListableRepository[] repos = PortletManager.getListableRepositories(request);
            for (int i = 0; i < repos.length; i++) {
                ListableRepository repo = repos[i];
                try {
                    final URI[] uris = repo.listURIs();
                    for (int j = 0; j < uris.length; j++) {
                        if(uris[j] == null) {
                            continue; // probably a JAR lacks a version number in the name, etc.
                        }
                        String fileName = uris[j].toString();
                        list.add(fileName);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
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
