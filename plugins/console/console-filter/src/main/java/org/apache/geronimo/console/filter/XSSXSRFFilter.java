/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.apache.geronimo.console.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * WebApp protection against XSS and XSRF attacks.
 * 
 * Simple XSS Url attack protection blocking access whenever the request url
 *  contains a &lt; or &quot; character in XSSHandler, was adapted from
 *  Apache JetSpeed v2.1.3 -
 *      jetspeed-2.1.3/src/components/portal/src/java/org/apache/jetspeed/engine/servlet/XXSUrlAttackFilter.java
 *  Modified to include basic XSS POST parameter protection and logging.
 *  
 * Simple XSRF protection via unique session token was added by XSRFHandler.
 * 
 * @version $Rev$ $Date$
 */
public class XSSXSRFFilter implements Filter, HttpSessionListener
{
    private static final Logger log = LoggerFactory.getLogger(XSSXSRFFilter.class);
    private XSSHandler xss = new XSSHandler();
    private XSRFHandler xsrf = new XSRFHandler();
    private boolean enableXSS = true;
    private boolean enableXSRF = true;

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        log.debug("init() called");
        String parmEnableXSS = config.getInitParameter("enableXSS");
        if ((parmEnableXSS != null) && (parmEnableXSS.equalsIgnoreCase("false"))) {
            this.enableXSS = false;
        }
        
        String parmEnableXSRF = config.getInitParameter("enableXSRF");
        if ((parmEnableXSRF != null) && (parmEnableXSRF.equalsIgnoreCase("false"))) {
            this.enableXSRF = false;
        }
        
        String ignoreResources = config.getInitParameter("xsrf.ignorePaths");
        if (ignoreResources != null) {
            xsrf.setIgnorePaths(ignoreResources);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent hse) {
        log.debug("sessionCreated() called for sesId=" + hse.getSession().getId());
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent hse) {
        // when sessions are invalidated, remove them form our map
        log.debug("sessionDestroyed() called for sesId=" + hse.getSession().getId());
        xsrf.destroySession(hse);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) &&
            (response instanceof HttpServletResponse)) {
            HttpServletRequest hreq = (HttpServletRequest)request;
            hreq.setCharacterEncoding("UTF-8");
            String errStr = null;
            
            //--------------------------------------------------------------
            // Check the URI and QueryString for simple XSS attacks
            // Validate any FORM submission with our XSRF protection code
            //--------------------------------------------------------------
            // check the URI/Params first, as they get logged during the XSRF checks
            if (enableXSS && xss.isInvalidURI(hreq)) {
                // Block simple XSS attacks in GET request URIs
                errStr = "XSSXSRFFilter blocked HttpServletRequest due to invalid URI content.";
            }
            else if (enableXSS && xss.isInvalidParameters(hreq)) {
                // Block simple XSS attacks in POST parameters
                errStr = "XSSXSRFFilter blocked HttpServletRequest due to invalid POST content.";
            }
            else if (enableXSRF && xsrf.isInvalidSession(hreq)) {
                // Block simple XSRF attacks on our forms
                errStr = "XSSXSRFFilter blocked HttpServletRequest due to invalid FORM content.";   
            }
            // if we found a problem, return a HTTP 400 error code and message
            if (errStr != null) {
                log.error(errStr);
                // create an error response with our message
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_BAD_REQUEST, errStr);
                // Shouldn't forward to next filter after response committed
                return;
            }
            //-----------------------------------------------
            // Call other filters and eventually the Servlet
            // Update the response with our XSRF FORM protection code
            //-----------------------------------------------
            String replacement = xsrf.getReplacement(hreq);
            ServletResponse whres = response;
            if (replacement != null ) {
                whres = new SubstituteResponseWrapper((HttpServletResponse)response, replacement);
            }
            chain.doFilter(hreq, whres);
        }
        else {
            log.debug("Request not HttpServletRequest and/or Response not HttpServletResponse");
            log.debug("Request: " + request);
            log.debug("Response: " + response);

            //-----------------------------------------------
            // Call other filters and eventually the Servlet
            //-----------------------------------------------
            chain.doFilter(request, response);
        }

    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        log.debug("destroy() called");
        // clear out our session map
        xsrf.clearSessions();
    }
}
