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
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextRefMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String PERSISTENCE_CONTEXT_REF_NAME_PREFIX = "persistence-context-ref.persistence-context-ref-name.";

    /**
     * Steps :
     * a. If we already have a persistence-context-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same persistence-context-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (PersistenceContextRefType srcPersistenceContextRef : webFragment.getPersistenceContextRefArray()) {
            String persistenceContextRefName = srcPersistenceContextRef.getPersistenceContextRefName().getStringValue();
            String persistenceContextRefKey = createPersistenceContextRefKey(persistenceContextRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(persistenceContextRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("persistence-context-ref", persistenceContextRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isPersistenceContextRefInjectTargetsConfiguredInInitialWebXML(persistenceContextRefName, mergeContext)) {
                    //Merge InjectTarget
                    PersistenceContextRefType persistenceContextRef = (PersistenceContextRefType) mergeItem.getValue();
                    for (InjectionTargetType injectTarget : srcPersistenceContextRef.getInjectionTargetArray()) {
                        String persistenceContextRefInjectTargetKey = createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectTarget.getInjectionTargetClass().getStringValue(), injectTarget
                                .getInjectionTargetName().getStringValue());
                        if (!mergeContext.containsAttribute(persistenceContextRefInjectTargetKey)) {
                            persistenceContextRef.addNewInjectionTarget().set(injectTarget);
                            mergeContext.setAttribute(persistenceContextRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                PersistenceContextRefType targetPersistenceContextRef = (PersistenceContextRefType) webApp.addNewPersistenceContextRef().set(srcPersistenceContextRef);
                mergeContext.setAttribute(persistenceContextRefKey, new MergeItem(targetPersistenceContextRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTargetType injectionTarget : targetPersistenceContextRef.getInjectionTargetArray()) {
                    mergeContext.setAttribute(createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
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
        for (PersistenceContextRefType persistenceContextRef : webApp.getPersistenceContextRefArray()) {
            String persistenceContextRefName = persistenceContextRef.getPersistenceContextRefName().getStringValue();
            mergeContext.setAttribute(createPersistenceContextRefKey(persistenceContextRefName), new MergeItem(persistenceContextRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (persistenceContextRef.getInjectionTargetArray().length > 0) {
                mergeContext.setAttribute(createPersistenceContextRefInjectTargetConfiguredInWebXMLKey(persistenceContextRefName), Boolean.TRUE);
            }
            for (InjectionTargetType injectionTarget : persistenceContextRef.getInjectionTargetArray()) {
                mergeContext.setAttribute(createPersistenceContextRefInjectTargetKey(persistenceContextRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                        .getStringValue()), Boolean.TRUE);
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
