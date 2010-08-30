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

package org.apache.geronimo.openejb.deployment;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.EJBAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.openejb.ClientEjbReference;
import org.apache.geronimo.openejb.GeronimoEjbInfo;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.config.JndiEncInfoBuilder;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision: 475950 $ $Date: 2006-11-16 14:18:14 -0800 (Thu, 16 Nov 2006) $
 */
public class EjbRefBuilder extends AbstractNamingBuilder {
    private static final Logger log = LoggerFactory.getLogger(EjbRefBuilder.class);

    private final QNameSet ejbRefQNameSet;
    private final QNameSet ejbLocalRefQNameSet;
    private final URI uri;

    public EjbRefBuilder(Environment defaultEnvironment, String[] eeNamespaces, String host, int port) throws URISyntaxException {
        super(defaultEnvironment);
        if (host != null) {
            uri = new URI("ejbd", null, host, port, null, null, null);
        } else {
            uri = null;
        }

        ejbRefQNameSet = buildQNameSet(eeNamespaces, "ejb-ref");
        ejbLocalRefQNameSet = buildQNameSet(eeNamespaces, "ejb-local-ref");
        ejbRefQNameSet.union(ejbLocalRefQNameSet);
    }

    public QNameSet getSpecQNameSet() {
        return ejbRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) {
        return !specDD.getEjbRef().isEmpty() || !specDD.getEjbLocalRef().isEmpty();
    }

    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        // skip ejb modules... they have alreayd been processed
//        if (module.getType() == ConfigurationModuleType.EJB) {
//            return;
//        }

        // map the refs declared in the vendor plan, so we can match them to the spec references
        //TODO how do we tell openejb about these?
        Map<String, GerEjbRefType> refMap = mapEjbRefs(plan);
        Map<String, GerEjbLocalRefType> localRefMap = mapEjbLocalRefs(plan);

        // Discover and process any @EJB annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {
            processAnnotations(specDD, module);
        }

        Map<String, List<InjectionTarget>> injectionsMap = new HashMap<String, List<InjectionTarget>>();
        for (Map.Entry<String, EjbRef> entry: specDD.getEjbRefMap().entrySet()) {
            injectionsMap.put(entry.getKey(), entry.getValue().getInjectionTarget());
        }
        for (Map.Entry<String, EjbLocalRef> entry: specDD.getEjbLocalRefMap().entrySet()) {
            List<InjectionTarget> injectionTargets = injectionsMap.get(entry.getKey());
            if (injectionTargets != null) {
                injectionTargets.addAll(entry.getValue().getInjectionTarget());
            } else {
                injectionsMap.put(entry.getKey(), entry.getValue().getInjectionTarget());
            }
        }

        Map<String, Object> map = null;
        try {
            EjbModuleBuilder.EarData earData = EjbModuleBuilder.EarData.KEY.get(module.getRootEarContext().getGeneralData());
            Collection<GeronimoEjbInfo> ejbInfos = Collections.emptySet();
            if (earData != null) {
                ejbInfos = earData.getEjbInfos();
            }

            AppInfo appInfo = new AppInfo();
            for (GeronimoEjbInfo ejbInfo : ejbInfos) {
                appInfo.ejbJars.add(ejbInfo.getEjbJarInfo());
            }

            JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(appInfo);
            JndiEncInfo moduleJndi = new JndiEncInfo();
            JndiEncInfo compJndi = new JndiEncInfo();

            String moduleId = module.getName();
            jndiEncInfoBuilder.build(specDD, "GeronimoEnc", moduleId, moduleJndi, compJndi);

            JndiEncInfo ejbEncInfo = new JndiEncInfo();
            ejbEncInfo.ejbReferences.addAll(appInfo.globalJndiEnc.ejbReferences);
            ejbEncInfo.ejbReferences.addAll(appInfo.appJndiEnc.ejbReferences);
            ejbEncInfo.ejbReferences.addAll(moduleJndi.ejbReferences);
            ejbEncInfo.ejbReferences.addAll(compJndi.ejbReferences);
            ejbEncInfo.ejbLocalReferences.addAll(appInfo.globalJndiEnc.ejbLocalReferences);
            ejbEncInfo.ejbLocalReferences.addAll(appInfo.appJndiEnc.ejbLocalReferences);
            ejbEncInfo.ejbLocalReferences.addAll(moduleJndi.ejbLocalReferences);
            ejbEncInfo.ejbLocalReferences.addAll(compJndi.ejbLocalReferences);

            JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(ejbEncInfo, null, module.getName(), getClass().getClassLoader());

            map = jndiEncBuilder.buildMap();
        } catch (OpenEJBException e) {
            throw new DeploymentException(e);
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            // work with names in different namespaces
            if (name.startsWith("global/") ||
                    name.startsWith("app/") ||
                    name.startsWith("module/") ||
                    name.startsWith("comp/")) {
                if (uri != null) {
                    value = createClientRef(value);
                }
                name = "java:" + name;
                if (value instanceof Serializable) {
                    List<InjectionTarget> injections = injectionsMap.get(name);
                    if (injections == null) {
                        log.warn("No entry in ejb-jar.xml for name:\n " + name + "\n Known names:\n " + injectionsMap.keySet());
                        injections = Collections.emptyList();
                    }
                    put(name, value, module.getJndiContext(), injections, sharedContext);
                }
            }
        }
    }

    private Object createClientRef(Object value) {
        if (value instanceof IntraVmJndiReference) {
            IntraVmJndiReference intraVmJndiReference = (IntraVmJndiReference) value;
            String deploymentId = intraVmJndiReference.getJndiName();
            if (deploymentId.startsWith("java:openejb/local/")) {
                deploymentId = deploymentId.substring("java:openejb/local/".length());
            }
            if (deploymentId.startsWith("java:openejb/remote/")) {
                deploymentId = deploymentId.substring("java:openejb/remote/".length());
            }
            if (deploymentId.startsWith("java:openejb/Deployment/")) {
                deploymentId = deploymentId.substring("java:openejb/Deployment/".length());
            }
            ClientEjbReference clientRef = new ClientEjbReference(uri.toString(), deploymentId);
            return clientRef;
        }
        return value;
    }

