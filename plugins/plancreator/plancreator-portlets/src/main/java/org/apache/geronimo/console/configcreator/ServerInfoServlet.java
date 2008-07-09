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
package org.apache.geronimo.console.configcreator;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.console.configcreator.JSR77_Util.ReferredData;
import org.apache.geronimo.management.geronimo.SecurityRealm;

/**
 * @version $Rev$ $Date$
 */
public class ServerInfoServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /* (non-Java-doc)
    * @see javax.servlet.http.HttpServlet#HttpServlet()
    */
    public ServerInfoServlet() {
        super();
    }

    public static String REQUESTED_INFO = "requestedInfo";
    public static String DEPLOYED_EJBS = "deployedEJBs";
    public static String JMS_CONNECTION_FACTORIES = "jmsConnectionFactories";
    public static String JMS_DESTINATIONS = "jmsDestinations";
    public static String JDBC_CONNECTION_POOLS = "jdbcConnectionPools";
    public static String JAVA_MAIL_SESSIONS = "javaMailSessions";
    public static String DEPLOLYED_CREDENTIAL_STORES = "deployedCredentialStores";
    public static String COMMON_LIBS = "commonLibs";
    public static String DEPLOYED_SECURITY_REALMS = "deployedSecurityRealms";

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("bin/obj");
        ObjectOutputStream oout = new ObjectOutputStream(response.getOutputStream());
        String requestedInfo = request.getParameter(REQUESTED_INFO);
        HttpSession session = request.getSession();
        if (DEPLOYED_EJBS.equalsIgnoreCase(requestedInfo)) {
            writeDeployedEJBs(session, oout);
        } else if (JMS_CONNECTION_FACTORIES.equalsIgnoreCase(requestedInfo)) {
            writeJMSConnectionFactories(session, oout);
        } else if (JMS_DESTINATIONS.equalsIgnoreCase(requestedInfo)) {
            writeJMSDestinations(session, oout);
        } else if (JDBC_CONNECTION_POOLS.equalsIgnoreCase(requestedInfo)) {
            writeJDBCConnectionPools(session, oout);
        } else if (JAVA_MAIL_SESSIONS.equalsIgnoreCase(requestedInfo)) {
            writeJavaMailSessions(session, oout);
        } else if (DEPLOLYED_CREDENTIAL_STORES.equalsIgnoreCase(requestedInfo)) {
            writeDeployedCredentialStores(session, oout);
        } else if (COMMON_LIBS.equalsIgnoreCase(requestedInfo)) {
            writeCommonLibs(session, oout);
        } else if (DEPLOYED_SECURITY_REALMS.equalsIgnoreCase(requestedInfo)) {
            writeDeployedSecurityRealms(session, oout);
        }
    }

    protected void writeDeployedEJBs(HttpSession session, ObjectOutputStream oout) throws IOException {
        writeList(JSR77_Util.getDeployedEJBs(session), oout);
    }

    protected void writeJMSConnectionFactories(HttpSession session, ObjectOutputStream oout) throws IOException {
        writeList(JSR77_Util.getJMSConnectionFactories(session), oout);
    }

    protected void writeJMSDestinations(HttpSession session, ObjectOutputStream oout) throws IOException {
        writeList(JSR77_Util.getJMSDestinations(session), oout);
    }

    protected void writeJDBCConnectionPools(HttpSession session, ObjectOutputStream oout) throws IOException {
        writeList(JSR77_Util.getJDBCConnectionPools(session), oout);
    }

    protected void writeJavaMailSessions(HttpSession session, ObjectOutputStream oout) throws IOException {
        writeList(JSR77_Util.getJavaMailSessions(session), oout);
    }

    protected void writeDeployedCredentialStores(HttpSession session, ObjectOutputStream oout) throws IOException {
        writeList(JSR77_Util.getDeployedCredentialStores(session), oout);
    }

    private void writeList(List<ReferredData> list, ObjectOutputStream oout) throws IOException {
        for (int i = 0; i < list.size(); ++i) {
            ReferredData refData = list.get(i);
            oout.writeObject(refData.getPatternName());
            oout.flush();
        }
    }

    protected void writeCommonLibs(HttpSession session, ObjectOutputStream oout) throws IOException {
        List<String> list = JSR77_Util.getCommonLibs(session);
        for (int i = 0; i < list.size(); ++i) {
            String libName = list.get(i);
            oout.writeObject(libName);
            oout.flush();
        }
    }

    protected void writeDeployedSecurityRealms(HttpSession session, ObjectOutputStream oout) throws IOException {
        SecurityRealm[] realms = JSR77_Util.getDeployedSecurityRealms(session);
        for (int i = 0; i < realms.length; ++i) {
            String realmName = realms[i].getRealmName();
            oout.writeObject(realmName);
            oout.flush();
        }
    }
}