/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.tomcat.cluster;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.geronimo.clustering.ClusteredInvocation;
import org.apache.geronimo.clustering.ClusteredInvocationException;

import com.agical.rmock.extension.junit.RMockTestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class AnAbstractClusteredValveTest extends RMockTestCase {

    private AbstractClusteredValve valve;
    private Request request;
    private Response response;
    private Valve nextValve;

    @Override
    protected void setUp() throws Exception {
        valve = new AbstractClusteredValve("NODE") {
            @Override
            protected ClusteredInvocation newClusteredInvocation(Request request, Response response) {
                return new WebClusteredInvocation(request, response) {
                    public void invoke() throws ClusteredInvocationException {
                        invokeLocally();
                    }
                };
            }
        };
        
        nextValve = (Valve) mock(Valve.class);
        valve.setNext(nextValve);

        request = new Request();
        response = new Response();
    }
    
    public void testSuccessfulInvocation() throws Exception {
        nextValve.invoke(request, response);
        
        startVerification();
        
        valve.invoke(request, response);
    }
    
    public void testIOEIsUnwrapped() throws Exception {
        nextValve.invoke(request, response);
        IOException expected = new IOException();
        modify().throwException(expected);
        
        startVerification();

        try {
            valve.invoke(request, response);
        } catch (IOException e) {
            assertSame(expected, e);
        }
    }
    
    public void testSEIsUnwrapped() throws Exception {
        nextValve.invoke(request, response);
        ServletException expected = new ServletException();
        modify().throwException(expected);
        
        startVerification();
        
        try {
            valve.invoke(request, response);
        } catch (ServletException e) {
            assertSame(expected, e);
        }
    }
    
    public void testCIEIsWrappedAsIOE() throws Exception {
        final ClusteredInvocationException expected = new ClusteredInvocationException();
        valve = new AbstractClusteredValve("NODE") {
            @Override
            protected ClusteredInvocation newClusteredInvocation(Request request, Response response) {
                return new WebClusteredInvocation(request, response) {
                    public void invoke() throws ClusteredInvocationException {
                        throw expected;
                    }
                };
            }
        };
        
        startVerification();
        
        try {
            valve.invoke(request, response);
        } catch (IOException e) {
            assertSame(expected, e.getCause());
        }
    }
    
}
