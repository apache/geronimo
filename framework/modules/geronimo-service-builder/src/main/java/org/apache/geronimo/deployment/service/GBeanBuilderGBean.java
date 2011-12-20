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


package org.apache.geronimo.deployment.service;

import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */

@GBean(j2eeType = "ModuleBuilder")
public class GBeanBuilderGBean extends AbstractServiceWrapper<GBeanBuilder> implements NamespaceDrivenBuilder {
    public GBeanBuilderGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, GBeanBuilder.class);
    }

    @Override
    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
        get().buildEnvironment(container, environment);
    }

    @Override
    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        get().build(container, applicationContext, moduleContext);
    }

    @Override
    public QNameSet getSpecQNameSet() {
        return get().getSpecQNameSet();
    }

    @Override
    public QNameSet getPlanQNameSet() {
        return get().getPlanQNameSet();
    }

    @Override
    public QName getBaseQName() {
        return get().getBaseQName();
    }
}
