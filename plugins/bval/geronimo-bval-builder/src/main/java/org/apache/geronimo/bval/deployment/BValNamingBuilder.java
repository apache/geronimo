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


package org.apache.geronimo.bval.deployment;

import java.util.Collections;
import java.util.Map;

import javax.validation.ValidatorFactory;

import org.apache.geronimo.bval.DefaultValidatorFactoryReference;
import org.apache.geronimo.bval.DefaultValidatorReference;
import org.apache.geronimo.bval.ValidatorFactoryResourceReference;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class BValNamingBuilder extends AbstractNamingBuilder {

    public BValNamingBuilder(@ParamAttribute(name = "defaultEnvironment")Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    @Override
    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) {
        // we always merge our information
        return true;
    }

    @Override
    public void buildNaming(JndiConsumer specDD, XmlObject xmlObject1, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        try {
            // perform a lookup on the bound GBean and add this as a resource reference.  If
            // we can't find one, then bind in a default validator.
            EARContext moduleContext = module.getEarContext();
            AbstractName abstractName = moduleContext.getNaming().createChildName(module.getModuleName(), "ValidatorFactory", NameFactory.VALIDATOR_FACTORY);
            // this verifies that the bean exists...if not, bind to a default factory reference.
            moduleContext.getGBeanInstance(abstractName);
            String osgiJndiName = module.getEarContext().getNaming().toOsgiJndiName(abstractName);
            String filter = "(osgi.jndi.service.name=" + osgiJndiName + ')';
            put("java:comp/ValidatorFactory", new ValidatorFactoryResourceReference(filter, ValidatorFactory.class.getName()), ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget>emptySet(), sharedContext);
        } catch (GBeanNotFoundException e) {
            // if we can't find one on the module, then bind to a default validator factory
            put("java:comp/ValidatorFactory", new DefaultValidatorFactoryReference(), ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget>emptySet(), sharedContext);
        }
        put("java:comp/Validator", new DefaultValidatorReference(), ReferenceType.RESOURCE_ENV, module.getJndiContext(), Collections.<InjectionTarget>emptySet(), sharedContext);
    }

    @Override
    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    @Override
    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }
}
