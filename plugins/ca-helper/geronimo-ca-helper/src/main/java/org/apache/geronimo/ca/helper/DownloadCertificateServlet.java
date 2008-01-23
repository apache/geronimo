/**
 *
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
package org.apache.geronimo.ca.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.cert.Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.ca.helper.util.CAHelperUtils;
import org.apache.geronimo.management.geronimo.CertificateRequestStore;
import org.apache.geronimo.management.geronimo.CertificateStore;

/**
 * Servlet implementation class for Servlet: DownloadCertificateServlet
 *
 * @version $Rev$ $Date$
 */
 public class DownloadCertificateServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public DownloadCertificateServlet() {
        super();
    }       

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }      

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        String csrId = request.getParameter("csrId");
        try {
            if(type != null && type.equals("ca")){
                // Request is to download CA's certificate
                // Retrieve CA's certificate from the CertificateStore
                CertificateStore certStore = CAHelperUtils.getCertificateStore();
                Certificate cert = certStore.getCACertificate();
                byte[] data = cert.getEncoded();
                // Upload the certificate with mime-header for CA certificates
                response.setContentType("application/x-x509-ca-cert");
                response.setContentLength(data.length);
                response.getOutputStream().write(data);
            } else if(csrId != null){
                // Request is to download user's own certificate
                // Get the serial number of the certificate based on the csrId
                CertificateRequestStore certReqStore = CAHelperUtils.getCertificateRequestStore();
                BigInteger sNo = certReqStore.getSerialNumberForRequest(csrId);
                if(sNo == null) {
                    // Either the CSR is yet to be fulfilled or the csrId is invalid.
                    throw new Exception("Either the CSR is yet to be fulfilled or the csrId is invalid. csrId = "+csrId);
                }
                CertificateStore certStore = CAHelperUtils.getCertificateStore();
                Certificate cert = certStore.getCertificate(sNo);
                byte[] data = cert.getEncoded();
                
                // Create a link for "verify certificate" page.
                String host = request.getServerName();
                int port = CAHelperUtils.getHttpsClientAuthPort();
                String contextPath = request.getContextPath();
                String link = "https://"+host+":"+port+""+contextPath+"/verifyCertificate.jsp?csrId="+request.getParameter("csrId");

                // Create a multi-part mime message with user's certificate and an information page.
                response.setContentType("multipart/mixed; boundary=\"BOUNDARY\"");
                OutputStream out = response.getOutputStream();
                out.write("This is a multi-part message in MIME format.\n".getBytes());

                // Upload the certificate with mime-header for user certificates.
                out.write("--BOUNDARY\n".getBytes());
                out.write(("Content-type: application/x-x509-user-cert\n\n").getBytes());
                out.write(data);

                // A web page showing "verify certificate" link if an HTTPS client-authentication connector is configured.
                out.write("--BOUNDARY\n".getBytes());
                out.write("Content-type: text/html\n\n".getBytes());
                out.write("<html><body>".getBytes());
                out.write("<p>Certificate is downloaded successfully. ".getBytes());
                if(port != -1)
                    out.write(("Access <a href="+link+">this link</a> to verify.</p>\n").getBytes());
                else
                    out.write("No HTTPS client-authentication port is configured to verify.</p>\n".getBytes());

                out.write(("<a href=\""+contextPath+"\"> Back to CA Helper home</a>").getBytes());
                out.write("</body></html>".getBytes());

                out.write("--BOUNDARY--\n".getBytes());
                out.flush();
            } else {
                // Request is for downloading neither CA's certificate nor user's certificate.
                throw new Exception("Invalid certificate download request.");
            }
        } catch (Exception e) {
            throw new ServletException("Exception while uploading certificate.", e);
        }
    }
}
