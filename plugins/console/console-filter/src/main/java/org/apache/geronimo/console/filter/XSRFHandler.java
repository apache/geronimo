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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

/**
 * Simple XSRF protection via injecting a hidden unique session token into forms
 * via JavaScript, which can then be used on the form submit by comparing
 * against the expected uniqueId based on the HttpSession id.
 * 
 * See the following for more explanation of XSRF and how adding a unique token
 * in each request can block attackers (no code was used from these sources):
 *    http://www.cgisecurity.com/csrf-faq.html
 *    http://shiflett.org/articles/cross-site-request-forgeries
 * 
 * @version $Rev$ $Date$
 */
public class XSRFHandler
{
    private static final Logger log = LoggerFactory.getLogger(XSRFHandler.class);
    private static final String XSRF_UNIQUEID = "formId";
    private static final String XSRF_JS_FILENAME = "/XSRF.js";
    private static final String XSRF_JS_UNIQUEID = "<%XSRF_UNIQUEID%>";
    private static final String NOXSS_SHOW_TREE = "noxssShowTree";
    private static final String NOXSS_HASH_OF_PAGE_TO_REDIRECT = "noxssPage";

    private Map<String, String> sessionMap = new ConcurrentHashMap<String, String>();
    private Set<String> ignoredPaths = new HashSet<String>();
    private String xsrfJS;

    private Random random = new Random();

    /**
     * Default constructor
     */
    public XSRFHandler() {
        xsrfJS = getFile(XSRF_JS_FILENAME);
        log.debug("loaded xsrf file");
    }

    /**
     * A comma separated list of resource paths that will be ignored during XSRF check.
     * 
     * @param resourceList
     */
    public void setIgnorePaths(String pathList) {
        String values[] = pathList.split(",");
        for (String value : values) {
            ignoredPaths.add(value.trim());
        }
    }
    
    //----- Session handler routines -----

    /**
     * Determines if the HttpServletRequest should be blocked due to 
     * a potential XSRF attack.  Only requests with a QueryString or
     * POST parameters are checked to verify they contain a unique
     * session token that we added via JavaScript on the original response.
     * @param hreq
     * @return String if the session was invalid or null if OK
     */
    public boolean isInvalidSession(HttpServletRequest hreq) {
        HttpSession hses = hreq.getSession(true);
        String uniqueId = getSession(hses);

        if (hses.isNew() || (uniqueId == null)) {
            // New client session, so create and add our uniqueId
            uniqueId = createSession(hses.getId());
            hses.setAttribute(XSRF_UNIQUEID, uniqueId);
            log.info("Created session for uid=" + hreq.getRemoteUser() + " with sessionId=" + hses.getId() + ", uniqueId=" + uniqueId);
            return false;
        }

        if (isIgnoredPath(hreq)) {
            log.debug("Skipped XSRF checking for requestURI=" + hreq.getRequestURI());
            return false;
        }
        
        if ((hreq.getQueryString() != null && hreq.getQueryString().length() > 0)
                || (hreq.getParameterNames().hasMoreElements())) {
            
            if (hreq.getParameterMap().keySet().size() == 1 && hreq.getParameter(NOXSS_SHOW_TREE) != null) {
                return false;
            }
            
            if (hreq.getParameterMap().keySet().size() == 2 && hreq.getParameter(NOXSS_SHOW_TREE) != null
                    && hreq.getParameter(NOXSS_HASH_OF_PAGE_TO_REDIRECT)!=null) {
                return false;
            }
            
            String sesId = (String)hses.getAttribute(XSRF_UNIQUEID);
            String reqId = (String)hreq.getParameter(XSRF_UNIQUEID);
            log.debug("XSRF checking requestURI=" + hreq.getRequestURI());
            // only check if this is a form GET/POST
            if (sesId == null) {
                // Request did not contain the expected session param
                log.warn("Blocked due to missing HttpSession data.");
                return true;
            }
            else if (reqId == null) {
                // Request did not contain the expected session param
                log.warn("Blocked due to missing HttpServletRequest parameter.");
                return true;                
            }
            else if (!reqId.equals(uniqueId)) {
                // The unique Ids didn't match
                log.warn("Blocked due to invalid HttpServletRequest parameter.");
                // TODO - Should we invalidate the session?
                return true;
            }
            else {
                // Unique Ids matched, so let the request thru
                log.debug("Validated sessionId=" + hses.getId() + ", uniqueId=" + uniqueId + ", requestURI=" + hreq.getRequestURI());
            }
        }
        else {
            log.debug("Skipped check due to no QueryString or ParameterNames for requestURI=" + hreq.getRequestURI());
        }
        return false;
    }

