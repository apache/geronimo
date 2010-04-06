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
import org.apache.geronimo.xbeans.javaee6.LocaleEncodingMappingListType;
import org.apache.geronimo.xbeans.javaee6.LocaleEncodingMappingType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class LocaleEncodingMappingListMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        LocaleEncodingMappingListType targetLocaleEncodingMappingList = null;
        for (LocaleEncodingMappingListType localeEncodingMappingList : webFragment.getLocaleEncodingMappingListArray()) {
            for (LocaleEncodingMappingType localeEncodingMapping : localeEncodingMappingList.getLocaleEncodingMappingArray()) {
                String localeEncodingMappingKey = createLocaleEncodingMappingKey(localeEncodingMapping.getLocale());
                MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(localeEncodingMappingKey);
                if (mergeItem != null && mergeItem.isFromWebFragment() && !mergeItem.getValue().equals(localeEncodingMapping.getEncoding())) {
                    throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateKeyValueMessage("locale-encoding-mapping", "locale", localeEncodingMapping.getLocale(), "encoding",
                            (String) mergeItem.getValue(), mergeItem.getBelongedURL(), localeEncodingMapping.getLocale(), mergeContext.getCurrentJarUrl()));
                }
                if (targetLocaleEncodingMappingList == null) {
                    targetLocaleEncodingMappingList = webApp.getLocaleEncodingMappingListArray().length > 0 ? webApp.getLocaleEncodingMappingListArray(0) : webApp.addNewLocaleEncodingMappingList();
                }
                targetLocaleEncodingMappingList.addNewLocaleEncodingMapping().set(localeEncodingMapping);
                mergeContext.setAttribute(localeEncodingMappingKey, new MergeItem(localeEncodingMapping.getEncoding(), mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
            }
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
        LocaleEncodingMappingListType[] localeEncodingMappingLists = webApp.getLocaleEncodingMappingListArray();
        if (localeEncodingMappingLists.length == 0) {
            return;
        }
        //Spec 14.2 While multiple locale-encoding-mapping lists are found, we need to concatenate the items
        if (localeEncodingMappingLists.length > 1) {
            LocaleEncodingMappingListType targetLocaleEncodingMappingList = localeEncodingMappingLists[0];
            for (int i = 1; i < localeEncodingMappingLists.length; i++) {
                LocaleEncodingMappingListType localeEncodingMappingList = localeEncodingMappingLists[i];
                for (LocaleEncodingMappingType localeEncodingMapping : localeEncodingMappingList.getLocaleEncodingMappingArray()) {
                    targetLocaleEncodingMappingList.addNewLocaleEncodingMapping().set(localeEncodingMapping);
                }
            }
            for (int i = 1, iLength = localeEncodingMappingLists.length; i < iLength; i++) {
                webApp.removeLocaleEncodingMappingList(1);
            }
        }
        for (LocaleEncodingMappingType localeEncodingMapping : webApp.getLocaleEncodingMappingListArray(0).getLocaleEncodingMappingArray()) {
            context.setAttribute(createLocaleEncodingMappingKey(localeEncodingMapping.getLocale()), new MergeItem(localeEncodingMapping.getEncoding(), null, ElementSource.WEB_XML));
        }
    }

    public static String createLocaleEncodingMappingKey(String locale) {
        return "locale-encoding-mapping-list.locale-encoding-mapping.locale." + locale;
    }
}
