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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.ca.helper.util.CAHelperUtils;

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

        if(spkac == null || spkac.equals("")) {
            // browser did not generate SignedPublicKeyAndChallenge
            throw new ServletException("Browser did not generate SignedPublicKeyAndChallenge. Resubmit your certificate request.");
        }
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

        // Store the CSR in the Certificate Request Store.
        String csrId = CAHelperUtils.getCertificateRequestStore().storeRequest(null, baos.toString());

        // Display the CSR Id to the user and confirm the receipt of CSR
        request.setAttribute("id", csrId);
        getServletContext().getRequestDispatcher("/receivedCSR.jsp").forward(request, response);
    }    
}
