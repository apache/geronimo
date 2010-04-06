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
import org.apache.geronimo.xbeans.javaee6.PersistenceUnitRefType;
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitRefMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String PERSISTENCE_UNIT_REF_NAME_PREFIX = "persistence-unit-ref.persistence-unit-ref-name.";

    /**
     * Steps :
     * a. If we already have a persistence-unit-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same persistence-unit-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (PersistenceUnitRefType srcPersistenceUnitRef : webFragment.getPersistenceUnitRefArray()) {
            String persistenceUnitRefName = srcPersistenceUnitRef.getPersistenceUnitRefName().getStringValue();
            String persistenceUnitRefKey = createPersistenceUnitRefKey(persistenceUnitRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(persistenceUnitRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("persistence-unit-ref", persistenceUnitRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isPersistenceUnitRefInjectTargetsConfiguredInInitialWebXML(persistenceUnitRefName, mergeContext)) {
                    //Merge InjectTarget
                    PersistenceUnitRefType persistenceUnitRef = (PersistenceUnitRefType) mergeItem.getValue();
                    for (InjectionTargetType injectTarget : srcPersistenceUnitRef.getInjectionTargetArray()) {
                        String persistenceUnitRefInjectTargetKey = createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectTarget.getInjectionTargetClass().getStringValue(), injectTarget
                                .getInjectionTargetName().getStringValue());
                        if (!mergeContext.containsAttribute(persistenceUnitRefInjectTargetKey)) {
                            persistenceUnitRef.addNewInjectionTarget().set(injectTarget);
                            mergeContext.setAttribute(persistenceUnitRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                PersistenceUnitRefType targetPersistenceUnitRef = (PersistenceUnitRefType) webApp.addNewPersistenceUnitRef().set(srcPersistenceUnitRef);
                mergeContext.setAttribute(persistenceUnitRefKey, new MergeItem(targetPersistenceUnitRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTargetType injectionTarget : targetPersistenceUnitRef.getInjectionTargetArray()) {
                    mergeContext.setAttribute(createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                            .getStringValue()), Boolean.TRUE);
                }
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (PersistenceUnitRefType persistenceUnitRef : webApp.getPersistenceUnitRefArray()) {
            String persistenceUnitRefName = persistenceUnitRef.getPersistenceUnitRefName().getStringValue();
            mergeContext.setAttribute(createPersistenceUnitRefKey(persistenceUnitRefName), new MergeItem(persistenceUnitRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (persistenceUnitRef.getInjectionTargetArray().length > 0) {
                mergeContext.setAttribute(createPersistenceUnitRefInjectTargetConfiguredInWebXMLKey(persistenceUnitRefName), Boolean.TRUE);
            }
            for (InjectionTargetType injectionTarget : persistenceUnitRef.getInjectionTargetArray()) {
                mergeContext.setAttribute(createPersistenceUnitRefInjectTargetKey(persistenceUnitRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                        .getStringValue()), Boolean.TRUE);
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
