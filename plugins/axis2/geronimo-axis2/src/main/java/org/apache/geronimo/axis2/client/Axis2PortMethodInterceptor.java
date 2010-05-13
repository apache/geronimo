/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2.client;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;

import net.sf.cglib.proxy.MethodProxy;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.client.PortMethodInterceptor;

public class Axis2PortMethodInterceptor extends PortMethodInterceptor {

    public Axis2PortMethodInterceptor(Map<Object, EndpointInfo> seiInfoMap) {
        super(seiInfoMap);
    }

    public Object intercept(Object target, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
        Object proxy = super.intercept(target, method, arguments, methodProxy);

        BindingProvider axisProxy = (BindingProvider) proxy;

        List<Handler> handlers =
            (axisProxy.getBinding() != null) ? axisProxy.getBinding().getHandlerChain() : null;
        AxisService axisService =
            (axisProxy.getEndpointDescription() != null) ? axisProxy.getEndpointDescription().getAxisService() : null;

        DescriptionUtils.registerHandlerHeaders(axisService, handlers);

        return proxy;
    }

}