    /**
     * When HttpSessions are invalidated, remove them form our map
     * @param hse
     */
    public void destroySession(HttpSessionEvent hse) {
        String sesId = hse.getSession().getId();
        log.info("Removed destroyed sessionId=" + sesId);
        removeSession(sesId);
    }

    /**
     * Allow cleanup of our session map on filter exit
     */
    public void clearSessions() {
        // clear out our session map
        log.debug("Cleaning out sessionMap");
        sessionMap.clear();
    }

    /**
     * Create and return a uniqueId for the given HttpSession id
     * @param sesId
     * @return String holding the unique token, else null if there was no HttpSession
     */
    private String createSession(String sesId) {
        String uniqueId = null;
        if (sesId != null) {
            uniqueId = String.valueOf(random.nextLong());
            sessionMap.put(sesId, uniqueId);        
        }
        return uniqueId;
    }

    /**
     * Get the uniqueId for the given HttpServletRequest.getSession()
     * @param hreq
     * @return String holding the unique token for this session, else null
     */
    private String getSession(HttpServletRequest hreq) {
        HttpSession hses = hreq.getSession(false);
        if (hses != null) {
            return sessionMap.get(hses.getId());
        }
        else {
            return null;
        }
    }

    /**
     * Get the uniqueId for the given HttpSession id
     * @param hses
     * @return String holding the unique token for this session, else null
     */
    private String getSession(HttpSession hses) {
        if (hses != null) {
            return sessionMap.get(hses.getId());
        }
        else {
            return null;
        }
    }

    /**
     * Remove the given HttpSession id from our session map
     * @param sesId
     */
    private void removeSession(String sesId) {
        if (sesId != null) {
            sessionMap.remove(sesId);        
        }
    }
    
    private boolean isIgnoredPath(HttpServletRequest hreq) {
        if (!ignoredPaths.isEmpty() && "GET".equals(hreq.getMethod())) {
            String path = hreq.getServletPath();
            if (hreq.getPathInfo() != null) {
                path = path + hreq.getPathInfo();
            }
            return ignoredPaths.contains(path);
        }
        return false;
    }

    //----- Response handler routines -----
    /**
     * Get XSRF JavaScript containing the unique session token.
     * 
     * @param hreq
     */
    public String getReplacement(HttpServletRequest hreq) throws IOException {
        // get the JavaScript file we're going to append to it
        String uniqueId = getSession(hreq);
        if (xsrfJS == null) {
            log.error("No JavaScript to append to the response!");
            return null;
        } else if (uniqueId == null) {
            // this should only happen for user logout or session timeout, so
            // ignore
            log.debug("HttpSession is null!");
            return null;
        } else {
            // update the JavaScript with the uniqueId for this session
            return xsrfJS.replace(XSRF_JS_UNIQUEID, uniqueId);
        }
    }

    /**
     * Helper function to retrieve our JavaScript from the classpath.
     * @param filename
     * @return String containing the JavaScript content, else null
     */
    private String getFile(String filename) {
        InputStream is = getClass().getResourceAsStream(filename);
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(is, "UTF-8");
                char[] buffer = new char[1024];
                int i = 0;
                while ((i = reader.read(buffer)) > 0) {
                    sb.append(buffer, 0, i);
                }
            } catch (IOException ioe) {
                log.error("Could not read resource=" + filename, ioe);
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (IOException ignored) {}
                }
                try { is.close(); } catch (IOException ignored) {}
            }
            return sb.toString();
        } else {
            log.error("Could not load required resource=" + filename);
            return null;
        }
    }

}
