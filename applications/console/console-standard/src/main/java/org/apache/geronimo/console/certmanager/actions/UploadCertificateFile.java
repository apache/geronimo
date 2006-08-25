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

package org.apache.geronimo.console.certmanager.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.geronimo.console.certmanager.CertManagerPortlet;

public class UploadCertificateFile {

    public static void action(CertManagerPortlet portlet,
            ActionRequest request, ActionResponse response)
            throws PortletException, IOException {
        if (!PortletFileUpload.isMultipartContent(request)) {
            throw new PortletException("Expected file upload");
        }

        File rootDir = new File(System.getProperty("java.io.tmpdir"));
        PortletFileUpload uploader = new PortletFileUpload(
                new DiskFileItemFactory(10240, rootDir));
        File certFile = null;

        try {
            List items = uploader.parseRequest(request);
            for (Iterator i = items.iterator(); i.hasNext();) {
                FileItem item = (FileItem) i.next();
                if (!item.isFormField()) {
                    String name = item.getName().trim();

                    if (name.length() == 0) {
                        certFile = null;
                    } else {
                        // Firefox sends basename, IE sends full path
                        int index = name.lastIndexOf('\\');
                        if (index != -1) {
                            name = name.substring(index + 1);
                        }
                        certFile = new File(rootDir, name);
                    }

                    if (certFile != null) {
                        try {
                            item.write(certFile);
                        } catch (Exception e) {
                            throw new PortletException(e);
                        }
                    }
                }
            }
        } catch (FileUploadException e) {
            throw new PortletException(e);
        }

        // pass certificate file name along
        String certFileName = certFile.getCanonicalPath();
        String enc = URLEncoder.encode(certFileName, "UTF-8");

        portlet.getPortletContext().log("cert-file-name: " + certFileName);
        portlet.getPortletContext().log("enc: " + enc);

        response.setRenderParameter("org.apache.geronimo.console.cert.file.enc", enc);
        response.setRenderParameter("action", request.getParameter("action"));
    }

    public static void render(CertManagerPortlet portlet,
            RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        String encodedCertFileName = request
                .getParameter("org.apache.geronimo.console.cert.file.enc");
        String certFileName = URLDecoder.decode(encodedCertFileName, "UTF-8");
        portlet.getPortletContext().log("cert file: " + certFileName);

        Collection certs = null;
        InputStream is = null;

        if (certFileName != null) {
            File certFile = new File(certFileName);
            try {
                is = new FileInputStream(certFile);

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certs = cf.generateCertificates(is);
            } catch (Exception e) {
                throw new PortletException(e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                }
            }

            request.setAttribute("org.apache.geronimo.console.certs", certs);
            request.setAttribute("org.apache.geronimo.console.cert.file.enc",
                    encodedCertFileName);
        }

        PortletRequestDispatcher prd = null;

        prd = portlet.getPortletContext().getRequestDispatcher(
                "/WEB-INF/view/certmanager/importTrustedCertNormal.jsp");

        prd.include(request, response);
    }
}
