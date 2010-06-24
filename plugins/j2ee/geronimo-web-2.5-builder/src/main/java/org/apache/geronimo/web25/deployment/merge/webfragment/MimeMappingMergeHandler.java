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
import org.apache.openejb.jee.MimeMapping;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class MimeMappingMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (MimeMapping mimeMapping : webFragment.getMimeMapping()) {
            String extension = mimeMapping.getExtension();
            if (isMimeMappingConfiguredInWebXML(extension, mergeContext)) {
                continue;
            }
            String jarUrl = mergeContext.getCurrentJarUrl();
            String qualifedMimeMappingName = createMimeMappingConfiguredInWebFragmentXMLKey(extension);
            MergeItem existedMimeMapping = (MergeItem) mergeContext.getAttribute(qualifedMimeMappingName);
            if (existedMimeMapping == null) {
                mergeContext.setAttribute(qualifedMimeMappingName, new MergeItem(mimeMapping.getMimeType(), jarUrl, ElementSource.WEB_FRAGMENT));
                webApp.getMimeMapping().add(mimeMapping);
            } else if (!mimeMapping.getMimeType().equals(existedMimeMapping.getValue())) {
                throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("mime-mapping", "extension", extension, "mime-type", (String) existedMimeMapping.getValue(), existedMimeMapping
                        .getBelongedURL(), mimeMapping.getMimeType(), jarUrl));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (MimeMapping mimeMapping : webApp.getMimeMapping()) {
            context.setAttribute(createMimeMappingConfiguredInWebXMLKey(mimeMapping.getExtension()), Boolean.TRUE);
        }
    }

    public static String createMimeMappingConfiguredInWebFragmentXMLKey(String extension) {
        return "mime-mapping.extension." + extension + ".configured_in_web_fragment_xml";
    }

    public static String createMimeMappingConfiguredInWebXMLKey(String extension) {
        return "mime-mapping.extension." + extension + ".configured_in_web_xml";
    }

    public static boolean isMimeMappingConfiguredInWebFragmentXML(String extension, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createMimeMappingConfiguredInWebFragmentXMLKey(extension));
    }

    public static boolean isMimeMappingConfiguredInWebXML(String extension, MergeContext mergeContext) {
        return mergeContext.containsAttribute(createMimeMappingConfiguredInWebXMLKey(extension));
    }
}
