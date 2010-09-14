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
package org.apache.geronimo.itest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

/**
 * @version $Rev$ $Date$
 */
public class TestServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String testName = request.getParameter("test");
        if (testName == null || !testName.startsWith("test")) {
            throw new ServletException("Invalid test name");
        }
        Method testMethod = null;
        try {
            testMethod = getClass().getMethod(testName, new Class[] {HttpServletRequest.class, HttpServletResponse.class});
        } catch (Exception e1) {
            throw new ServletException("No such test: " + testName);
        }
        try {
            testMethod.invoke(this, new Object[] {request, response});
        } catch (IllegalArgumentException e) {
            throw new ServletException("Error invoking test: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ServletException("Error invoking test: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable root = e.getTargetException();
            ServletException ex = new ServletException("Test '" + testName + "' failed");
            ex.initCause(root);
            throw ex;
        }
        response.setContentType("text/plain");
    }
    
    public void testEjb(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        TestListener.getCallbacks().clear();
        
        InitialContext ctx = new InitialContext();
        //test ejb access using geronimo plan refs
        TestSessionHome home = (TestSessionHome)ctx.lookup("java:comp/env/TestSession");
        boolean result = home.create().testEntityManager();
        httpServletResponse.getOutputStream().print("Test EJB container managed entity manager test OK: " + result);
        result = home.create().testEntityManagerFactory();
        httpServletResponse.getOutputStream().print("\nTest EJB app managed entity manager factory test OK: " + result);
        
        verifyCallbacks();
    }
    
    public void testServlet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        TestListener.getCallbacks().clear();
        
        InitialContext ctx = new InitialContext();
        //test servlet access using spec dd refs
        TestSessionBean bean = new TestSessionBean();
        UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        ut.begin();
        boolean result = bean.testEntityManager();
        httpServletResponse.getOutputStream().print("\nTest servlet container managed entity manager test OK: " + result);
        result = bean.testEntityManagerFactory();
        httpServletResponse.getOutputStream().print("\nTest servlet app managed entity manager factory test OK: " + result);
        ut.commit();
        httpServletResponse.getOutputStream().print("\ncommit OK");
        
        verifyCallbacks();
    }
    
    private static void verifyCallbacks() throws Exception {
        List<String> callbacks;        
        callbacks = getCallbacks("prePersist");
        if (callbacks.size() != 2) {
            throw new Exception("Unexpected number of prePersist callbacks: " + callbacks);
        }
        callbacks = getCallbacks("postPersist");
        if (callbacks.size() != 2) {
            throw new Exception("Unexpected number of postPersist callbacks: " + callbacks);
        }
    }

    private static List<String> getCallbacks(String type) {
        List<String> selected = new ArrayList<String>();
        for (String callback : TestListener.getCallbacks()) {
            if (callback.startsWith(type)) {
                selected.add(callback);
            }
        }
        return selected;
    }
}
