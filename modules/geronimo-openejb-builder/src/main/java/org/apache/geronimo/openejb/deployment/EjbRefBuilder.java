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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.Local;
import javax.ejb.Remote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.annotation.EJBAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.openejb.ClientEjbReference;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.geronimo.xbeans.javaee.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.JndiEncInfoBuilder;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.SessionBean;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 475950 $ $Date: 2006-11-16 14:18:14 -0800 (Thu, 16 Nov 2006) $
 */
public class EjbRefBuilder extends AbstractNamingBuilder {
    private static final Log log = LogFactory.getLog(EjbRefBuilder.class);

    private final QNameSet ejbRefQNameSet;
    private final QNameSet ejbLocalRefQNameSet;
    private final URI uri;

    public EjbRefBuilder(Environment defaultEnvironment, String[] eeNamespaces, String host, int port) throws URISyntaxException {
        super(defaultEnvironment);
        if (host != null) {
            uri = new URI("ejb", null, host, port, null, null, null);
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

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(ejbRefQNameSet).length > 0 || specDD.selectChildren(ejbLocalRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        JndiConsumer consumer = createJndiConsumer(specDD, componentContext);

        // Discover and process any @EJB annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {
            processAnnotations(module);

            // Augment the JndiConsumer with any discovered annotations
            augmentJndiConsumer(module, consumer, componentContext);
        }

//      processWebEjbAnnotations(module, consumer);

        Map<String, Object> map = null;
        try {
            EjbModuleBuilder.EarData earData = (EjbModuleBuilder.EarData) module.getRootEarContext().getGeneralData().get(EjbModuleBuilder.EarData.class);
            Collection<EjbJarInfo> ejbJars = Collections.emptySet();
            if (earData != null) {
                ejbJars = earData.getEjbJars();
            }
            JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(ejbJars);
            JndiEncInfo jndiEncInfo = jndiEncInfoBuilder.build(consumer, "GeronimoEnc");
            JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(jndiEncInfo, module.getName());
            map = jndiEncBuilder.buildMap();
        } catch (OpenEJBException e) {
            throw new DeploymentException(e);
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            // work with names prefixed with java:comp/
            if (name.startsWith("java:comp/")) {
                name = name.substring("java:comp/".length());
            }

            // if this is a ref it will be prefixed with env/
            if (name.startsWith("env/")) {
                if (uri != null) {
                    value = createClientRef(value);
                }
                getJndiContextMap(componentContext).put(name, value);
            }
        }
    }

    private Object createClientRef(Object value) {
        IntraVmJndiReference intraVmJndiReference = (IntraVmJndiReference) value;
        String deploymentId = intraVmJndiReference.getJndiName();
        if (deploymentId.startsWith("java:openejb/ejb/")) {
            deploymentId = deploymentId.substring("java:openejb/ejb/".length());
        }
        ClientEjbReference clientRef = new ClientEjbReference(uri.toString(), deploymentId);
        return clientRef;
    }

    protected JndiConsumer createJndiConsumer(XmlObject specDD, Map componentContext) throws DeploymentException {
        List<EjbRefType> ejbRefs = convert(specDD.selectChildren(ejbRefQNameSet), JEE_CONVERTER, EjbRefType.class, EjbRefType.type);
        List<EjbLocalRefType> ejbLocalRefs = convert(specDD.selectChildren(ejbLocalRefQNameSet), JEE_CONVERTER, EjbLocalRefType.class, EjbLocalRefType.type);

        // build jndi consumer
        JndiConsumer jndiConsumer = new SessionBean();
        for (EjbRefType xmlbeansRef : ejbRefs) {
            // create the ejb-ref
            EjbRef ref = new EjbRef();
            jndiConsumer.getEjbRef().add(ref);

            // ejb-ref-name
            String refName = getStringValue(xmlbeansRef.getEjbRefName());
            ref.setEjbRefName(refName);

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

            // openejb handling of injection-targets
            if (xmlbeansRef.getInjectionTargetArray() != null) {
                for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
                    InjectionTarget injectionTarget = new InjectionTarget();
                    injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
                    injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
                    ref.getInjectionTarget().add(injectionTarget);
                }
            }
            //geronimo's handling of injection-target
            addInjections(refName, xmlbeansRef.getInjectionTargetArray(), componentContext);

        }

        for (EjbLocalRefType xmlbeansRef : ejbLocalRefs) {
            // create the ejb-ref
            EjbLocalRef ref = new EjbLocalRef();
            jndiConsumer.getEjbLocalRef().add(ref);

            // ejb-ref-name
            String refName = getStringValue(xmlbeansRef.getEjbRefName());
            ref.setEjbRefName(refName);

            // ejb-ref-type
            String refType = getStringValue(xmlbeansRef.getEjbRefType());
            if ("SESSION".equalsIgnoreCase(refType)) {
                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.SESSION);
            } else if ("ENTITY".equalsIgnoreCase(refType)) {
                ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.ENTITY);
            }

            // home
            ref.setLocalHome(getStringValue(xmlbeansRef.getLocalHome()));

            // remote
            ref.setLocal(getStringValue(xmlbeansRef.getLocal()));

