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


package org.apache.geronimo.security;

import javax.xml.soap.SOAPMessage;
import javax.servlet.http.HttpServletRequest;
import javax.ejb.EnterpriseBean;

/**
 * @version $Rev$ $Date$
 */
public class ThreadData {
    private Callers callers;
    private HttpServletRequest request;
    private SOAPMessage soapMessage;
    private EnterpriseBean bean;
    private Object[] args;

    public Callers getCallers() {
        return callers;
    }

    public void setCallers(Callers callers) {
        this.callers = callers;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }

    public void setSoapMessage(SOAPMessage soapMessage) {
        this.soapMessage = soapMessage;
    }

    public EnterpriseBean getBean() {
        return bean;
    }

    public void setBean(EnterpriseBean bean) {
        this.bean = bean;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
