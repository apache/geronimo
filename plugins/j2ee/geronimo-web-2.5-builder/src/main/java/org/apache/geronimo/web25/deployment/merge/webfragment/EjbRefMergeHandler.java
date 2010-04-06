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
import org.apache.geronimo.xbeans.javaee6.EjbRefType;
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class EjbRefMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String EJB_REF_NAME_PREFIX = "ejb-ref.ejb-ref-name.";

    /**
     * Steps :
     * a. If we already have a ejb-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same ejb-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (EjbRefType srcEjbRef : webFragment.getEjbRefArray()) {
            String ejbRefName = srcEjbRef.getEjbRefName().getStringValue();
            String ejbRefKey = createEjbRefKey(ejbRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(ejbRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("ejb-ref", ejbRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isEjbRefInjectTargetsConfiguredInInitialWebXML(ejbRefName, mergeContext)) {
                    //Merge InjectTarget
                    EjbRefType ejbRef = (EjbRefType) mergeItem.getValue();
                    for (InjectionTargetType injectTarget : srcEjbRef.getInjectionTargetArray()) {
                        String ejbRefInjectTargetKey = createEjbRefInjectTargetKey(ejbRefName, injectTarget.getInjectionTargetClass().getStringValue(), injectTarget
                                .getInjectionTargetName().getStringValue());
                        if (!mergeContext.containsAttribute(ejbRefInjectTargetKey)) {
                            ejbRef.addNewInjectionTarget().set(injectTarget);
                            mergeContext.setAttribute(ejbRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                EjbRefType targetEjbRef = (EjbRefType) webApp.addNewEjbRef().set(srcEjbRef);
                mergeContext.setAttribute(ejbRefKey, new MergeItem(targetEjbRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTargetType injectionTarget : targetEjbRef.getInjectionTargetArray()) {
                    mergeContext.setAttribute(createEjbRefInjectTargetKey(ejbRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
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
        for (EjbRefType ejbRef : webApp.getEjbRefArray()) {
            String ejbRefName = ejbRef.getEjbRefName().getStringValue();
            mergeContext.setAttribute(createEjbRefKey(ejbRefName), new MergeItem(ejbRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (ejbRef.getInjectionTargetArray().length > 0) {
                mergeContext.setAttribute(createEjbRefInjectTargetConfiguredInWebXMLKey(ejbRefName), Boolean.TRUE);
            }
            for (InjectionTargetType injectionTarget : ejbRef.getInjectionTargetArray()) {
                mergeContext.setAttribute(createEjbRefInjectTargetKey(ejbRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                        .getStringValue()), Boolean.TRUE);
            }
        }
    }

    public static String createEjbRefInjectTargetConfiguredInWebXMLKey(String ejbRefName) {
        return EJB_REF_NAME_PREFIX + ejbRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createEjbRefInjectTargetKey(String ejbRefName, String injectTargetClassName, String injectTargetName) {
        return EJB_REF_NAME_PREFIX + ejbRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createEjbRefKey(String ejbRefName) {
        return EJB_REF_NAME_PREFIX + ejbRefName;
    }

    public static boolean isEjbRefInjectTargetConfigured(String ejbRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createEjbRefInjectTargetKey(ejbRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isEjbRefInjectTargetsConfiguredInInitialWebXML(String ejbRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createEjbRefInjectTargetConfiguredInWebXMLKey(ejbRefName));
    }
}
