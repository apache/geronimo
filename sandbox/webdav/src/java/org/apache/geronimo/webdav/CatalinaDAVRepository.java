/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.webdav;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.directory.DirContext;

import org.apache.catalina.Globals;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.naming.resources.FileDirContext;

/**
 * DAVRepository implementation using the Tomcat WebDAV servlet as the
 * processing servlet.
 *
 * @version $Rev$ $Date$
 */
public class CatalinaDAVRepository
    implements DAVRepository, GBeanLifecycle
{

    private static final Log log = LogFactory.getLog(CatalinaDAVRepository.class);

    private static final Class HANDLING_SERVLET = WebdavServlet.class;

    private final static GBeanInfo GBEAN_INFO;

    /**
     * DirContext abstracting the repository exposed by this repository.
     */
    private final DirContext dirContext;

    /**
     * Root of the repository.
     */
    private final File root;

    /**
     * Host filter.
     */
    private final String host;

    /**
     * Servlet context.
     */
    private final String context;

    /**
     * Servlet context attribute name to value.
     */
    private final Map servletContextAttr;

    /**
     * Servlet init parameter name to value.
     */
    private final Map servletInitParam;

    /**
     * Builds a DAVRepository relying on Tomcat WebDAV servlet in order to
     * process the WebDAV request.
     *
     * @param aRoot Root of the directory/DirContext exposed by this repository.
     * @param aContext Context within which the servlet should be mounted.
     * @param anHost Host filter, if any.
     */
    public CatalinaDAVRepository(File aRoot, String aContext, String anHost) {
        if (null == aRoot) {
            throw new IllegalArgumentException("Root MUST be specified.");
        } else if (null == aContext) {
            throw new IllegalArgumentException("Context MUST be specified.");
        }
        context = aContext;
        host = anHost;

        if (!aRoot.isDirectory()) {
            throw new IllegalArgumentException(aRoot.getAbsolutePath() +
                    " does not exist.");
        }
        root = aRoot;
        dirContext = new FileDirContext();
        ((FileDirContext) dirContext).setDocBase(root.getAbsolutePath());

        servletContextAttr = new HashMap();
        servletContextAttr.put(Globals.RESOURCES_ATTR, dirContext);

        servletInitParam = new HashMap();
        servletInitParam.put("readonly", "false");
        servletInitParam.put("listings", "true");
    }

    public Class getHandlingServlet() {
        return HANDLING_SERVLET;
    }

    public String getHost() {
        return host;
    }

    public String getContext() {
        return context;
    }

    /**
     * Gets the root of the directory exposed by this repository.
     *
     * @return Root of the exposed directory.
     */
    public File getRoot() {
        return root;
    }

    public DirContext getDirContext() {
        return dirContext;
    }

    public Map getServletContextAttr() {
        return Collections.unmodifiableMap(servletContextAttr);
    }

    public Map getServletInitParam() {
        return Collections.unmodifiableMap(servletInitParam);
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Starting Catalina DAV Repository");
    }

    public void doStop() throws WaitingException {
        log.info("Stopping Catalina DAV Repository");
    }

    public void doFail() {
        log.info("Failing Catalina DAV Repository");
    }

    static {
        GBeanInfoFactory infoFactory =
                new GBeanInfoFactory("DAV Repository - Catalina WebDAV Servlet",
                        CatalinaDAVRepository.class);
        infoFactory.addAttribute("Root", File.class, true);
        infoFactory.addAttribute("Context", String.class, true);
        infoFactory.addAttribute("Host", String.class, true);
        infoFactory.addAttribute("HandlingServlet", Class.class, false);
        infoFactory.addAttribute("ServletContextAttr", Map.class, false);
        infoFactory.addAttribute("ServletInitParam", Map.class, false);
        infoFactory.setConstructor(new String[]{"Root", "Context", "Host"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
