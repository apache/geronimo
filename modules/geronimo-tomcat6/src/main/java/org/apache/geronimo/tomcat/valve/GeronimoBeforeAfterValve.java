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
package org.apache.geronimo.tomcat.valve;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;

public class GeronimoBeforeAfterValve extends ValveBase{
    
    private final BeforeAfter beforeAfter;
    private final int contextIndexCount;

    public GeronimoBeforeAfterValve(BeforeAfter beforeAfter, int contextIndexCount) {
        this.beforeAfter = beforeAfter;
        this.contextIndexCount = contextIndexCount;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        Object context[] = new Object[contextIndexCount];
        
        if (beforeAfter != null){
            beforeAfter.before(context, request, response, BeforeAfter.EDGE_SERVLET);
        }
        
        // Pass this request on to the next valve in our pipeline
        getNext().invoke(request, response);
        
        if (beforeAfter != null){
            beforeAfter.after(context, request, response, 0);
        }
        
    }

}
