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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Simple servlet to dispatch based on 'action'.  Also inits velocity in a
 * simple way
 *
 * @version $Rev$ $Date$
 */
public abstract class BasicVelocityActionServlet extends HttpServlet {
    public static final String DEFAULT_PROPS = "org/apache/geronimo/jmxdebug/web/velocity/velocity.defaults";

    /**
     * for dispatch purposes
     */
    private static final Class[] DISPATCH_ARGS = {HttpServletRequest.class, HttpServletResponse.class};

    /**
     * velocity engine for this servlet
     */
    private final VelocityEngine velocityEngine = new VelocityEngine();

    /**
     * for dispatching to the method specified...
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        // get the action
        String action = req.getParameter(getActionVerb());
        if (action == null || action.length() == 0) {
            action = "defaultAction";
        }

        // look up and invoke the method with the action name
        try {
            Method method = this.getClass().getMethod(action, DISPATCH_ARGS);
            method.invoke(this, new Object[]{req, res});
        } catch (NoSuchMethodException nsme) {
            unknownAction(req, res);
        } catch (Exception e) {
            log("BasicVelocityActionServlet.service() : exception", e);
        }
    }

    public void init() throws ServletException {
        Properties p = new Properties();

        // load the default properties file using the classloader
        try {
            p.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPS));
        } catch (Exception e) {
            log("BasicVelocityActionServlet : default " + DEFAULT_PROPS + " not found.", e);
            throw new ServletException(e);
        }

        // apply default propertis to velocitty engine
        for (Iterator iterator = p.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            velocityEngine.setProperty(key, p.getProperty(key));
        }

        // hook velocity logger up to the servlet logger
        ServletLogger sl = new ServletLogger(getServletContext());
        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, sl);

        // set an app context for the webapploader if we are using it
        ServletAppContext vssac = new ServletAppContext(getServletContext());
        velocityEngine.setApplicationAttribute(WebappLoader.KEY, vssac);

        // start the velocity engine
        try {
            velocityEngine.init();
        } catch (Exception e) {
            log("BasicVelocityActionServlet", e);
            throw new ServletException(e);
        }
    }

    /**
     * Defines the 'action verb' for the app
     */
    protected abstract String getActionVerb();

    /**
     * Called when there is a request w/ no action verb
     */
    public abstract void defaultAction(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException;

    /**
     * Called when there is a request w/ invalid action verb
     */
    public abstract void unknownAction(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException;

    protected VelocityEngine getVelocityEngine() {
        return this.velocityEngine;
    }


    protected boolean renderTemplate(HttpServletRequest req,
            HttpServletResponse res,
            VelocityContext velocityContext,
            String templateName) {

        try {
            Template template = getVelocityEngine().getTemplate(templateName);
            template.merge(velocityContext, res.getWriter());
            return true;
        } catch (Exception e) {
            log("Error rendering template: " + templateName, e);
        }

        return false;
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
