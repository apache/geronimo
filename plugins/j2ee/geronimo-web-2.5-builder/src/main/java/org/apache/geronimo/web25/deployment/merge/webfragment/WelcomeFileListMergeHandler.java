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
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;
import org.apache.geronimo.xbeans.javaee6.WelcomeFileListType;

/**
 * TODO 8.1.6 By default all applications will have index.htm(l) and index.jsp in the list of  welcome-file-list.
 * The descriptor may to be used to override these default settings. So do we need to add them if none is found
 * @version $Rev$ $Date$
 */
public class WelcomeFileListMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        WelcomeFileListType targetWelcomeFileList = null;
        for (WelcomeFileListType welcomeFileList : webFragment.getWelcomeFileListArray()) {
            for (String welcomeFile : welcomeFileList.getWelcomeFileArray()) {
                String welcomeFileKey = createWelcomeFileKey(welcomeFile);
                if (mergeContext.containsAttribute(welcomeFileKey)) {
                    continue;
                }
                if (targetWelcomeFileList == null) {
                    targetWelcomeFileList = webApp.getWelcomeFileListArray().length > 0 ? webApp.getWelcomeFileListArray(0) : webApp.addNewWelcomeFileList();
                }
                targetWelcomeFileList.addNewWelcomeFile().setStringValue(welcomeFile);
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType parentElement, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        WelcomeFileListType[] welcomeFileLists = webApp.getWelcomeFileListArray();
        if (welcomeFileLists.length == 0) {
            return;
        }
        //Spec 14.2 While multiple welcome file lists are found, we need to concatenate the items
        if (welcomeFileLists.length > 1) {
            WelcomeFileListType targetWelcomeFileList = welcomeFileLists[0];
            for (int i = 1; i < welcomeFileLists.length; i++) {
                WelcomeFileListType welcomeFileList = welcomeFileLists[i];
                for (String welcomeFile : welcomeFileList.getWelcomeFileArray()) {
                    targetWelcomeFileList.addNewWelcomeFile().setStringValue(welcomeFile);
                }
            }
            for (int i = 1, iLength = welcomeFileLists.length; i < iLength; i++) {
                webApp.removeWelcomeFileList(1);
            }
        }
        for (String welcomeFile : welcomeFileLists[0].getWelcomeFileArray()) {
            context.setAttribute(createWelcomeFileKey(welcomeFile), Boolean.TRUE);
        }
    }

    public static String createWelcomeFileKey(String welcomeFile) {
        return "welcome-file-list.welcome-file." + welcomeFile;
    }

    public static boolean isWelcomeFileConfigured(String welcomeFile, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createWelcomeFileKey(welcomeFile));
    }
}
