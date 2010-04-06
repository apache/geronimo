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
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.xbeans.javaee6.ListenerType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * According to the spec 8.2.3.g.vii
 * Multiple <listener> elements with the same <listener-class> are treated as a single <listener> declaration
 * @version $Rev$ $Date$
 */
public class ListenerMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (ListenerType listener : webFragment.getListenerArray()) {
            String listenerClassName = listener.getListenerClass().getStringValue();
            if (!mergeContext.containsAttribute(createListenerKey(listenerClassName))) {
                ListenerType targetListener = webApp.addNewListener();
                targetListener.addNewListenerClass().setStringValue(listenerClassName);
                mergeContext.setAttribute(createListenerKey(listener.getListenerClass().getStringValue()), listener);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (ListenerType listener : webApp.getListenerArray()) {
            context.setAttribute(createListenerKey(listener.getListenerClass().getStringValue()), listener);
        }
    }

    public static String createListenerKey(String listenerClassName) {
        return "listener.listener-class." + listenerClassName;
    }

    public static boolean isListenerConfigured(String listenerClassName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createListenerKey(listenerClassName));
    }

    public static void addListener(ListenerType listener, MergeContext mergeContext) {
        mergeContext.setAttribute(createListenerKey(listener.getListenerClass().getStringValue()), listener);
    }
}
