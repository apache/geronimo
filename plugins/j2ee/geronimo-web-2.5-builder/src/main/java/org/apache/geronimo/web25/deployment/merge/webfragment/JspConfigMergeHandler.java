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
import org.apache.openejb.jee.JspConfig;
import org.apache.openejb.jee.JspPropertyGroup;
import org.apache.openejb.jee.Taglib;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class JspConfigMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    private static final Logger logger = LoggerFactory.getLogger(JspConfigMergeHandler.class);

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        if (webFragment.getJspConfig().isEmpty()) {
            return;
        }
        if (webFragment.getJspConfig().size() > 1) {
            logger.warn(WebDeploymentMessageUtils.createMultipleConfigurationWarningMessage("jsp-config", mergeContext.getCurrentJarUrl()));
        }
        JspConfig srcJspConfig = webFragment.getJspConfig().get(0);
        if (webApp.getJspConfig().isEmpty()) {
            webApp.getJspConfig().add( new JspConfig());
        }
        JspConfig targetJspConfig = webApp.getJspConfig().get(0);
        //Merge Tag lib configurations
        for (Taglib taglib : srcJspConfig.getTaglib()) {
            if (MergeHelper.mergeRequired(createTaglibKey(taglib), "jsp-config/tag-lib", "taglib-uri", taglib.getTaglibUri(), "taglib-location", taglib.getTaglibLocation()
                    , mergeContext)) {
                targetJspConfig.getTaglib().add(taglib);
            }
        }
        //Merge jsp-property-group configurations, seem that no merge actions are required, just add them to the web.xml file
        for (JspPropertyGroup srcJspPropertyGroup : srcJspConfig.getJspPropertyGroup()) {
            targetJspConfig.getJspPropertyGroup().add(srcJspPropertyGroup);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        if (webApp.getJspConfig().isEmpty()) {
            return;
        }
        if (webApp.getJspConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("jsp-config"));
        }
        JspConfig jspConfig = webApp.getJspConfig().get(0);
        for (Taglib taglib : jspConfig.getTaglib()) {
            context.setAttribute(createTaglibKey(taglib), new MergeItem(taglib.getTaglibLocation(), null, ElementSource.WEB_XML));
        }
    }

    public static String createTaglibKey(Taglib taglib) {
        return "jsp-config.taglib.taglib-uri." + taglib.getTaglibUri();
    }
}