            // ejb-link
            ref.setEjbLink(getStringValue(xmlbeansRef.getEjbLink()));

            // mapped-name
            ref.setMappedName(getStringValue(xmlbeansRef.getMappedName()));

            // openejb handling of injection-targets
            if (xmlbeansRef.getInjectionTargetArray() != null) {
                for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
                    InjectionTarget injectionTarget = new InjectionTarget();
                    injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
                    injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
                    ref.getInjectionTarget().add(injectionTarget);
                }
            }
            //geronimo's handling of injection-target
            addInjections(refName, xmlbeansRef.getInjectionTargetArray(), componentContext);
        }
        return jndiConsumer;
    }

    private void processAnnotations(Module module) throws DeploymentException {

        // Process all the annotations for this naming builder type
        if (EJBAnnotationHelper.annotationsPresent(module.getClassFinder())) {
            try {
                EJBAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder());
            }
            catch (Exception e) {
                log.warn("Unable to process @EJB annotations for module" + module.getName(), e);
            }
        }
    }

    private void augmentJndiConsumer(Module module, JndiConsumer consumer, Map componentContext) throws DeploymentException {

        //------------------------------------------
        // Convert from XmlBeans to JAXB format
        //------------------------------------------

        EjbRefType[] ejbRefs = module.getAnnotatedApp().getEjbRefArray();
        if (ejbRefs != null) {
            for ( EjbRefType xmlbeansRef : ejbRefs ) {

                // create the ejb-ref
                EjbRef ref = new EjbRef();
                consumer.getEjbRef().add(ref);

                // ejb-ref-name
                String refName = getStringValue(xmlbeansRef.getEjbRefName());
                ref.setEjbRefName(refName);

                // ejb-ref-type
                String refType = getStringValue(xmlbeansRef.getEjbRefType());
                if ("SESSION".equalsIgnoreCase(refType)) {
                    ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.SESSION);
                }
                else if ("ENTITY".equalsIgnoreCase(refType)) {
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

                // openejb handling of injection-targets
                if (xmlbeansRef.getInjectionTargetArray() != null) {
                    for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
                        InjectionTarget injectionTarget = new InjectionTarget();
                        injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
                        injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
                        ref.getInjectionTarget().add(injectionTarget);
                    }
                }
                //geronimo's handling of injection-target
                addInjections(refName, xmlbeansRef.getInjectionTargetArray(), componentContext);
            }
        }

        EjbLocalRefType[] ejbLocalRefs = module.getAnnotatedApp().getEjbLocalRefArray();
        if (ejbLocalRefs != null) {
            for ( EjbLocalRefType xmlbeansRef : ejbLocalRefs ) {

                // create the ejb-ref
                EjbLocalRef ref = new EjbLocalRef();
                consumer.getEjbLocalRef().add(ref);

                // ejb-ref-name
                String refName = getStringValue(xmlbeansRef.getEjbRefName());
                ref.setEjbRefName(refName);

                // ejb-ref-type
                String refType = getStringValue(xmlbeansRef.getEjbRefType());
                if ("SESSION".equalsIgnoreCase(refType)) {
                    ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.SESSION);
                }
                else if ("ENTITY".equalsIgnoreCase(refType)) {
                    ref.setEjbRefType(org.apache.openejb.jee.EjbRefType.ENTITY);
                }

                // home
                ref.setLocalHome(getStringValue(xmlbeansRef.getLocalHome()));

                // remote
                ref.setLocal(getStringValue(xmlbeansRef.getLocal()));

                // ejb-link
                ref.setEjbLink(getStringValue(xmlbeansRef.getEjbLink()));

                // mapped-name
                ref.setMappedName(getStringValue(xmlbeansRef.getMappedName()));

                // openejb handling of injection-targets
                if (xmlbeansRef.getInjectionTargetArray() != null) {
                    for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
                        InjectionTarget injectionTarget = new InjectionTarget();
                        injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
                        injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
                        ref.getInjectionTarget().add(injectionTarget);
                    }
                }
                //geronimo's handling of injection-target
                addInjections(refName, xmlbeansRef.getInjectionTargetArray(), componentContext);
            }
        }

        List<EjbRefType> ambiguous = module.getAnnotatedApp().getAmbiguousEjbRefs();
        if (ambiguous != null) {
            for ( EjbRefType xmlbeansRef : ambiguous ) {

                // create the ejb-ref
                EjbRef ref = new EjbRef();
                consumer.getEjbRef().add(ref);

                // ejb-ref-name
                String refName = getStringValue(xmlbeansRef.getEjbRefName());
                ref.setEjbRefName(refName);

                // ejb-ref-type
                ref.setRefType(EjbRef.Type.UNKNOWN);

                // home
                ref.setHome(getStringValue(xmlbeansRef.getHome()));

                // remote
                ref.setRemote(getStringValue(xmlbeansRef.getRemote()));

                // ejb-link
                ref.setEjbLink(getStringValue(xmlbeansRef.getEjbLink()));

                // mapped-name
                ref.setMappedName(getStringValue(xmlbeansRef.getMappedName()));

                // openejb handling of injection-targets
                if (xmlbeansRef.getInjectionTargetArray() != null) {
                    for (InjectionTargetType injectionTargetType : xmlbeansRef.getInjectionTargetArray()) {
                        InjectionTarget injectionTarget = new InjectionTarget();
                        injectionTarget.setInjectionTargetClass(getStringValue(injectionTargetType.getInjectionTargetClass()));
                        injectionTarget.setInjectionTargetName(getStringValue(injectionTargetType.getInjectionTargetName()));
                        ref.getInjectionTarget().add(injectionTarget);
                    }
                }
                //geronimo's handling of injection-target
                addInjections(refName, xmlbeansRef.getInjectionTargetArray(), componentContext);
            }
        }
    }

    private void processWebEjbAnnotations(Module module, JndiConsumer consumer) throws DeploymentException {
        if (module instanceof WebModule) {
            try {
                ClassLoader classLoader = module.getEarContext().getClassLoader();
                UrlSet urlSet = new UrlSet(classLoader);
                if (classLoader instanceof MultiParentClassLoader) {
                    MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) classLoader;
                    for (ClassLoader parent : multiParentClassLoader.getParents()) {
                        if (parent != null) {
                            urlSet = urlSet.exclude(parent);
                        }
                    }
                } else {
                    ClassLoader parent = classLoader.getParent();
                    if (parent != null) {
                        urlSet = urlSet.exclude(parent);
                    }
                }
                ClassFinder finder = new ClassFinder(classLoader, urlSet.getUrls());
                for (Field field : finder.findAnnotatedFields(EJB.class)) {
                    EJB ejb = field.getAnnotation(EJB.class);
                    AnnotationDeployer.Member member = new AnnotationDeployer.FieldMember(field);
                    buildEjbRef(consumer, ejb, member);
                }

                for (Method method : finder.findAnnotatedMethods(EJB.class)) {
                    EJB ejb = method.getAnnotation(EJB.class);
                    AnnotationDeployer.Member member = new AnnotationDeployer.MethodMember(method);
                    buildEjbRef(consumer, ejb, member);
                }

            } catch (IOException e) {
                // ignored... we tried
                log.warn("Unable to process @EJB annotations web module" + module.getName(), e);
            }
        }
    }

    private void buildEjbRef(JndiConsumer consumer, EJB ejb, AnnotationDeployer.Member member) {
        EjbRef ejbRef = new EjbRef();

        // This is how we deal with the fact that we don't know
        // whether to use an EjbLocalRef or EjbRef (remote).
        // We flag it uknown and let the linking code take care of
        // figuring out what to do with it.
        ejbRef.setRefType(EjbRef.Type.UNKNOWN);

        if (member != null) {
            // Set the member name where this will be injected
            InjectionTarget target = new InjectionTarget();
            target.setInjectionTargetClass(member.getDeclaringClass().getName());
            target.setInjectionTargetName(member.getName());
            ejbRef.getInjectionTarget().add(target);
        }

        Class interfce = ejb.beanInterface();
        if (interfce.equals(Object.class)) {
            interfce = (member == null) ? null : member.getType();
        }

        if (interfce != null && !interfce.equals(Object.class)) {
            if (EJBHome.class.isAssignableFrom(interfce)) {
                ejbRef.setHome(interfce.getName());
                Method[] methods = interfce.getMethods();
                for (Method method : methods) {
                    if (method.getName().startsWith("create")) {
                        ejbRef.setRemote(method.getReturnType().getName());
                        break;
                    }
                }
                ejbRef.setRefType(EjbRef.Type.REMOTE);
            } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                ejbRef.setHome(interfce.getName());
                Method[] methods = interfce.getMethods();
                for (Method method : methods) {
                    if (method.getName().startsWith("create")) {
                        ejbRef.setRemote(method.getReturnType().getName());
                        break;
                    }
                }
                ejbRef.setRefType(EjbRef.Type.LOCAL);
            } else {
                ejbRef.setRemote(interfce.getName());
                if (interfce.getAnnotation(Local.class) != null) {
                    ejbRef.setRefType(EjbRef.Type.LOCAL);
                } else if (interfce.getAnnotation(Remote.class) != null) {
                    ejbRef.setRefType(EjbRef.Type.REMOTE);
                }
            }
        }

        // Get the ejb-ref-name
        String refName = ejb.name();
        if (refName.equals("")) {
            refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
        }
        ejbRef.setEjbRefName(refName);

        // Set the ejb-link, if any
        String ejbName = ejb.beanName();
        if (ejbName.equals("")) {
            ejbName = null;
        }
        ejbRef.setEjbLink(ejbName);

        // Set the mappedName, if any
        String mappedName = ejb.mappedName();
        if (mappedName.equals("")) {
            mappedName = null;
        }
        ejbRef.setMappedName(mappedName);

        switch (ejbRef.getRefType()) {
            case UNKNOWN:
            case REMOTE:
                consumer.getEjbRef().add(ejbRef);
                break;
            case LOCAL:
                consumer.getEjbLocalRef().add(new EjbLocalRef(ejbRef));
                break;
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
