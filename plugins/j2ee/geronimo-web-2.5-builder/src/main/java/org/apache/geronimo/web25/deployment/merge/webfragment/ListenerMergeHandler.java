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
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * According to the spec 8.2.3.g.vii
 * Multiple <listener> elements with the same <listener-class> are treated as a single <listener> declaration
 * @version $Rev$ $Date$
 */
public class ListenerMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (Listener listener : webFragment.getListener()) {
            String listenerClassName = listener.getListenerClass();
            if (!mergeContext.containsAttribute(createListenerKey(listenerClassName))) {
                webApp.getListener().add(listener);
                mergeContext.setAttribute(createListenerKey(listener.getListenerClass()), listener);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (Listener listener : webApp.getListener()) {
            context.setAttribute(createListenerKey(listener.getListenerClass()), listener);
        }
    }

    public static String createListenerKey(String listenerClassName) {
        return "listener.listener-class." + listenerClassName;
    }

    public static boolean isListenerConfigured(String listenerClassName, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createListenerKey(listenerClassName));
    }

    public static void addListener(Listener listener, MergeContext mergeContext) {
        mergeContext.setAttribute(createListenerKey(listener.getListenerClass()), listener);
    }
}
