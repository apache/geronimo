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
package org.apache.geronimo.console.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This filter is used to
 *   1,Keep a hash-->url mapping
 *   2,Redirect to certain url based on hash parameter in request* 
 * 
 * @version $Rev$ $Date$
 */
public class RedirectByHashFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RedirectByHashFilter.class);
    
    private static final Random random = new Random(System.currentTimeMillis());
    
    private static final Map<String,String> hashToRedirectURL=new WeakHashMap<String,String>();
    private static final Map<String,String> redirectURLToHash=new WeakHashMap<String,String>();
    
    private static final String NOXSS_HASH_OF_PAGE_TO_REDIRECT = "noxssPage";
    private static final String NOXSS_SHOW_TREE = "noxssShowTree";
    private static final String HASH_OF_CURRENT_PORTAL_PAGE = "hashOfCurrentPortalPage";
    
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {

        //Generate hash for pure portlets page but not the index page.
        if (request instanceof HttpServletRequest && request.getParameter(NOXSS_SHOW_TREE) == null) {

            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            String currentRequestURI = httpServletRequest.getServletPath()+httpServletRequest.getPathInfo();

            String hash;
            
            if (redirectURLToHash.get(currentRequestURI) == null) {

                    hash = Long.toString(random.nextLong());   
                    
                    //ensure the key is unique
                    while (hashToRedirectURL.keySet().contains(hash)) {
                        hash = Long.toString(random.nextLong());
                    }
                
                    hashToRedirectURL.put(hash, currentRequestURI);
                    redirectURLToHash.put(currentRequestURI, hash);
                
            } else {       
                    hash = redirectURLToHash.get(currentRequestURI);     
            }
            
            log.debug("Hash value for page:"+currentRequestURI+" is:"+hash);
            
            //this attribute will be used to add hash to index page.
            request.setAttribute(HASH_OF_CURRENT_PORTAL_PAGE,hash);

        }
        
        String hashOfPageToRedirect = request.getParameter(NOXSS_HASH_OF_PAGE_TO_REDIRECT);
        
        //Redirect index page url that contain noxssPage=xxxxxx to the real destination.
        if (hashOfPageToRedirect != null && request.getParameter(NOXSS_SHOW_TREE) != null) {

            String pageToRedirect = hashToRedirectURL.get(hashOfPageToRedirect) + "?"+NOXSS_SHOW_TREE+"=true";
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
           
            //log.info("Redirecting to:" + pageToRedirect+" according to hash:"+hashOfPageToRedirect);
            
           // pageToRedirect=pageToRedirect.substring("/console".length());
            log.debug("Redirecting to:" + pageToRedirect+" according to hash:"+hashOfPageToRedirect);
           // request.getParameterMap().remove(NOXSS_HASH_OF_PAGE_TO_REDIRECT);
            request.getRequestDispatcher(pageToRedirect).forward(request, response);
            
            return;
            //httpServletResponse.sendRedirect(pageToRedirect);
            
        } else {
            
            log.debug("no redirect for:" + ((HttpServletRequest)request).getRequestURL());
            filterChain.doFilter(request, response);
        }    


    }

    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub   
    }

}
