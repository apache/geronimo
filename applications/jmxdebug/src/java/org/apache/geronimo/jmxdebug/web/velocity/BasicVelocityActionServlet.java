/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.jmxdebug.web.velocity;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Enumeration;
import java.io.InputStream;

/**
 *  Simple servlet to dispatch based on 'action'.  Also inits velocity in a
 *  simple way
 * 
 * @version $Id: BasicVelocityActionServlet.java,v 1.1 2004/02/18 15:33:41 geirm Exp $
 */
public abstract class BasicVelocityActionServlet extends HttpServlet {

    /**
     * for dispatch purposes
     */
    private final Class[] args =
            {HttpServletRequest.class, HttpServletResponse.class};

    /**
     *  velocity engine for this servlet
     */
    private VelocityEngine velEngine = new VelocityEngine();

    public static final String DEFAULT_PROPS =
            "org/apache/geronimo/jmxdebug/web/velocity/velocity.defaults";

    /**
     * for dispatching to the method specified...
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws java.io.IOException
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException {

        String actionVerb = getActionVerb();

        String what = req.getParameter(actionVerb);

        if (what == null) {
            what = "defaultAction";
        }

        try {
            Method method = this.getClass().getMethod(what, args);

            Object[] objs = {req, res};
            method.invoke(this, objs);
        }
        catch (NoSuchMethodException nsme) {
            unknownAction(req, res);
        }
        catch (Exception e) {
            log("BasicVelocityActionServlet.service() : exception", e);
        }
    }

    public void init()
        throws ServletException {

        /*
         *  get the default properties from the classloader
         */

        Properties p = new Properties();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPS);

            p.load(is);
        }
        catch (Exception e) {
            log("BasicVelocityActionServlet : default " + DEFAULT_PROPS + " not found.", e);
            throw new ServletException(e);
        }

        /*
         *  run through them and use them
         */

        for (Enumeration en = p.propertyNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();

            velEngine.setProperty(key, p.getProperty(key));
        }

        /*
         *  for now, log to the servlet log
         */

        org.apache.geronimo.jmxdebug.web.velocity.ServletLogger sl = new org.apache.geronimo.jmxdebug.web.velocity.ServletLogger(getServletContext());
        velEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, sl);

        /*
         * set an app context for the webapploader if we are using it
         */

        ServletAppContext vssac = new ServletAppContext(getServletContext());
        velEngine.setApplicationAttribute(WebappLoader.KEY, vssac);

        try {
            velEngine.init();
        }
        catch (Exception e) {
            log("BasicVelocityActionServlet", e);
            throw new ServletException(e);
        }
    }

    /**
     *  Defines the 'action verb' for the app
     * @return
     */
    protected abstract String getActionVerb();

    /**
     * Called when there is a request w/ no action verb
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws java.io.IOException
     */
    public abstract void defaultAction(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException;

    /**
     * Called when there is a request w/ invalid action verb
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws java.io.IOException
     */
    public abstract void unknownAction(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, java.io.IOException;

    protected VelocityEngine getVelocityEngine() {
        return this.velEngine;
    }


    protected boolean renderTemplate(HttpServletRequest req,
                                     HttpServletResponse res,
                                     VelocityContext vc, String template) {
        boolean result = false;

        try {
            Template t = getVelocityEngine().getTemplate(template);
            t.merge(vc, res.getWriter());
            result = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * little wrapper class to safely pass the ServletContext to the loader
     */
    public class ServletAppContext implements WebappLoader.WebappLoaderAppContext {
        ServletContext servletContext = null;

        ServletAppContext(ServletContext sc) {
            servletContext = sc;
        }

        public ServletContext getServletContext() {
            return servletContext;
        }
    }
}
