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
package org.apache.geronimo.deployment.model.web;

import org.apache.geronimo.deployment.model.j2ee.Displayable;
import org.apache.geronimo.deployment.model.j2ee.RunAs;
import org.apache.geronimo.deployment.model.j2ee.SecurityRoleRef;

/**
 * JavaBean for the web.xml tag servlet
 *
 * @version $Revision: 1.1 $
 */
public class Servlet extends Displayable {
    private String servletName;
    private String servletClass;
    private String jspFile;
    private InitParam[] initParam = new InitParam[0];
    private Integer loadOnStartup;
    private RunAs runAs;
    private SecurityRoleRef[] securityRoleRef = new SecurityRoleRef[0];

    public InitParam[] getInitParam() {
        return initParam;
    }

    public InitParam getInitParam(int i) {
        return initParam[i];
    }

    public void setInitParam(InitParam[] initParam) {
        this.initParam = initParam;
    }

    public void setInitParam(int i, InitParam initParam) {
        this.initParam[i] = initParam;
    }

    public String getJspFile() {
        return jspFile;
    }

    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public Integer getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(Integer loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public RunAs getRunAs() {
        return runAs;
    }

    public void setRunAs(RunAs runAs) {
        this.runAs = runAs;
    }

    public SecurityRoleRef[] getSecurityRoleRef() {
        return securityRoleRef;
    }

    public SecurityRoleRef getSecurityRoleRef(int i) {
        return securityRoleRef[i];
    }

    public void setSecurityRoleRef(SecurityRoleRef[] securityRoleRef) {
        this.securityRoleRef = securityRoleRef;
    }

    public void setSecurityRoleRef(int i, SecurityRoleRef securityRoleRef) {
        this.securityRoleRef[i] = securityRoleRef;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }
}
