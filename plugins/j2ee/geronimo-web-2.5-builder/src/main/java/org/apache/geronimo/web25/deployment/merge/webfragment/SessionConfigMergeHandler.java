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
import org.apache.openejb.jee.CookieConfig;
import org.apache.openejb.jee.SessionConfig;
import org.apache.openejb.jee.TrackingMode;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class SessionConfigMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        if (webFragment.getSessionConfig().size() == 1) {
            mergeSessionConfig(webApp, webFragment.getSessionConfig().get(0), mergeContext, ElementSource.WEB_FRAGMENT);
        } else if (webFragment.getSessionConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebFragmentErrorMessage("session-config", mergeContext.getCurrentJarUrl()));
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        if (webApp.getSessionConfig().size() == 1) {
            SessionConfig sessionConfig = webApp.getSessionConfig().get(0);
            mergeSessionConfig(webApp, sessionConfig, context, ElementSource.WEB_XML);
            context.setAttribute("session-config", sessionConfig);
        } else if (webApp.getSessionConfig().size() > 1) {
            throw new DeploymentException(WebDeploymentMessageUtils.createMultipleConfigurationWebAppErrorMessage("session-config"));
        }
    }

    private CookieConfig getCookieConfig(WebApp webApp, MergeContext context) {
        SessionConfig sessionConfig = getSessionConfig(webApp, context);
        if (sessionConfig.getCookieConfig() == null) {
            sessionConfig.setCookieConfig(new CookieConfig());
        }
        return sessionConfig.getCookieConfig();
    }

    private SessionConfig getSessionConfig(WebApp webApp, MergeContext context) {
        SessionConfig sessionConfig = (SessionConfig) context.getAttribute("session-config");
        if (sessionConfig == null) {
            sessionConfig = new SessionConfig();
            webApp.getSessionConfig().add(sessionConfig);
            context.setAttribute("session-config", sessionConfig);
        }
        return sessionConfig;
    }

    private void mergeSessionConfig(WebApp webApp, SessionConfig sessionConfig, MergeContext context, ElementSource elementSource) throws DeploymentException {
        if (sessionConfig.getSessionTimeout() != null) {
            if (mergeSingleAttribute(context, "timeout", "session-config.session-timeout", "" + sessionConfig.getSessionTimeout(), null, elementSource)) {
                getSessionConfig(webApp, context).setSessionTimeout(sessionConfig.getSessionTimeout());
            }
        }
        if (sessionConfig.getCookieConfig() != null) {
            CookieConfig cookieConfig = sessionConfig.getCookieConfig();
            if (cookieConfig.getName() != null && mergeSingleAttribute(context, "name", "session-config.cookie-config.name", cookieConfig.getName(), null, elementSource)) {
                getCookieConfig(webApp, context).setName(cookieConfig.getName());
            }
            if (cookieConfig.getDomain() != null && mergeSingleAttribute(context, "domain", "session-config.cookie-config.domain", cookieConfig.getDomain(), null, elementSource)) {
                getCookieConfig(webApp, context).setDomain(cookieConfig.getDomain());
            }
            if (cookieConfig.getPath() != null && mergeSingleAttribute(context, "path", "session-config.cookie-config.path", cookieConfig.getPath(), null, elementSource)) {
                getCookieConfig(webApp, context).setPath(cookieConfig.getPath());
            }
            if (cookieConfig.getComment() != null && mergeSingleAttribute(context, "comment", "session-config.cookie-config.comment", cookieConfig.getComment(), null, elementSource)) {
                getCookieConfig(webApp, context).setComment(cookieConfig.getComment());
            }
            if (cookieConfig.getHttpOnly() != null && mergeSingleAttribute(context, "http-only", "session-config.cookie-config.http-only", "" + cookieConfig.getHttpOnly(), null, elementSource)) {
                getCookieConfig(webApp, context).setHttpOnly(cookieConfig.getHttpOnly());
            }
            if (cookieConfig.getSecure() != null && mergeSingleAttribute(context, "secure", "session-config.cookie-config.secure", "" +  cookieConfig.getSecure(), null, elementSource)) {
                getCookieConfig(webApp, context).setSecure(cookieConfig.getSecure());
            }
            if (cookieConfig.getMaxAge() != null && mergeSingleAttribute(context, "max-age", "session-config.cookie-config.max-age",  "" +cookieConfig.getMaxAge(), null, elementSource)) {
                getCookieConfig(webApp, context).setMaxAge(cookieConfig.getMaxAge());
            }
        }
        if (elementSource.equals(ElementSource.WEB_FRAGMENT) && !sessionConfig.getTrackingMode().isEmpty()) {
            for (TrackingMode trackingMode : sessionConfig.getTrackingMode()) {
                if (!context.containsAttribute("session-config.tracking-mode." + trackingMode)) {
                    getSessionConfig(webApp, context).getTrackingMode().add(trackingMode);
                    context.setAttribute("session-config.tracking-mode." + trackingMode, Boolean.TRUE);
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
