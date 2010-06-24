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
import org.apache.openejb.jee.LocaleEncodingMapping;
import org.apache.openejb.jee.LocaleEncodingMappingList;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class LocaleEncodingMappingListMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        LocaleEncodingMappingList targetLocaleEncodingMappingList = webApp.getLocaleEncodingMappingList().isEmpty() ? null: webApp.getLocaleEncodingMappingList().get(0);
        for (LocaleEncodingMappingList localeEncodingMappingList : webFragment.getLocaleEncodingMappingList()) {
            for (LocaleEncodingMapping localeEncodingMapping : localeEncodingMappingList.getLocaleEncodingMapping()) {
                String localeEncodingMappingKey = createLocaleEncodingMappingKey(localeEncodingMapping.getLocale());
                MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(localeEncodingMappingKey);
                if (mergeItem != null && mergeItem.isFromWebFragment() && !mergeItem.getValue().equals(localeEncodingMapping.getEncoding())) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("locale-encoding-mapping", "locale", localeEncodingMapping.getLocale(), "encoding",
                            (String) mergeItem.getValue(), mergeItem.getBelongedURL(), localeEncodingMapping.getLocale(), mergeContext.getCurrentJarUrl()));
                }
                if (targetLocaleEncodingMappingList == null) {
                    targetLocaleEncodingMappingList = new LocaleEncodingMappingList();
                    webApp.getLocaleEncodingMappingList().add(targetLocaleEncodingMappingList);
                }
                targetLocaleEncodingMappingList.getLocaleEncodingMapping().add(localeEncodingMapping);
                mergeContext.setAttribute(localeEncodingMappingKey, new MergeItem(localeEncodingMapping.getEncoding(), mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
        LocaleEncodingMappingList targetLocaleEncodingMappingList = null;
        for (LocaleEncodingMappingList list: webApp.getLocaleEncodingMappingList()) {
            if (targetLocaleEncodingMappingList == null) {
                targetLocaleEncodingMappingList = list;
            } else {
                targetLocaleEncodingMappingList.getLocaleEncodingMapping().addAll(list.getLocaleEncodingMapping());
            }
        }
        if (targetLocaleEncodingMappingList != null) {
            webApp.getLocaleEncodingMappingList().clear();
            webApp.getLocaleEncodingMappingList().add(targetLocaleEncodingMappingList);
            for (LocaleEncodingMapping localeEncodingMapping : targetLocaleEncodingMappingList.getLocaleEncodingMapping()) {
                context.setAttribute(createLocaleEncodingMappingKey(localeEncodingMapping.getLocale()), new MergeItem(localeEncodingMapping.getEncoding(), null, ElementSource.WEB_XML));
            }
        }
    }

    public static String createLocaleEncodingMappingKey(String locale) {
        return "locale-encoding-mapping-list.locale-encoding-mapping.locale." + locale;
    }
}
