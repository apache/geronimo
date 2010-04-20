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
import org.apache.geronimo.web25.deployment.utils.WebDeploymentMessageUtils;
import org.apache.geronimo.web25.deployment.utils.WebDeploymentValidationUtils;
import org.apache.geronimo.xbeans.javaee6.SecurityConstraintType;
import org.apache.geronimo.xbeans.javaee6.UrlPatternType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;
import org.apache.geronimo.xbeans.javaee6.WebResourceCollectionType;

/**
 * FIXME For security-constraint, we just need to merge them to web.xml file, please correct me if I miss anything
 * @version $Rev$ $Date$
 */
public class SecurityConstraintMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (SecurityConstraintType securityConstraint : webApp.getSecurityConstraintArray()) {
            for (WebResourceCollectionType webResourceCollection : securityConstraint.getWebResourceCollectionArray()) {
                for (UrlPatternType pattern : webResourceCollection.getUrlPatternArray()) {
                    String urlPattern = pattern.getStringValue();
                    if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("security-constraint", webResourceCollection.getWebResourceName().getStringValue(),
                                urlPattern, "web-fragment.xml located in " + mergeContext.getCurrentJarUrl()));
                    }
                }
                validateHTTPMethods(webResourceCollection.getHttpMethodArray(), mergeContext.getCurrentJarUrl());
                validateHTTPMethods(webResourceCollection.getHttpMethodOmissionArray(), mergeContext.getCurrentJarUrl());
            }
            webApp.addNewSecurityConstraint().set(securityConstraint);
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        for (SecurityConstraintType securityConstraint : webApp.getSecurityConstraintArray()) {
            for (WebResourceCollectionType webResourceCollection : securityConstraint.getWebResourceCollectionArray()) {
                for (UrlPatternType pattern : webResourceCollection.getUrlPatternArray()) {
                    String urlPattern = pattern.getStringValue();
                    if (!WebDeploymentValidationUtils.isValidUrlPattern(urlPattern)) {
                        throw new DeploymentException(WebDeploymentMessageUtils.createInvalidUrlPatternErrorMessage("security-constraint", webResourceCollection.getWebResourceName().getStringValue(),
                                urlPattern, "web.xml "));
                    }
                }
                validateHTTPMethods(webResourceCollection.getHttpMethodArray(), "web.xml");
                validateHTTPMethods(webResourceCollection.getHttpMethodOmissionArray(), "web.xml");
            }
        }
    }

    private void validateHTTPMethods(String[] httpMethods, String source) throws DeploymentException {
        for (String httpMethod : httpMethods) {
            if (!WebDeploymentValidationUtils.isValidHTTPMethod(httpMethod)) {
                throw new DeploymentException("Invalid HTTP method value is found in " + source);
            }
        }
    }
}
