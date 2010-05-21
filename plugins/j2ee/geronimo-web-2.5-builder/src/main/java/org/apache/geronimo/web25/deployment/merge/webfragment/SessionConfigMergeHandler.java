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
import org.apache.geronimo.xbeans.javaee6.CookieConfigType;
import org.apache.geronimo.xbeans.javaee6.SessionConfigType;
import org.apache.geronimo.xbeans.javaee6.TrackingModeType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class SessionConfigMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        if (webFragment.getSessionConfigArray().length == 1) {
            mergeSessionConfig(webApp, webFragment.getSessionConfigArray(0), mergeContext, ElementSource.WEB_FRAGMENT);
        } else if (webFragment.getSessionConfigArray().length > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebFragmentErrorMessage("session-config", mergeContext.getCurrentJarUrl()));
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        if (webApp.getSessionConfigArray().length == 1) {
            SessionConfigType sessionConfig = webApp.getSessionConfigArray(0);
            mergeSessionConfig(webApp, sessionConfig, context, ElementSource.WEB_XML);
            context.setAttribute("session-config", sessionConfig);
        } else if (webApp.getSessionConfigArray().length > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("session-config"));
        }
    }

    private CookieConfigType getCookieConfig(WebAppType webApp, MergeContext context) {
        SessionConfigType sessionConfig = getSessionConfig(webApp, context);
        if (sessionConfig.isSetCookieConfig()) {
            return sessionConfig.getCookieConfig();
        } else {
            return sessionConfig.addNewCookieConfig();
        }
    }

    private SessionConfigType getSessionConfig(WebAppType webApp, MergeContext context) {
        SessionConfigType sessionConfig = (SessionConfigType) context.getAttribute("session-config");
        if (sessionConfig == null) {
            sessionConfig = webApp.addNewSessionConfig();
            context.setAttribute("session-config", sessionConfig);
        }
        return sessionConfig;
    }

    private void mergeSessionConfig(WebAppType webApp, SessionConfigType sessionConfig, MergeContext context, ElementSource elementSource) throws DeploymentException {
        if (sessionConfig.isSetSessionTimeout()) {
            if (mergeSingleAttribute(context, "timeout", "session-config.session-timeout", sessionConfig.getSessionTimeout().getStringValue(), null, elementSource)) {
                getSessionConfig(webApp, context).addNewSessionTimeout().set(sessionConfig.getSessionTimeout());
            }
        }
        if (sessionConfig.isSetCookieConfig()) {
            CookieConfigType cookieConfig = sessionConfig.getCookieConfig();
            if (cookieConfig.isSetName() && mergeSingleAttribute(context, "name", "session-config.cookie-config.name", cookieConfig.getName().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewName().set(cookieConfig.getName());
            }
            if (cookieConfig.isSetDomain() && mergeSingleAttribute(context, "domain", "session-config.cookie-config.domain", cookieConfig.getDomain().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewDomain().set(cookieConfig.getDomain());
            }
            if (cookieConfig.isSetPath() && mergeSingleAttribute(context, "path", "session-config.cookie-config.path", cookieConfig.getPath().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewPath().set(cookieConfig.getPath());
            }
            if (cookieConfig.isSetComment() && mergeSingleAttribute(context, "comment", "session-config.cookie-config.comment", cookieConfig.getComment().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewComment().set(cookieConfig.getComment());
            }
            if (cookieConfig.isSetHttpOnly() && mergeSingleAttribute(context, "http-only", "session-config.cookie-config.http-only", cookieConfig.getHttpOnly().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewHttpOnly().set(cookieConfig.getHttpOnly());
            }
            if (cookieConfig.isSetSecure() && mergeSingleAttribute(context, "secure", "session-config.cookie-config.secure", cookieConfig.getSecure().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewSecure().set(cookieConfig.getSecure());
            }
            if (cookieConfig.isSetMaxAge() && mergeSingleAttribute(context, "max-age", "session-config.cookie-config.max-age", cookieConfig.getMaxAge().getStringValue(), null, elementSource)) {
                getCookieConfig(webApp, context).addNewMaxAge().set(cookieConfig.getMaxAge());
            }
        }
        if (elementSource.equals(ElementSource.WEB_FRAGMENT) && sessionConfig.getTrackingModeArray().length > 0) {
            for (TrackingModeType trackingMode : sessionConfig.getTrackingModeArray()) {
                if (!context.containsAttribute("session-config.tracking-mode." + trackingMode.getStringValue())) {
                    getSessionConfig(webApp, context).addNewTrackingMode().set(trackingMode);
                    context.setAttribute("session-config.tracking-mode." + trackingMode.getStringValue(), Boolean.TRUE);
                }
            }
        }
    }

    private boolean mergeSingleAttribute(MergeContext mergeContext, String elementName, String key, String value, String jarUrl, ElementSource elementSource) throws DeploymentException {
        MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(key);
        if (mergeItem != null) {
            if (mergeItem.getSourceType().equals(ElementSource.WEB_FRAGMENT)) {
                if (!mergeItem.getValue().equals(value)) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateValueMessage("session-config", elementName, (String) mergeItem.getValue(), mergeItem.getBelongedURL(),
                            value, jarUrl));
                }
            }
            return false;
        } else {
            mergeContext.setAttribute(key, new MergeItem(value, jarUrl, elementSource));
            return elementSource.equals(ElementSource.WEB_FRAGMENT);
        }
    }
}
