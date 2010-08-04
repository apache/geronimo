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

import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.web25.deployment.merge.MergeContext;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentValidationUtils;
import org.apache.openejb.jee.SecurityConstraint;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;
import org.apache.openejb.jee.WebResourceCollection;

/**
 * FIXME For security-constraint, we just need to merge them to web.xml file, please correct me if I miss anything
 * @version $Rev$ $Date$
 */
public class SecurityConstraintMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (SecurityConstraint securityConstraint : webFragment.getSecurityConstraint()) {
            for (WebResourceCollection webResourceCollection : securityConstraint.getWebResourceCollection()) {
                for (String urlPattern : webResourceCollection.getUrlPattern()) {
                    if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("security-constraint", webResourceCollection.getWebResourceName(),
                                urlPattern, "web-fragment.xml located in " + mergeContext.getCurrentJarUrl()));
                    }
                }
                validateHTTPMethods(webResourceCollection.getHttpMethod(), mergeContext.getCurrentJarUrl());
                validateHTTPMethods(webResourceCollection.getHttpMethodOmission(), mergeContext.getCurrentJarUrl());
            }
            webApp.getSecurityConstraint().add(securityConstraint);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        for (SecurityConstraint securityConstraint : webApp.getSecurityConstraint()) {
            for (WebResourceCollection webResourceCollection : securityConstraint.getWebResourceCollection()) {
                for (String urlPattern : webResourceCollection.getUrlPattern()) {
                    if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("security-constraint", webResourceCollection.getWebResourceName(),
                                urlPattern, "web.xml "));
                    }
                }
                validateHTTPMethods(webResourceCollection.getHttpMethod(), "web.xml");
                validateHTTPMethods(webResourceCollection.getHttpMethodOmission(), "web.xml");
            }
        }
    }

    private void validateHTTPMethods(List<String> httpMethods, String source) throws DeploymentException {
        for (String httpMethod : httpMethods) {
            if (!WebDeploymentValidationUtils.isValidHTTPMethod(httpMethod)) {
                throw new DeploymentException("Invalid HTTP method value is found in " + source);
            }
        }
    }
}
