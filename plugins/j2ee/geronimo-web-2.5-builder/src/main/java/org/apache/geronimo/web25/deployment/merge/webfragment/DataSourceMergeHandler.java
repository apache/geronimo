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
import org.apache.geronimo.xbeans.javaee6.DataSourceType;
import org.apache.geronimo.xbeans.javaee6.WebAppType;
import org.apache.geronimo.xbeans.javaee6.WebFragmentType;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceMergeHandler implements WebFragmentMergeHandler<WebFragmentType, WebAppType> {

    @Override
    public void merge(WebFragmentType webFragment, WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (DataSourceType srcDataSource : webFragment.getDataSourceArray()) {
            String dataSourceKey = createDataSourceKey(srcDataSource, mergeContext);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(dataSourceKey);
            if (mergeItem != null && mergeItem.isFromWebFragment()) {
                throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("data-source", srcDataSource.getName().getStringValue(), mergeContext.getCurrentJarUrl(), mergeItem
                        .getBelongedURL()));
            }
            DataSourceType targetDataSource = (DataSourceType) webApp.addNewDataSource().set(srcDataSource);
            mergeContext.setAttribute(dataSourceKey, new MergeItem(targetDataSource, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
        }
    }

    @Override
    public void postProcessWebXmlElement(WebAppType webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebAppType webApp, MergeContext mergeContext) throws DeploymentException {
        for (DataSourceType dataSource : webApp.getDataSourceArray()) {
            mergeContext.setAttribute(createDataSourceKey(dataSource, mergeContext), new MergeItem(dataSource, null, ElementSource.WEB_XML));
        }
    }

    public static String createDataSourceKey(DataSourceType dataSource, MergeContext mergeContext) {
        return "data-source.name." + dataSource.getName().getStringValue();
    }
}
