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
import org.apache.geronimo.xbeans.javaee6.ParamValueType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class ContextParamMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    public static final String QUALIFIED_CONTEXT_PARAM_NAME_PREFIX = "context-param.param-name.";

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        String jarUrl = mergeContext.getCurrentJarUrl();
        for (ParamValueType paramValue : webFragment.getContextParamArray()) {
            String qualifiedContextParamName = QUALIFIED_CONTEXT_PARAM_NAME_PREFIX + paramValue.getParamName().getStringValue();
            if (mergeContext.containsAttribute(qualifiedContextParamName)) {
                continue;
            }
            MergeItem existedContextParamValue = (MergeItem) mergeContext.getAttribute(qualifiedContextParamName);
            if (existedContextParamValue == null) {
                webApp.addNewContextParam().set(paramValue);
                mergeContext.setAttribute(qualifiedContextParamName, new MergeItem(paramValue.getParamValue().getStringValue(), jarUrl, ElementSource.WEB_FRAGMENT));
            } else if (!existedContextParamValue.getValue().equals(paramValue.getParamValue().getStringValue())) {
                throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("context-param", "param-name", paramValue.getParamName().getStringValue(), "param-value",
                        existedContextParamValue.getValue().toString(), existedContextParamValue.getBelongedURL(), paramValue.getParamValue().getStringValue(), jarUrl));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (ParamValueType paramValue : webApp.getContextParamArray()) {
            context.setAttribute(QUALIFIED_CONTEXT_PARAM_NAME_PREFIX + paramValue.getParamName().getStringValue(), paramValue.getParamName().getStringValue());
        }
    }
}
