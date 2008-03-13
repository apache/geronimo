/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.naming.deployment;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;

/**
 * @version $Rev$ $Date$
 */
public class UnavailableRefBuilder implements NamingBuilder {
    private final QNameSet unavailableQNameSet;
    private QName unavailableQName;

    public UnavailableRefBuilder(String namespaceURI, String localPart) {
        unavailableQName = new QName(namespaceURI, localPart);
        unavailableQNameSet = QNameSet.singleton(unavailableQName);
    }
    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) throws DeploymentException {
        if (specDD != null) {
            checkUnavailable(specDD);
        }
    }

    private void checkUnavailable(XmlObject specDD) throws DeploymentException {
        XmlObject[] specRefs = specDD.selectChildren(unavailableQNameSet);
        if (specRefs.length > 0) {
            throw new DeploymentException("This server cannot deploy references of type " + unavailableQName);
        }
    }

    public void initContext(XmlObject specDD, XmlObject plan, Module module) throws DeploymentException {
        checkUnavailable(specDD);
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        checkUnavailable(specDD);
    }

    public int getPriority() {
        return NORMAL_PRIORITY;
    }
    
    public QNameSet getSpecQNameSet() {
        return unavailableQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(UnavailableRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("specNamespaceURI", String.class, true, true);
        infoBuilder.addAttribute("specLocalPart", String.class, true, true);

        infoBuilder.setConstructor(new String[] {"specNamespaceURI", "specLocalPart"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
