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
import org.apache.geronimo.xbeans.javaee6.ErrorPageType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class ErrorPageMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (ErrorPageType errorPage : webFragment.getErrorPageArray()) {
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(createErrorPageKey(errorPage));
            if (mergeItem != null) {
                if (mergeItem.getSourceType().equals(ElementSource.WEB_XML)) {
                    continue;
                } else if (mergeItem.getValue().equals(errorPage.getLocation().getStringValue())) {
                    boolean isErrorCodeConfigured = errorPage.getErrorCode() != null;
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("error-page", isErrorCodeConfigured ? "error-code" : "exception-type", isErrorCodeConfigured ? errorPage
                            .getErrorCode().getStringValue() : errorPage.getExceptionType().getStringValue(), "location", (String) mergeItem.getValue(), mergeItem.getBelongedURL(), errorPage
                            .getLocation().getStringValue(), mergeContext.getCurrentJarUrl()));
                }
            } else {
                webApp.addNewErrorPage().set(errorPage);
                mergeContext.setAttribute(createErrorPageKey(errorPage), new MergeItem(errorPage.getLocation().getStringValue(), mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (ErrorPageType errorPage : webApp.getErrorPageArray()) {
            context.setAttribute(createErrorPageKey(errorPage), new MergeItem(errorPage.getLocation().getStringValue(), null, ElementSource.WEB_XML));
        }
    }

    public static final String createErrorCodeKey(String errorCode) {
        return "error-page.error-code." + errorCode;
    }

    public static final String createExceptionTypeKey(String exceptionType) {
        return "error-page.exception-type." + exceptionType;
    }

    public static final String createErrorPageKey(ErrorPageType errorPage) {
        return errorPage.getErrorCode() == null ? createExceptionTypeKey(errorPage.getExceptionType().getStringValue()) : createErrorCodeKey(errorPage.getErrorCode().getStringValue());
    }
}
