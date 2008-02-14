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

import org.apache.geronimo.console.MultiPagePortlet;
import org.apache.geronimo.console.MultiPageModel;

import javax.portlet.PortletRequest;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;

/**
 * Portlet that can import and export CAR files
 *
 * @version $Rev$ $Date$
 */
public class ImportExportPortlet extends MultiPagePortlet {
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new IndexHandler(), config);
        addHelper(new ListHandler(), config);
        addHelper(new ResultsHandler(), config);
        addHelper(new ExportConfigHandler(), config);
        addHelper(new ExportHandler(), config);
        addHelper(new DownloadStatusHandler(), config);
        addHelper(new UpdateListHandler(), config);
        addHelper(new AddRepositoryHandler(), config);
        addHelper(new ViewPluginDownloadHandler(), config);
        addHelper(new AssemblyConfirmHandler(), config);
        addHelper(new AssemblyListHandler(), config);
        addHelper(new AssemblyViewHandler(), config);
    }

    protected String getModelJSPVariableName() {
        return "model";
    }

    protected MultiPageModel getModel(PortletRequest request) {
        return null;
    }
}
