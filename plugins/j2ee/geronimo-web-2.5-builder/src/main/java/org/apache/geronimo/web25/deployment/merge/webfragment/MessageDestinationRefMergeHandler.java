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
import org.apache.geronimo.xbeans.javaee6.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class MessageDestinationRefMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String MESSAGE_DESTINATION_REF_NAME_PREFIX = "message-destination-ref.message-destination-ref-name.";

    /**
     * Steps :
     * a. If we already have a message-destination-ref in the current merged web.xml file, then
     *     a.1 If it is from web-fragment.xml, throw an error, as it is not allowed that the same message-destination-ref in different web-fragment.xml while it is not present in web.xml
     *     a.2 Else it is from web.xml, check whether inject-target is configured in the initial web.xml, if not, merge the configurations from web-fragment.xml, else ignore.
     * b. web.xml file should inherit it from the web-fragment.xml file
     */
    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (MessageDestinationRefType srcMessageDestinationRef : webFragment.getMessageDestinationRefArray()) {
            String messageDestinationRefName = srcMessageDestinationRef.getMessageDestinationRefName().getStringValue();
            String messageDestinationRefKey = createMessageDestinationRefKey(messageDestinationRefName);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(messageDestinationRefKey);
            if (mergeItem != null) {
                if (mergeItem.isFromWebFragment()) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("message-destination-ref", messageDestinationRefName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
                } else if (mergeItem.isFromWebXml() && !isMessageDestinationRefInjectTargetsConfiguredInInitialWebXML(messageDestinationRefName, mergeContext)) {
                    //Merge InjectTarget
                    MessageDestinationRefType messageDestinationRef = (MessageDestinationRefType) mergeItem.getValue();
                    for (InjectionTargetType injectTarget : srcMessageDestinationRef.getInjectionTargetArray()) {
                        String messageDestinationRefInjectTargetKey = createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectTarget.getInjectionTargetClass().getStringValue(), injectTarget
                                .getInjectionTargetName().getStringValue());
                        if (!mergeContext.containsAttribute(messageDestinationRefInjectTargetKey)) {
                            messageDestinationRef.addNewInjectionTarget().set(injectTarget);
                            mergeContext.setAttribute(messageDestinationRefInjectTargetKey, Boolean.TRUE);
                        }
                    }
                }
            } else {
                MessageDestinationRefType targetMessageDestinationRef = (MessageDestinationRefType) webApp.addNewMessageDestinationRef().set(srcMessageDestinationRef);
                mergeContext.setAttribute(messageDestinationRefKey, new MergeItem(targetMessageDestinationRef, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
                for (InjectionTargetType injectionTarget : targetMessageDestinationRef.getInjectionTargetArray()) {
                    mergeContext.setAttribute(createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
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
        for (MessageDestinationRefType messageDestinationRef : webApp.getMessageDestinationRefArray()) {
            String messageDestinationRefName = messageDestinationRef.getMessageDestinationRefName().getStringValue();
            mergeContext.setAttribute(createMessageDestinationRefKey(messageDestinationRefName), new MergeItem(messageDestinationRef, null, ElementSource.WEB_XML));
            //Create an attribute tag to indicate whether injectTarget is configured in web.xml file
            if (messageDestinationRef.getInjectionTargetArray().length > 0) {
                mergeContext.setAttribute(createMessageDestinationRefInjectTargetConfiguredInWebXMLKey(messageDestinationRefName), Boolean.TRUE);
            }
            for (InjectionTargetType injectionTarget : messageDestinationRef.getInjectionTargetArray()) {
                mergeContext.setAttribute(createMessageDestinationRefInjectTargetKey(messageDestinationRefName, injectionTarget.getInjectionTargetClass().getStringValue(), injectionTarget.getInjectionTargetName()
                        .getStringValue()), Boolean.TRUE);
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
