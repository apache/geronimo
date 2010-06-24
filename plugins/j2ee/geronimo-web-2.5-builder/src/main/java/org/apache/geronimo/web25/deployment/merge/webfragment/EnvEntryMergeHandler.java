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
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class EnvEntryMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String ENV_ENTRY_NAME_PREFIX = "env-entry.env-entry-name.";

    /**
     * Steps :
     * a. If we already have a env-entry in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same env-entry in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (EnvEntry srcEnvEntry : webFragment.getEnvEntry()) {
            String envEntryName = srcEnvEntry.getEnvEntryName();
            String envEntryKey = createEnvEntryKey(envEntryName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(envEntryKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("env-entry", envEntryName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isEnvEntryInjectTargetsConfiguredInInitialWebXML(envEntryName, mergeContext)) {
                    //Merge InjectTarget
                    EnvEntry envEntry = (EnvEntry) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcEnvEntry.getInjectionTarget()) {
                        String envEntryInjectTargetKey = createEnvEntryInjectTargetKey(envEntryName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(envEntryInjectTargetKey)) {
                            envEntry.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(envEntryInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getEnvEntry().add(srcEnvEntry);
                mergeContext.setAttribute(envEntryKey, new MergeItem(srcEnvEntry, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcEnvEntry.getInjectionTarget()) {
                    mergeContext.setAttribute(createEnvEntryInjectTargetKey(envEntryName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
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
        for (EnvEntry envEntry : webApp.getEnvEntry()) {
            String envEntryName = envEntry.getEnvEntryName();
            mergeContext.setAttribute(createEnvEntryKey(envEntryName), new MergeItem(envEntry, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (envEntry.getInjectionTarget().size() > 0) {
                mergeContext.setAttribute(createEnvEntryInjectTargetConfiguredInWebXMLKey(envEntryName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : envEntry.getInjectionTarget()) {
                mergeContext.setAttribute(createEnvEntryInjectTargetKey(envEntryName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
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
