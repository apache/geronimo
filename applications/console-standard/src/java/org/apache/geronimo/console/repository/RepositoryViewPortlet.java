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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.BasePortlet;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public class RepositoryViewPortlet extends BasePortlet {

    private static final String REPO_OBJ_NAME = "geronimo.server:name=Repository,J2EEServer=geronimo,J2EEApplication=null,j2eeType=GBean,J2EEModule=org/apache/geronimo/System";

    private Kernel kernel;

    private PortletContext ctx;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher helpView;

    private URL rootURL;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        ctx = portletConfig.getPortletContext();
        normalView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/normal.jsp");
        helpView = ctx
                .getRequestDispatcher("/WEB-INF/view/repository/help.jsp");

        URI uri = null;

        try {
            ObjectName reponame = new ObjectName(REPO_OBJ_NAME);
            uri = new URI(".");
            rootURL = (URL) kernel.invoke(reponame, "getURL",
                    new Object[] {uri}, new String[] {"java.net.URI"});
            uri = new URI(rootURL.toString());
            rootURL.getFile();
        } catch (Exception e) {
            throw new PortletException(e);
        }

        if (!uri.getScheme().equals("file")) {
            throw new PortletException("unsupported scheme: repositoryURL = "
                    + rootURL.toString());
        }
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        try {

            File rootDir = new File(rootURL.getFile() + File.separatorChar
                    + "upload" + File.separatorChar + "jars");

            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }

            PortletFileUpload uploader = new PortletFileUpload(
                    new DiskFileItemFactory(10240, rootDir));

            File uploadFile = null;

            try {

                List items = uploader.parseRequest(actionRequest);
                for (Iterator i = items.iterator(); i.hasNext();) {
                    FileItem item = (FileItem) i.next();
                    if (!item.isFormField()) {
                        String fieldName = item.getFieldName().trim();
                        String name = item.getName().trim();
                        File file;

                        if (name.length() == 0) {
                            file = null;
                        } else {
                            // Firefox sends basename, IE sends full path
                            int index = name.lastIndexOf('\\');
                            if (index != -1) {
                                name = name.substring(index + 1);
                            }
                            file = new File(rootDir, name);
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
                    }
                }
            } catch (FileUploadException e) {
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

        try {
            File f = new File(new URI(rootURL.toString()));
            List ls = listing(f, f.getCanonicalPath());
            Collections.sort(ls);

            request.setAttribute("org.apache.geronimo.console.repo.root",
                    rootURL.toString());
            request.setAttribute("org.apache.geronimo.console.repo.list", ls);
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
