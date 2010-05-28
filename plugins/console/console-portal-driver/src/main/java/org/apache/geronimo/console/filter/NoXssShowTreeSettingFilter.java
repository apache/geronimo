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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class NoXssShowTreeSettingFilter implements Filter {

	private static final String NOXSS_SHOW_TREE = "noxssShowTree";

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {
    	
    	if (request instanceof HttpServletRequest && request.getParameterMap()!=null && !request.getParameterMap().containsKey(NOXSS_SHOW_TREE)) {
    	
    	    NoXssShowTreeSettingRequest wrapper = new  NoXssShowTreeSettingRequest((HttpServletRequest)request);

    	    filterChain.doFilter(wrapper, response);
    	}else{
            filterChain.doFilter(request, response);
    	}
    }


    public void init(FilterConfig arg0) throws ServletException {

    }

    protected static class NoXssShowTreeSettingRequest extends HttpServletRequestWrapper {
        
    	private Map<String, String[]> newParamMap = new HashMap<String, String[]>();
    	
        public NoXssShowTreeSettingRequest(HttpServletRequest request) {
            super(request);
            
            
            newParamMap.putAll(request.getParameterMap());
            
            /*
             * To show navigator tree in admin console, we need to set NOXSS_SHOW_TREE to true.
             * 
             */
            String[] noxssShowTree = new String[1];
            noxssShowTree[0]="true";
            newParamMap.put(NOXSS_SHOW_TREE,noxssShowTree);
        }
        
        @Override
        public String getParameter(final String name)
        {
            String[] strArray = newParamMap.get(name);
            if (strArray != null && strArray.length>0)   
            	return strArray[0];
            else  return null;
        }
     
        @Override
        public Map<String, String[]> getParameterMap()
        {
            return Collections.unmodifiableMap(newParamMap);
        }
     
        @Override
        public Enumeration<String> getParameterNames()
        {
            return Collections.enumeration(newParamMap.keySet());
        }
     
        @Override
        public String[] getParameterValues(final String key)
        {
            return newParamMap.get(key);
        } 
    }
}
