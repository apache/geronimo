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
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;
import org.apache.openejb.jee.WelcomeFileList;

/**
 * TODO 8.1.6 By default all applications will have index.htm(l) and index.jsp in the list of  welcome-file-list.
 * The descriptor may to be used to override these default settings. So do we need to add them if none is found
 * @version $Rev$ $Date$
 */
public class WelcomeFileListMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        WelcomeFileList targetWelcomeFileList = webApp.getWelcomeFileList().isEmpty() ? null: webApp.getWelcomeFileList().get(0);
        for (WelcomeFileList welcomeFileList : webFragment.getWelcomeFileList()) {
            for (String welcomeFile : welcomeFileList.getWelcomeFile()) {
                String welcomeFileKey = createWelcomeFileKey(welcomeFile);
                if (mergeContext.containsAttribute(welcomeFileKey)) {
                    continue;
                }
                if (targetWelcomeFileList == null) {
                    targetWelcomeFileList = new WelcomeFileList();
                    webApp.getWelcomeFileList().add(targetWelcomeFileList);
                }
                targetWelcomeFileList.getWelcomeFile().add(welcomeFile);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp parentElement, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        WelcomeFileList welcomeFileList = null;
        //Spec 14.2 While multiple welcome file lists are found, we need to concatenate the items
        for (WelcomeFileList list: webApp.getWelcomeFileList()) {
            if (welcomeFileList == null) {
                welcomeFileList = list;
            } else {
                welcomeFileList.getWelcomeFile().addAll(list.getWelcomeFile());
            }
        }
        webApp.getWelcomeFileList();
        if (welcomeFileList != null) {
            for (String welcomeFile : welcomeFileList.getWelcomeFile()) {
                context.setAttribute(createWelcomeFileKey(welcomeFile), Boolean.TRUE);
            }
        }
    }

    public static String createWelcomeFileKey(String welcomeFile) {
        return "welcome-file-list.welcome-file." + welcomeFile;
    }

    public static boolean isWelcomeFileConfigured(String welcomeFile, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createWelcomeFileKey(welcomeFile));
    }
}
