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

package org.apache.geronimo.openejb.deployment.ejbref;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefDocument;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 475950 $ $Date: 2006-11-16 14:18:14 -0800 (Thu, 16 Nov 2006) $
 */
public class RemoteEjbRefBuilder extends AbstractEjbRefBuilder {
    private static final QName GER_EJB_REF_QNAME = GerEjbRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_REF_QNAME_SET = QNameSet.singleton(GER_EJB_REF_QNAME);

    private final QNameSet ejbRefQNameSet;

    public RemoteEjbRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        ejbRefQNameSet = buildQNameSet(eeNamespaces, "ejb-ref");
    }

    public QNameSet getSpecQNameSet() {
        return ejbRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_EJB_REF_QNAME_SET;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(ejbRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        JndiConsumer jndiConsumer = createJndiConsumer(specDD);
        bindContext(module, jndiConsumer, componentContext);
    }

    protected JndiConsumer createJndiConsumer(XmlObject specDD) throws DeploymentException {
        List<EjbRefType> ejbRefs = convert(specDD.selectChildren(ejbRefQNameSet), J2EE_CONVERTER, EjbRefType.class, EjbRefType.type);

        // build jndi consumer
        JndiConsumer jndiConsumer = new ApplicationClient();
        for (EjbRefType xmlbeansRef : ejbRefs) {
            // create the ejb-ref
            EjbRef ref = new EjbRef();
            jndiConsumer.getEjbRef().add(ref);

            // ejb-ref-name
            ref.setEjbRefName(getStringValue(xmlbeansRef.getEjbRefName()));

            // ejb-ref-type
            String refType = getStringValue(xmlbeansRef.getEjbRefType());
            if ("SESSION".equalsIgnoreCase(refType)) {
                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.SESSION);
            } else if ("ENTITY".equalsIgnoreCase(refType)) {
                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.ENTITY);
            }

            // home
            ref.setHome(getStringValue(xmlbeansRef.getHome()));

            // remote
            ref.setRemote(getStringValue(xmlbeansRef.getRemote()));

            // ejb-link
            ref.setEjbLink(getStringValue(xmlbeansRef.getEjbLink()));

            // mapped-name
            ref.setMappedName(getStringValue(xmlbeansRef.getMappedName()));

            // injection-targets
            if (xmlbeansRef.getInjectionTargetArray() != null) {
                for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
                    InjectionTarget injectionTarget = new InjectionTarget();
                    injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
                    injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
                    ref.getInjectionTarget().add(injectionTarget);
                }
            }
        }
        return jndiConsumer;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(RemoteEjbRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