//    private void addRefs(JndiConsumer jndiConsumer,
//                         List<EjbRefType> ejbRefs,
//                         Map<String, GerEjbRefType> refMap,
//                         List<EjbLocalRefType> ejbLocalRefs,
//                         Map<String, GerEjbLocalRefType> localRefMap,
//                         Map<EARContext.Key, Object> sharedContext) {
//        Set<String> declaredEjbRefs = new TreeSet<String>();
//        for (EjbRef ejbRef : jndiConsumer.getEjbRef()) {
//            declaredEjbRefs.add(ejbRef.getName());
//        }
//        for (EjbRefType xmlbeansRef : ejbRefs) {
//            // skip refs that have already been declared
//            String refName = getStringValue(xmlbeansRef.getEjbRefName());
//            if (declaredEjbRefs.contains(refName)) {
//                continue;
//            }
//
//            // skip corba refs
//            GerEjbRefType ejbRefType = refMap.get(refName);
//            if (ejbRefType != null) {
//                if (ejbRefType.getNsCorbaloc() != null) {
//                    continue;
//                }
//            }
//            // create the ejb-ref
//            EjbRef ref = new EjbRef();
//            // ejb-ref-name
//            ref.setEjbRefName(refName);
//
//            jndiConsumer.getEjbRef().add(ref);
//
//            // ejb-ref-type
//            String refType = getStringValue(xmlbeansRef.getEjbRefType());
//            if ("SESSION".equalsIgnoreCase(refType)) {
//                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.SESSION);
//            } else if ("ENTITY".equalsIgnoreCase(refType)) {
//                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.ENTITY);
//            } else {
//                ref.setRefType(EjbRef.Type.UNKNOWN);
//            }
//
//            // home
//            ref.setHome(getStringValue(xmlbeansRef.getHome()));
//
//            // remote
//            ref.setRemote(getStringValue(xmlbeansRef.getRemote()));
//
//            // ejb-link
//            ref.setEjbLink(getStringValue(xmlbeansRef.getEjbLink()));
//
//            // mapped-name
//            ref.setMappedName(getStringValue(xmlbeansRef.getMappedName()));
//
//            // handle external refs
//            if (ejbRefType != null) {
//                if (ejbRefType.getNsCorbaloc() != null) {
//                    // corba refs are simple delegated back to Geronimo
//                    ref.setMappedName("jndi:java:comp/geronimo/env/" + ref.getEjbRefName());
//                } else if (ejbRefType.getPattern() != null) {
//                    // external ear ref
//                    // set mapped name to the deploymentId of the external ref
//                    GerPatternType pattern = ejbRefType.getPattern();
//                    String module = pattern.getModule();
//                    if (module == null) {
//                        module = pattern.getArtifactId();
//                    }
//                    String ejbName = pattern.getName();
//                    String deploymentId = module.trim() + "/" + ejbName;
//                    ref.setMappedName(deploymentId.trim());
//                }
//            }
//
//            // openejb handling of injection-targets
//            if (xmlbeansRef.getInjectionTargetArray() != null) {
//                for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
//                    InjectionTarget injectionTarget = new InjectionTarget();
//                    injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
//                    injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
//                    ref.getInjectionTarget().add(injectionTarget);
//                }
//            }
//            //geronimo's handling of injection-target
//            addInjections(refName, xmlbeansRef.getInjectionTargetArray(), sharedContext);
//
//        }
//
//        Set<String> declaredEjbLocalRefs = new TreeSet<String>();
//        for (EjbLocalRef ejbLocalRef : jndiConsumer.getEjbLocalRef()) {
//            declaredEjbLocalRefs.add(ejbLocalRef.getName());
//        }
//
//        for (EjbLocalRefType xmlbeansRef : ejbLocalRefs) {
//            // skip refs that have already been declared
//            String refName = getStringValue(xmlbeansRef.getEjbRefName());
//            if (declaredEjbLocalRefs.contains(refName)) {
//                continue;
//            }
//
//            // create the ejb-ref
//            EjbLocalRef ref = new EjbLocalRef();
//            // ejb-ref-name
//            ref.setEjbRefName(refName);
//
//            jndiConsumer.getEjbLocalRef().add(ref);
//
//            // ejb-ref-type
//            String refType = getStringValue(xmlbeansRef.getEjbRefType());
//            if ("SESSION".equalsIgnoreCase(refType)) {
//                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.SESSION);
//            } else if ("ENTITY".equalsIgnoreCase(refType)) {
//                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.ENTITY);
//            }
//
//            // home
//            ref.setLocalHome(getStringValue(xmlbeansRef.getLocalHome()));
//
//            // remote
//            ref.setLocal(getStringValue(xmlbeansRef.getLocal()));
//
//            // ejb-link
//            ref.setEjbLink(getStringValue(xmlbeansRef.getEjbLink()));
//
//            // mapped-name
//            ref.setMappedName(getStringValue(xmlbeansRef.getMappedName()));
//
//            // handle external refs
//            GerEjbLocalRefType ejbLocalRefType = localRefMap.get(ref.getEjbRefName());
//            if (ejbLocalRefType != null && ejbLocalRefType.getPattern() != null) {
//                // external ear ref
//                // set mapped name to the deploymentId of the external ref
//                GerPatternType pattern = ejbLocalRefType.getPattern();
//                String module = pattern.getModule();
//                if (module == null) {
//                    module = pattern.getArtifactId();
//                }
//                String ejbName = pattern.getName();
//                String deploymentId = module.trim() + "/" + ejbName;
//                ref.setMappedName(deploymentId.trim());
//            }
//
//            // openejb handling of injection-targets
//            if (xmlbeansRef.getInjectionTargetArray() != null) {
//                for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
//                    InjectionTarget injectionTarget = new InjectionTarget();
//                    injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
//                    injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
//                    ref.getInjectionTarget().add(injectionTarget);
//                }
//            }
//            //geronimo's handling of injection-target
//            addInjections(refName, xmlbeansRef.getInjectionTargetArray(), sharedContext);
//        }
//    }

    private Map<String, GerEjbRefType> mapEjbRefs(XmlObject plan) {
        Map<String, GerEjbRefType> refMap = new HashMap<String, GerEjbRefType>();

        if (plan == null) {
            return refMap;
        }

        QNameSet qnameSet = QNameSet.singleton(GerEjbRefDocument.type.getDocumentElementName());
        XmlObject[] xmlObjects = plan.selectChildren(qnameSet);
        if (xmlObjects != null) {
            for (XmlObject xmlObject : xmlObjects) {
                GerEjbRefType ref = (GerEjbRefType) xmlObject.copy().changeType(GerEjbRefType.type);
                refMap.put(getJndiName(ref.getRefName().trim()), ref);
            }
        }
        return refMap;
    }

    private Map<String, GerEjbLocalRefType> mapEjbLocalRefs(XmlObject plan) {
        Map<String, GerEjbLocalRefType> refMap = new HashMap<String, GerEjbLocalRefType>();

        if (plan == null) {
            return refMap;
        }

        QNameSet qnameSet = QNameSet.singleton(GerEjbLocalRefDocument.type.getDocumentElementName());
        XmlObject[] xmlObjects = plan.selectChildren(qnameSet);
        if (xmlObjects != null) {
            for (XmlObject xmlObject : xmlObjects) {
                GerEjbLocalRefType ref = (GerEjbLocalRefType) xmlObject.copy().changeType(GerEjbLocalRefType.type);
                refMap.put(getJndiName(ref.getRefName().trim()), ref);
            }
        }
        return refMap;
    }


    // XMLBean uses lame arrays that can be null, so we need an asList that handles nulls
    // Beware Arrays.asList(), it returns an ArrayList lookalike, that is not fully mutable...

    public static <E> List<E> asList(E[] array) {
        if (array == null) {
            return new ArrayList<E>();
        } else {
            return new ArrayList<E>(Arrays.asList(array));
        }
    }

    private void processAnnotations(JndiConsumer ejb, Module module) {
        // Process all the annotations for this naming builder type
        if (EJBAnnotationHelper.annotationsPresent(module.getClassFinder())) {
            try {
                EJBAnnotationHelper.processAnnotations(ejb, module.getClassFinder());
            } catch (Exception e) {
                log.warn("Unable to process @EJB annotations for module" + module.getName(), e);
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EjbRefBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", int.class, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces", "host", "port"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
