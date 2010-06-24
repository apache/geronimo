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
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceMergeHandler implements WebFragmentMergeHandler<WebFragment, WebApp> {

    @Override
    public void merge(WebFragment webFragment, WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (DataSource srcDataSource : webFragment.getDataSource()) {
            String dataSourceKey = createDataSourceKey(srcDataSource, mergeContext);
            MergeItem mergeItem = (MergeItem) mergeContext.getAttribute(dataSourceKey);
            if (mergeItem != null && mergeItem.isFromWebFragment()) {
                throw new DeploymentException(WebDeploymentMessageUtils.createDuplicateJNDIRefMessage("data-source", srcDataSource.getName(), mergeContext.getCurrentJarUrl(), mergeItem
                        .getBelongedURL()));
            }
            webApp.getDataSource().add(srcDataSource);
            mergeContext.setAttribute(dataSourceKey, new MergeItem(srcDataSource, mergeContext.getCurrentJarUrl(), ElementSource.WEB_FRAGMENT));
        }
    }

    @Override
    public void postProcessWebXmlElement(WebApp webApp, MergeContext context) throws DeploymentException {
    }

    @Override
    public void preProcessWebXmlElement(WebApp webApp, MergeContext mergeContext) throws DeploymentException {
        for (DataSource dataSource : webApp.getDataSource()) {
            mergeContext.setAttribute(createDataSourceKey(dataSource, mergeContext), new MergeItem(dataSource, null, ElementSource.WEB_XML));
        }
    }

    public static String createDataSourceKey(DataSource dataSource, MergeContext mergeContext) {
        return "data-source.name." + dataSource.getName();
    }
}
