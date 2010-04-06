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
import org.apache.geronimo.web25.deployment.merge.MergeHelper;
import org.apache.geronimo.web25.deployment.merge.MergeItem;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.geronimo.xbeans.javaee6.JspConfigType;
import org.apache.geronimo.xbeans.javaee6.JspPropertyGroupType;
import org.apache.geronimo.xbeans.javaee6.TaglibType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class JspConfigMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    private static final Logger logger = LoggerFactory.getLogger(JspConfigMergeHandler.class);

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        if (webFragment.getJspConfigArray().length == 0) {
            return;
        }
        if (webFragment.getJspConfigArray().length > 1) {
            logger.warn(WebDeploymentMessageUtils.createMultipleConfigurationWarningMessage("jsp-config", mergeContext.getCurrentJarUrl()));
        }
        JspConfigType srcJspConfig = webFragment.getJspConfigArray(0);
        JspConfigType targetJspConfig = webApp.getJspConfigArray().length == 0 ? webApp.addNewJspConfig() : webApp.getJspConfigArray(0);
        //Merge Tag lib configurations
        for (TaglibType taglib : srcJspConfig.getTaglibArray()) {
            if (MergeHelper.mergeRequired(createTaglibKey(taglib), "jsp-config/tag-lib", "taglib-uri", taglib.getTaglibUri().getStringValue(), "taglib-location", taglib.getTaglibLocation()
                    .getStringValue(), mergeContext)) {
                TaglibType newTaglib = targetJspConfig.addNewTaglib();
                newTaglib.set(taglib);
            }
        }
        //Merge jsp-property-group configurations, seem that no merge actions are required, just add them to the web.xml file
        for (JspPropertyGroupType srcJspPropertyGroup : srcJspConfig.getJspPropertyGroupArray()) {
            targetJspConfig.addNewJspPropertyGroup().set(srcJspPropertyGroup);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        if (webApp.getJspConfigArray().length == 0) {
            return;
        }
        if (webApp.getJspConfigArray().length > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("jsp-config"));
        }
        JspConfigType jspConfig = webApp.getJspConfigArray(0);
        for (TaglibType taglib : jspConfig.getTaglibArray()) {
            context.setAttribute(createTaglibKey(taglib), new MergeItem(taglib.getTaglibLocation().getStringValue(), null, ElementSource.WEB_XML));
        }
    }

    public static String createTaglibKey(TaglibType taglib) {
        return "jsp-config.taglib.taglib-uri." + taglib.getTaglibUri().getStringValue();
    }
}
