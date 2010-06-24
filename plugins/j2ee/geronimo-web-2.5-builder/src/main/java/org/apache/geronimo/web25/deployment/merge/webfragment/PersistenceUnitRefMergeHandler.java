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
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String PERSISTENCE_UNIT_REF_NAME_PREFIX = "persistence-unit-ref.persistence-unit-ref-name.";

    /**
     * Steps :
     * a. If we already have a persistence-unit-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same persistence-unit-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (PersistenceUnitRef srcPersistenceUnitRef : webFragment.getPersistenceUnitRef()) {
            String persistenceUnitRefName = srcPersistenceUnitRef.getPersistenceUnitRefName();
            String persistenceUnitRefKey = createPersistenceUnitRefKey(persistenceUnitRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(persistenceUnitRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("persistence-unit-ref", persistenceUnitRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isPersistenceUnitRefInjectTargetsConfiguredInInitialWebXML(persistenceUnitRefName, mergeContext)) {
                    //Merge InjectTarget
                    PersistenceUnitRef persistenceUnitRef = (PersistenceUnitRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcPersistenceUnitRef.getInjectionTarget()) {
                        String persistenceUnitRefInjectTargetKey = createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(persistenceUnitRefInjectTargetKey)) {
                            persistenceUnitRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(persistenceUnitRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getPersistenceUnitRef().add(srcPersistenceUnitRef);
                mergeContext.setAttribute(persistenceUnitRefKey, new MergeItem(srcPersistenceUnitRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcPersistenceUnitRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
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
        for (PersistenceUnitRef persistenceUnitRef : webApp.getPersistenceUnitRef()) {
            String persistenceUnitRefName = persistenceUnitRef.getPersistenceUnitRefName();
            mergeContext.setAttribute(createPersistenceUnitRefKey(persistenceUnitRefName), new MergeItem(persistenceUnitRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (!persistenceUnitRef.getInjectionTarget().isEmpty()) {
                mergeContext.setAttribute(createPersistenceUnitRefInjectTargetConfiguredInWebXMLKey(persistenceUnitRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : persistenceUnitRef.getInjectionTarget()) {
                mergeContext.setAttribute(createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createPersistenceUnitRefInjectTargetConfiguredInWebXMLKey(String persistenceUnitRefName) {
        return PERSISTENCE_UNIT_REF_NAME_PREFIX + persistenceUnitRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createPersistenceUnitRefInjectTargetKey(String persistenceUnitRefName, String injectTargetClassName, String injectTargetName) {
        return PERSISTENCE_UNIT_REF_NAME_PREFIX + persistenceUnitRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createPersistenceUnitRefKey(String persistenceUnitRefName) {
        return PERSISTENCE_UNIT_REF_NAME_PREFIX + persistenceUnitRefName;
    }

    public static boolean isPersistenceUnitRefInjectTargetConfigured(String persistenceUnitRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isPersistenceUnitRefInjectTargetsConfiguredInInitialWebXML(String persistenceUnitRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createPersistenceUnitRefInjectTargetConfiguredInWebXMLKey(persistenceUnitRefName));
    }
}
