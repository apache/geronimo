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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.ca.helper.util.CAHelperUtils;
import org.apache.geronimo.crypto.CaUtils;

/**
 * Servlet implementation class for Servlet: CertificateRequestServlet
 *
 * @version $Rev$ $Date$
 */
 public class CertificateRequestServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public CertificateRequestServlet() {
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
        // Retrieve the values submitted by the user
        String reqCN = request.getParameter("reqCN");
        String reqOU = request.getParameter("reqOU");
        String reqO = request.getParameter("reqO");
        String reqL = request.getParameter("reqL");
        String reqST = request.getParameter("reqST");
        String reqC = request.getParameter("reqC");
        String spkac = request.getParameter("spkac");
        String pkcs10req = request.getParameter("pkcs10req");

        String toStore = null;
        if(pkcs10req != null && !pkcs10req.equals("")) {
            // Either generated from Internet Explorer or submitted as PKCS10 request
            if(!pkcs10req.startsWith(CaUtils.CERT_REQ_HEADER)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(baos);
                out.println(CaUtils.CERT_REQ_HEADER);
                out.println(pkcs10req.trim());
                out.println(CaUtils.CERT_REQ_FOOTER);
                out.close();
                toStore = baos.toString();
            } else {
                toStore = pkcs10req;
            }
        } else if(spkac != null && !spkac.equals("")) {
            // Received from a web browser that supports KEYGEN tag
            // Create a Properties object with user supplied values
            Properties csrProps = new Properties();
            csrProps.setProperty("CN", reqCN);
            csrProps.setProperty("OU", reqOU);
            csrProps.setProperty("O", reqO);
            csrProps.setProperty("L", reqL);
            csrProps.setProperty("ST", reqST);
            csrProps.setProperty("C", reqC);
            csrProps.setProperty("SPKAC", spkac);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            csrProps.store(baos, "Request received through CA Helper Application");
            baos.close();
            toStore = baos.toString();
        } else {
            // Did not receive a SignedPublicKeyAndChallenge or a PKCS10 Cerificate Request
            throw new ServletException("Did not receive a SignedPublicKeyAndChallenge or a PKCS10 Cerificate Request. Resubmit your certificate request.");
        }

        // Store the CSR in the Certificate Request Store.
        String csrId = CAHelperUtils.getCertificateRequestStore().storeRequest(null, toStore);

        // Display the CSR Id to the user and confirm the receipt of CSR
        request.setAttribute("id", csrId);
        getServletContext().getRequestDispatcher("/receivedCSR.jsp").forward(request, response);
    }    
}
