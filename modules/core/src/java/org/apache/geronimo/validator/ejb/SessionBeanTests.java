/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.validator.ejb;

import java.lang.reflect.Modifier;
import javax.ejb.EJBHome;
import javax.ejb.SessionBean;
import org.apache.geronimo.validator.ValidationResult;
import org.apache.geronimo.validator.ValidationContext;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;

/**
 * Some basic tests for session beans.  Right now this is not exhaustive, but
 * it's an example of how the tests can be written.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:44 $
 */
public class SessionBeanTests extends BaseEjbJarTest {
    protected SessionBeanType bean;

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
        bean = (SessionBeanType) context.getCurrentNode();
        return super.initialize(context);
    }

    /**
     * Check that the bean implementation class exists and meets certain minimum
     * criteria.
     */
    public ValidationResult testBeanImplementationClass() {
        Class cls = loadClass(bean.getEjbClass().getStringValue(), "Session Bean Implementation Class");
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
        Class cls = loadClass(bean.getHome().getStringValue(), "Session Bean Home Class");
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
