/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel;

import java.util.Collections;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GOperationInfo;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/16 23:31:21 $
 */
public class MockGBean implements MockEndpoint {
    private static final GBeanInfo GBEAN_INFO;
    private final String name;
    private String value;

    private MockEndpoint endpoint;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("MockGBean", MockGBean.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Name", true));
        infoFactory.addAttribute(new GAttributeInfo("Value", true));
        infoFactory.addOperation(new GOperationInfo("checkResource", new String[]{"java.lang.String"}));
        infoFactory.addOperation(new GOperationInfo("checkEndpoint"));
        infoFactory.addOperation(new GOperationInfo("doSomething", new String[]{"java.lang.String"}));
        infoFactory.addEndpoint(new GEndpointInfo("MockEndpoint", MockEndpoint.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(Collections.singletonList("Name"), Collections.singletonList(String.class)));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public MockGBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MockEndpoint getMockEndpoint() {
        return endpoint;
    }

    public void setMockEndpoint(MockEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public boolean checkResource(String name) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(name) != null;
    }

    public String doSomething(String name) {
        return name;
    }

    public String checkEndpoint() {
        if (endpoint == null) {
            return "no endpoint";
        }
        return endpoint.doSomething("endpointCheck");
    }
}
