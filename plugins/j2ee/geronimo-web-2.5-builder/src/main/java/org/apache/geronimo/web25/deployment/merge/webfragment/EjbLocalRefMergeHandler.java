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
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class EjbLocalRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String EJB_LOCAL_REF_NAME_PREFIX = "ejb-local-ref.ejb-ref-name.";

    /**
     * Steps :
     * a. If we already have a ejb-local-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same ejb-local-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (EjbLocalRef srcEjbLocalRef : webFragment.getEjbLocalRef()) {
            String ejbLocalRefName = srcEjbLocalRef.getEjbRefName();
            String ejbLocalRefKey = createEjbLocalRefKey(ejbLocalRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(ejbLocalRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("ejb-local-ref", ejbLocalRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isEjbLocalRefInjectTargetsConfiguredInInitialWebXML(ejbLocalRefName, mergeContext)) {
                    //Merge InjectTarget
                    EjbLocalRef ejbLocalRef = (EjbLocalRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcEjbLocalRef.getInjectionTarget()) {
                        String ejbLocalRefInjectTargetKey = createEjbLocalRefInjectTargetKey(ejbLocalRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(ejbLocalRefInjectTargetKey)) {
                            ejbLocalRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(ejbLocalRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getEjbLocalRef().add(srcEjbLocalRef);
                mergeContext.setAttribute(ejbLocalRefKey, new MergeItem(srcEjbLocalRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcEjbLocalRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createEjbLocalRefInjectTargetKey(ejbLocalRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
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
        for (EjbLocalRef ejbLocalRef : webApp.getEjbLocalRef()) {
            String ejbLocalRefName = ejbLocalRef.getEjbRefName();
            mergeContext.setAttribute(createEjbLocalRefKey(ejbLocalRefName), new MergeItem(ejbLocalRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (ejbLocalRef.getInjectionTarget().size() > 0) {
                mergeContext.setAttribute(createEjbLocalRefInjectTargetConfiguredInWebXMLKey(ejbLocalRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : ejbLocalRef.getInjectionTarget()) {
                mergeContext.setAttribute(createEjbLocalRefInjectTargetKey(ejbLocalRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createEjbLocalRefInjectTargetConfiguredInWebXMLKey(String ejbLocalRefName) {
        return EJB_LOCAL_REF_NAME_PREFIX + ejbLocalRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createEjbLocalRefInjectTargetKey(String ejbLocalRefName, String injectTargetClassName, String injectTargetName) {
        return EJB_LOCAL_REF_NAME_PREFIX + ejbLocalRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createEjbLocalRefKey(String ejbLocalRefName) {
        return EJB_LOCAL_REF_NAME_PREFIX + ejbLocalRefName;
    }

    public static boolean isEjbLocalRefInjectTargetConfigured(String ejbLocalRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createEjbLocalRefInjectTargetKey(ejbLocalRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isEjbLocalRefInjectTargetsConfiguredInInitialWebXML(String ejbLocalRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createEjbLocalRefInjectTargetConfiguredInWebXMLKey(ejbLocalRefName));
    }
}
