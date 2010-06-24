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
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class ServiceRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String SERVICE_REF_NAME_PREFIX = "service-ref.service-ref-name.";

    /**
     * Steps :
     * a. If we already have a service-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same service-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (ServiceRef srcServiceRef : webFragment.getServiceRef()) {
            String serviceRefName = srcServiceRef.getServiceRefName();
            String serviceRefKey = createServiceRefKey(serviceRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(serviceRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("service-ref", serviceRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isServiceRefInjectTargetsConfiguredInInitialWebXML(serviceRefName, mergeContext)) {
                    //Merge InjectTarget
                    ServiceRef serviceRef = (ServiceRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcServiceRef.getInjectionTarget()) {
                        String serviceRefInjectTargetKey = createServiceRefInjectTargetKey(serviceRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(serviceRefInjectTargetKey)) {
                            serviceRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(serviceRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getServiceRef().add(srcServiceRef);
                mergeContext.setAttribute(serviceRefKey, new MergeItem(srcServiceRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcServiceRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createServiceRefInjectTargetKey(serviceRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
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
        for (ServiceRef serviceRef : webApp.getServiceRef()) {
            String serviceRefName = serviceRef.getServiceRefName();
            mergeContext.setAttribute(createServiceRefKey(serviceRefName), new MergeItem(serviceRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (!serviceRef.getInjectionTarget().isEmpty()) {
                mergeContext.setAttribute(createServiceRefInjectTargetConfiguredInWebXMLKey(serviceRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : serviceRef.getInjectionTarget()) {
                mergeContext.setAttribute(createServiceRefInjectTargetKey(serviceRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createServiceRefInjectTargetConfiguredInWebXMLKey(String serviceRefName) {
        return SERVICE_REF_NAME_PREFIX + serviceRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createServiceRefInjectTargetKey(String serviceRefName, String injectTargetClassName, String injectTargetName) {
        return SERVICE_REF_NAME_PREFIX + serviceRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createServiceRefKey(String serviceRefName) {
        return SERVICE_REF_NAME_PREFIX + serviceRefName;
    }

    public static boolean isServiceRefInjectTargetConfigured(String serviceRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServiceRefInjectTargetKey(serviceRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isServiceRefInjectTargetsConfiguredInInitialWebXML(String serviceRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createServiceRefInjectTargetConfiguredInWebXMLKey(serviceRefName));
    }
}
