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
import org.apache.geronimo.xbeans.javaee6.ServiceRefType;
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class ServiceRefMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String SERVICE_REF_NAME_PREFIX = "service-ref.service-ref-name.";

    /**
     * Steps :
     * a. If we already have a service-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same service-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (ServiceRefType srcServiceRef : webFragment.getServiceRefArray()) {
            String serviceRefName = srcServiceRef.getServiceRefName().getStringValue();
            String serviceRefKey = createServiceRefKey(serviceRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(serviceRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("service-ref", serviceRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isServiceRefInjectTargetsConfiguredInInitialWebXML(serviceRefName, mergeContext)) {
                    //Merge InjectTarget
                    ServiceRefType serviceRef = (ServiceRefType) mergeItem.getValue();
                    for (InjectionTargetType injectTarget : srcServiceRef.getInjectionTargetArray()) {
                        String serviceRefInjectTargetKey = createServiceRefInjectTargetKey(serviceRefName, injectTarget.getInjectionTargetClass().getStringValue(), injectTarget
                                .getInjectionTargetName().getStringValue());
                        if (!mergeContext.containsAttribute(serviceRefInjectTargetKey)) {
                            serviceRef.addNewInjectionTarget().set(injectTarget);
                            mergeContext.setAttribute(serviceRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                ServiceRefType targetServiceRef = (ServiceRefType) webApp.addNewServiceRef().set(srcServiceRef);
                mergeContext.setAttribute(serviceRefKey, new MergeItem(targetServiceRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTargetType injectionTarget : targetServiceRef.getInjectionTargetArray()) {
                    mergeContext.setAttribute(createServiceRefInjectTargetKey(serviceRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
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
        for (ServiceRefType serviceRef : webApp.getServiceRefArray()) {
            String serviceRefName = serviceRef.getServiceRefName().getStringValue();
            mergeContext.setAttribute(createServiceRefKey(serviceRefName), new MergeItem(serviceRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (serviceRef.getInjectionTargetArray().length > 0) {
                mergeContext.setAttribute(createServiceRefInjectTargetConfiguredInWebXMLKey(serviceRefName), Boolean.TRUE);
            }
            for (InjectionTargetType injectionTarget : serviceRef.getInjectionTargetArray()) {
                mergeContext.setAttribute(createServiceRefInjectTargetKey(serviceRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                        .getStringValue()), Boolean.TRUE);
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
