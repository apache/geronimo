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
package org.apache.geronimo.validator.ejb;

import java.lang.reflect.Modifier;
import javax.ejb.EJBHome;
import javax.ejb.SessionBean;
import org.apache.geronimo.validator.ValidationResult;
import org.apache.geronimo.validator.ValidationContext;
import org.apache.geronimo.deployment.model.ejb.Session;

/**
 * Some basic tests for session beans.  Right now this is not exhaustive, but
 * it's an example of how the tests can be written.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/02 17:04:20 $
 */
public class SessionBeanTests extends BaseEjbJarTest {
    protected Session bean;

    /**
     * Get called for each session bean element in the standard DD.
     */
    public String getXpath() {
        return "/ejb-jar/enterprise-beans/session";
    }

    /**
     * Trap the actual bean to examine.
     */
    public ValidationResult initialize(ValidationContext context) {
        bean = (Session) context.getCurrentNode();
        return super.initialize(context);
    }

    /**
     * Check that the bean implementation class exists and meets certain minimum
     * criteria.
     */
    public ValidationResult testBeanImplementationClass() {
        Class cls = loadClass(bean.getEjbClass(), "Session Bean Implementation Class");
        ValidationResult result = ValidationResult.PASSED;
        if(!Modifier.isPublic(cls.getModifiers())) {
            result = error("ejb.impl.not.public", result);
        }
        if(Modifier.isFinal(cls.getModifiers())) {
            result = error("ejb.impl.is.final", result);
        }
        if(!SessionBean.class.isAssignableFrom(cls)) {
            result = error("ejb.session.impl.wrong.interface", result);
        }
        if(!Modifier.isAbstract(cls.getModifiers())) {
            result = error("ejb.session.impl.abstract", result);
        }
        return result;
    }

    /**
     * Tests that there's either a local and a remote and either a local home
     * or a home.  todo: make sure you don't get a remote and a local home
     */
    public ValidationResult testLocalOrRemote() {
        ValidationResult result = ValidationResult.PASSED;
        if(bean.getHome() == null && bean.getLocalHome() == null) {
            result = error("ejb.no.home.interface", result);
        }
        if(bean.getRemote() == null && bean.getLocal() == null) {
            result = error("ejb.no.component.interface", result);
        }
        return result;
    }

    /**
     * Check that the home interface class exists and meets certain minimum
     * criteria.
     */
    public ValidationResult testHomeInterface() {
        ValidationResult result = ValidationResult.PASSED;
        if(bean.getHome() == null) {
            return result;
        }
        Class cls = loadClass(bean.getHome(), "Session Bean Home Class");
        if(!EJBHome.class.isAssignableFrom(cls)) {
            result = error("ejb.home.wrong.superclass", result);
        }
        if(!Modifier.isPublic(cls.getModifiers())) {
            result = error("ejb.home.not.public", result);
        }
        if(!Modifier.isInterface(cls.getModifiers())) {
            result = error("ejb.home.not.interface", result);
        }
        return result;
    }
}
