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
package org.apache.geronimo.console.car;

import org.apache.geronimo.console.MultiPageAbstractHandler;

/**
 * The base class for all handlers for this portlet
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseImportExportHandler extends MultiPageAbstractHandler {
    protected static final String CONFIG_LIST_SESSION_KEY = "console.plugins.ConfigurationList";
    public static final String DOWNLOAD_RESULTS_SESSION_KEY = "console.plugins.DownloadResults";
    protected static final String INDEX_MODE = "index";
    protected static final String ADD_REPO_MODE = "addRepository";
    protected static final String LIST_MODE = "list";
    protected static final String DOWNLOAD_MODE = "download";
    protected static final String VIEW_FOR_DOWNLOAD_MODE = "viewForDownload";
    protected static final String DOWNLOAD_STATUS_MODE = "downloadStatus";
    protected static final String RESULTS_MODE = "results";
    protected static final String CONFIGURE_EXPORT_MODE = "configure";
    protected static final String CONFIRM_EXPORT_MODE = "confirm";
    protected static final String UPDATE_REPOS_MODE = "updateList";

    protected BaseImportExportHandler(String mode, String viewName) {
        super(mode, viewName);
    }
}
