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
import org.apache.geronimo.xbeans.javaee6.EnvEntryType;
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class EnvEntryMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String ENV_ENTRY_NAME_PREFIX = "env-entry.env-entry-name.";

    /**
     * Steps :
     * a. If we already have a env-entry in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same env-entry in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (EnvEntryType srcEnvEntry : webFragment.getEnvEntryArray()) {
            String envEntryName = srcEnvEntry.getEnvEntryName().getStringValue();
            String envEntryKey = createEnvEntryKey(envEntryName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(envEntryKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("env-entry", envEntryName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isEnvEntryInjectTargetsConfiguredInInitialWebXML(envEntryName, mergeContext)) {
                    //Merge InjectTarget
                    EnvEntryType envEntry = (EnvEntryType) mergeItem.getValue();
                    for (InjectionTargetType injectTarget : srcEnvEntry.getInjectionTargetArray()) {
                        String envEntryInjectTargetKey = createEnvEntryInjectTargetKey(envEntryName, injectTarget.getInjectionTargetClass().getStringValue(), injectTarget
                                .getInjectionTargetName().getStringValue());
                        if (!mergeContext.containsAttribute(envEntryInjectTargetKey)) {
                            envEntry.addNewInjectionTarget().set(injectTarget);
                            mergeContext.setAttribute(envEntryInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                EnvEntryType targetEnvEntry = (EnvEntryType) webApp.addNewEnvEntry().set(srcEnvEntry);
                mergeContext.setAttribute(envEntryKey, new MergeItem(targetEnvEntry, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTargetType injectionTarget : targetEnvEntry.getInjectionTargetArray()) {
                    mergeContext.setAttribute(createEnvEntryInjectTargetKey(envEntryName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
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
        for (EnvEntryType envEntry : webApp.getEnvEntryArray()) {
            String envEntryName = envEntry.getEnvEntryName().getStringValue();
            mergeContext.setAttribute(createEnvEntryKey(envEntryName), new MergeItem(envEntry, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (envEntry.getInjectionTargetArray().length > 0) {
                mergeContext.setAttribute(createEnvEntryInjectTargetConfiguredInWebXMLKey(envEntryName), Boolean.TRUE);
            }
            for (InjectionTargetType injectionTarget : envEntry.getInjectionTargetArray()) {
                mergeContext.setAttribute(createEnvEntryInjectTargetKey(envEntryName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                        .getStringValue()), Boolean.TRUE);
            }
        }
    }

    public static String createEnvEntryInjectTargetConfiguredInWebXMLKey(String envEntryName) {
        return ENV_ENTRY_NAME_PREFIX + envEntryName + ".inject_target_configured_in_web_xml";
    }

    public static String createEnvEntryInjectTargetKey(String envEntryName, String injectTargetClassName, String injectTargetName) {
        return ENV_ENTRY_NAME_PREFIX + envEntryName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createEnvEntryKey(String envEntryName) {
        return ENV_ENTRY_NAME_PREFIX + envEntryName;
    }

    public static boolean isEnvEntryInjectTargetConfigured(String envEntryName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createEnvEntryInjectTargetKey(envEntryName, injectTargetClassName, injectTargetName));
    }

    public static boolean isEnvEntryInjectTargetsConfiguredInInitialWebXML(String envEntryName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createEnvEntryInjectTargetConfiguredInWebXMLKey(envEntryName));
    }
}
