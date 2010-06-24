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
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class MessageDestinationRefMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    public static final String MESSAGE_DESTINATION_REF_NAME_PREFIX = "message-destination-ref.message-destination-ref-name.";

    /**
     * Steps :
     * a. If we already have a message-destination-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same message-destination-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (MessageDestinationRef srcMessageDestinationRef : webFragment.getMessageDestinationRef()) {
            String messageDestinationRefName = srcMessageDestinationRef.getMessageDestinationRefName();
            String messageDestinationRefKey = createMessageDestinationRefKey(messageDestinationRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(messageDestinationRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("message-destination-ref", messageDestinationRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isMessageDestinationRefInjectTargetsConfiguredInInitialWebXML(messageDestinationRefName, mergeContext)) {
                    //Merge InjectTarget
                    MessageDestinationRef messageDestinationRef = (MessageDestinationRef) mergeItem.getValue();
                    for (InjectionTarget injectTarget : srcMessageDestinationRef.getInjectionTarget()) {
                        String messageDestinationRefInjectTargetKey = createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectTarget.getInjectionTargetClass(), injectTarget
                                .getInjectionTargetName());
                        if (!mergeContext.containsAttribute(messageDestinationRefInjectTargetKey)) {
                            messageDestinationRef.getInjectionTarget().add(injectTarget);
                            mergeContext.setAttribute(messageDestinationRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                webApp.getMessageDestinationRef().add(srcMessageDestinationRef);
                mergeContext.setAttribute(messageDestinationRefKey, new MergeItem(srcMessageDestinationRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTarget injectionTarget : srcMessageDestinationRef.getInjectionTarget()) {
                    mergeContext.setAttribute(createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
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
        for (MessageDestinationRef messageDestinationRef : webApp.getMessageDestinationRef()) {
            String messageDestinationRefName = messageDestinationRef.getMessageDestinationRefName();
            mergeContext.setAttribute(createMessageDestinationRefKey(messageDestinationRefName), new MergeItem(messageDestinationRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (!messageDestinationRef.getInjectionTarget().isEmpty()) {
                mergeContext.setAttribute(createMessageDestinationRefInjectTargetConfiguredInWebXMLKey(messageDestinationRefName), Boolean.TRUE);
            }
            for (InjectionTarget injectionTarget : messageDestinationRef.getInjectionTarget()) {
                mergeContext.setAttribute(createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectionTarget.getInjectionTargetClass(), injectionTarget.getInjectionTargetName()
                        ), Boolean.TRUE);
            }
        }
    }

    public static String createMessageDestinationRefInjectTargetConfiguredInWebXMLKey(String messageDestinationRefName) {
        return MESSAGE_DESTINATION_REF_NAME_PREFIX + messageDestinationRefName + ".inject_target_configured_in_web_xml";
    }

    public static String createMessageDestinationRefInjectTargetKey(String messageDestinationRefName, String injectTargetClassName, String injectTargetName) {
        return MESSAGE_DESTINATION_REF_NAME_PREFIX + messageDestinationRefName + "." + injectTargetClassName + "." + injectTargetName;
    }

    public static String createMessageDestinationRefKey(String messageDestinationRefName) {
        return MESSAGE_DESTINATION_REF_NAME_PREFIX + messageDestinationRefName;
    }

    public static boolean isMessageDestinationRefInjectTargetConfigured(String messageDestinationRefName, String injectTargetClassName, String injectTargetName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectTargetClassName, injectTargetName));
    }

    public static boolean isMessageDestinationRefInjectTargetsConfiguredInInitialWebXML(String messageDestinationRefName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createMessageDestinationRefInjectTargetConfiguredInWebXMLKey(messageDestinationRefName));
    }
}
