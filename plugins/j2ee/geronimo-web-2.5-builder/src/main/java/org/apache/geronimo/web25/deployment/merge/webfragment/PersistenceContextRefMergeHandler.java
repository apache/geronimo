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

package org.apache.geronimo.web25.deployment.merge.webfragment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.ElementSource;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.merge.MergeItem;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String PERSISTENCE_CONTEXT_REF_NAME_PREFIX = "persistence-context-ref.persistence-context-ref-name.";

    /**
     * Steps :
     * a. If we already have a persistence-context-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same persistence-context-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (PersistenceContextRef srcPersistenceContextRef : webFragment.getPersistenceContextRef()) {
            String persistenceContextRefName = srcPersistenceContextRef.getPersistenceContextRefName();
            String persistenceContextRefKey = createPersistenceContextRefKey(persistenceContextRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(persistenceContextRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("persistence-context-ref", persistenceContextRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isPersistenceContextRefInjectTargetsConfiguredInInitialWebXML(persistenceContextRefName, mergeContext)) {
                    //Merge InjectTarget
                    PersistenceContextRef persistenceContextRef = (PersistenceContextRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcPersistenceContextRef.getInjectionTarget()) {
                        String persistenceContextRefInjectTargetKey = createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(persistenceContextRefInjectTargetKey)) {
                            persistenceContextRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(persistenceContextRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getPersistenceContextRef().add(srcPersistenceContextRef);
                mergeContext.setAttribute(persistenceContextRefKey, new MergeItem(srcPersistenceContextRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcPersistenceContextRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                            ), Boolean.TRUE);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (PersistenceContextRef persistenceContextRef : webApp.getPersistenceContextRef()) {
            String persistenceContextRefName = persistenceContextRef.getPersistenceContextRefName();
            mergeContext.setAttribute(createPersistenceContextRefKey(persistenceContextRefName), new MergeItem(persistenceContextRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (!persistenceContextRef.getInjectionTarget().isEmpty()) {
                mergeContext.setAttribute(createPersistenceContextRefInjectTargetConfiguredInWebXMLKey(persistenceContextRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : persistenceContextRef.getInjectionTarget()) {
                mergeContext.setAttribute(createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createPersistenceContextRefInjectTargetConfiguredInWebXMLKey(String persistenceContextRefName) {
        return PERSISTENCE_CONTEXT_REF_NAME_PREFIX + persistenceContextRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createPersistenceContextRefInjectTargetKey(String persistenceContextRefName, String injectTargetClassName, String injectTargetName) {
        return PERSISTENCE_CONTEXT_REF_NAME_PREFIX + persistenceContextRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createPersistenceContextRefKey(String persistenceContextRefName) {
        return PERSISTENCE_CONTEXT_REF_NAME_PREFIX + persistenceContextRefName;
    }

    public static boolean isPersistenceContextRefInjectTargetConfigured(String persistenceContextRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isPersistenceContextRefInjectTargetsConfiguredInInitialWebXML(String persistenceContextRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createPersistenceContextRefInjectTargetConfiguredInWebXMLKey(persistenceContextRefName));
    }
}
