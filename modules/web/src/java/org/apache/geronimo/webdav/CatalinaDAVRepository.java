/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.webdav;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.DirContext;

import org.apache.catalina.Globals;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.naming.resources.FileDirContext;

/**
 * DAVRepository implementation using the Tomcat WebDAV servlet as the
 * processing servlet.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/20 14:58:08 $
 */
public class CatalinaDAVRepository
    implements DAVRepository, GBean
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
        if ( null == aRoot ) {
            throw new IllegalArgumentException("Root MUST be specified.");
        } else if ( null == aContext ) {
            throw new IllegalArgumentException("Context MUST be specified.");
        }
        context = aContext;
        host = anHost;

        if ( !aRoot.isDirectory() ) {
            throw new IllegalArgumentException(aRoot.getAbsolutePath() +
                " does not exist.");
        } 
        root = aRoot;
        dirContext = new FileDirContext();
            ((FileDirContext)dirContext).setDocBase(root.getAbsolutePath());

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
            CatalinaDAVRepository.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Root", true));
        infoFactory.addAttribute(new GAttributeInfo("Context", true));
        infoFactory.addAttribute(new GAttributeInfo("Host", true));
        infoFactory.addAttribute(new GAttributeInfo("HandlingServlet"));
        infoFactory.addAttribute(new GAttributeInfo("ServletContextAttr"));
        infoFactory.addAttribute(new GAttributeInfo("ServletInitParam"));
        infoFactory.setConstructor(new GConstructorInfo(
            Arrays.asList(new Object[] {"Root", "Context", "Host"}),
            Arrays.asList(new Object[] {File.class, String.class, String.class})));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
