/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.axis2.ejb;

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.server.dispatcher.ProviderDispatcher;

public class EJBProviderDispatcher extends ProviderDispatcher {

    private InvocationContext invContext;

    public EJBProviderDispatcher(Class<?> serviceImplClass, InvocationContext invContext) {
        super(serviceImplClass, getDummyInstance(serviceImplClass));
        this.invContext = invContext;
    }

    //  TODO: change ProviderDispatcher so that instance is not required
    private static Object getDummyInstance(Class<?> serviceImplClass) {
        try {
            return serviceImplClass.newInstance();
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException("Failed to create provider instance");
        }
    }

    @Override
    protected Object invokeTargetOperation(Method method, Object[] args) throws Throwable {
        this.invContext.setParameters(args);
        return this.invContext.proceed();
    }

}
