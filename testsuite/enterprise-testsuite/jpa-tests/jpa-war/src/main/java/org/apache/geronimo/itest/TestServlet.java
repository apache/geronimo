/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.itest;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.NotSupportedException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;


/**
 * @version $Rev$ $Date$
 */
public class TestServlet extends HttpServlet {

    public void init() {
        System.out.println("Test Servlet init");
    }

    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.getOutputStream().print("TestServlet\n");
        try {
            InitialContext ctx = new InitialContext();

            //test ejb access using geronimo plan refs
            TestSessionHome home = (TestSessionHome)ctx.lookup("java:comp/env/TestSession");
            boolean result = home.create().testEntityManager();
            httpServletResponse.getOutputStream().print("Test EJB container managed entity manager test OK: " + result);
            result = home.create().testEntityManagerFactory();
            httpServletResponse.getOutputStream().print("\nTest EJB app managed entity manager factory test OK: " + result);

            //test servlet access using spec dd refs
            TestSessionBean bean = new TestSessionBean();
            UserTransaction ut = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
            ut.begin();
            result = bean.testEntityManager();
            httpServletResponse.getOutputStream().print("\nTest servlet container managed entity manager test OK: " + result);
            result = bean.testEntityManagerFactory();
            httpServletResponse.getOutputStream().print("\nTest servlet app managed entity manager factory test OK: " + result);
            ut.commit();
            httpServletResponse.getOutputStream().print("\ncommit OK");
        } catch (NamingException e) {
            System.out.print("Exception:");
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (CreateException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (NotSupportedException e) {
            e.printStackTrace();
        } catch (HeuristicMixedException e) {
            e.printStackTrace();
        } catch (HeuristicRollbackException e) {
            e.printStackTrace();
        } catch (RollbackException e) {
            e.printStackTrace();
        }
        httpServletResponse.flushBuffer();
    }


}
