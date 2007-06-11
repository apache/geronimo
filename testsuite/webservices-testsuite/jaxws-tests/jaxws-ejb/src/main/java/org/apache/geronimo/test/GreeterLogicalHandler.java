/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.test;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.WebServiceContext;

import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.Set;
import java.util.TreeSet;
import java.util.Map;

import javax.xml.namespace.QName;

import javax.xml.soap.*;

public class GreeterLogicalHandler implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> { 

    @Resource WebServiceContext context;

    @Resource(name="greeting")
    private String greeting;

    public boolean handleMessage(LogicalMessageContext context) {
        System.out.println(this + " HandleMessage: " + context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY) + " " + greeting);
        System.out.println(context.getMessage().getPayload());
        return true;
    }

    @PostConstruct
    public void init() {
        System.out.println(this + " init " + context);
    }

    @PreDestroy
    public void destroy() {
        System.out.println(this + " destroy");
    }
    
    public void init(Map<String,Object> config) {
    }
    
    public boolean handleFault(LogicalMessageContext context) {
        System.out.println(this + " handleFault");
        return true;
    }
    
    public void close(MessageContext context) {
        System.out.println(this + " close");
    }

}
