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
import org.apache.geronimo.xbeans.javaee6.MessageDestinationType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class MessageDestinationMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (MessageDestinationType messageDestination : webApp.getMessageDestinationArray()) {
            String messageDestinationName = messageDestination.getMessageDestinationName().getStringValue();
            if (mergeContext.containsAttribute(createMessageDestinationConfiguredInWebXMLKey(messageDestination.getMessageDestinationName().getStringValue()))) {
                continue;
            }
            if (mergeContext.containsAttribute(createMessageDestinationConfiguredInWebFragmentXMLKey(messageDestinationName))) {
                MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(createMessageDestinationConfiguredInWebFragmentXMLKey(messageDestinationName));
                throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("message-destination", messageDestinationName, mergeItem.getBelongedURL(), mergeContext.getCurrentJarUrl()));
            } else {
                MessageDestinationType newMessageDestination = (MessageDestinationType) webApp.addNewMessageDestination().set(messageDestination);
                mergeContext.setAttribute(createMessageDestinationConfiguredInWebFragmentXMLKey(messageDestinationName), new MergeItem(newMessageDestination, mergeContext.getCurrentJarUrl(),
                        ElementSource.WEB_FRAGMENT));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        // TODO Auto-generated method stub
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (MessageDestinationType messageDestination : webApp.getMessageDestinationArray()) {
            context.setAttribute(createMessageDestinationConfiguredInWebXMLKey(messageDestination.getMessageDestinationName().getStringValue()), Boolean.TRUE);
        }
    }

    public static String createMessageDestinationConfiguredInWebFragmentXMLKey(String messageDestinationName) {
        return "message-destination.message-destination-name." + messageDestinationName + ".configured_in_web_fragment_xml";
    }

    public static String createMessageDestinationConfiguredInWebXMLKey(String messageDestinationName) {
        return "message-destination.message-destination-name." + messageDestinationName + ".configured_in_web_xml";
    }
}
