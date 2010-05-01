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

/**
 * @version $Rev$ $Date$
 */
public class DistributableMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String CURRENT_MERGED_DISTRIBUTABLE_VALUE = "CURRENT_MERGED_DISTRIBUTABLE_VALUE";

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        boolean currentMergedDistributableValue = (Boolean) mergeContext.getAttribute(CURRENT_MERGED_DISTRIBUTABLE_VALUE);
        if (currentMergedDistributableValue) {
            mergeContext.setAttribute(CURRENT_MERGED_DISTRIBUTABLE_VALUE, webFragment.getDistributableArray().length > 0);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        boolean currentMergedDistributableValue = (Boolean) mergeContext.getAttribute(CURRENT_MERGED_DISTRIBUTABLE_VALUE);
        boolean distributableInWebXml = webApp.getDistributableArray().length > 0;
        if (currentMergedDistributableValue) {
            if (!distributableInWebXml) {
                webApp.addNewDistributable();
            }
        } else {
            if (distributableInWebXml) {
                for (int i = 0, iLoopSize = webApp.getDistributableArray().length; i < iLoopSize; i++) {
                    webApp.removeDistributable(0);
                }
            }
        }
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        context.setAttribute(CURRENT_MERGED_DISTRIBUTABLE_VALUE, Boolean.TRUE);
    }
}
