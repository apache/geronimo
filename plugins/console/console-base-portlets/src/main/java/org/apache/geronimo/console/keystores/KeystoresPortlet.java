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
package org.apache.geronimo.console.keystores;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.MultiPagePortlet;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

/**
 * A portlet that deals with multiple keystores in Geronimo.
 *
 * @version $Rev$ $Date$
 */
public class KeystoresPortlet extends MultiPagePortlet {
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new ListHandler(), config);
        addHelper(new EditKeystoreHandler(this), config);
        addHelper(new UnlockKeystoreHandler(this), config);
        addHelper(new CreateKeystoreHandler(), config);
        addHelper(new ViewKeystoreHandler(this), config);
        addHelper(new UploadCertificateHandler(), config);
        addHelper(new ConfirmCertificateHandler(), config);
        addHelper(new ConfigureNewKeyHandler(), config);
        addHelper(new ConfirmKeyHandler(), config);
        addHelper(new LockEditKeystoreHandler(this), config);
        addHelper(new LockKeystoreHandler(this), config);
        addHelper(new UnlockKeyHandler(this), config);
        addHelper(new CertificateDetailsHandler(), config);
        addHelper(new GenerateCSRHandler(), config);
        addHelper(new ImportCAReplyHandler(), config);
        addHelper(new DeleteEntryHandler(), config);
        addHelper(new ChangePasswordHandler(this), config);
    }

    protected String getModelJSPVariableName() {
        return "model";
    }

    protected MultiPageModel getModel(PortletRequest request) {
        return new BaseKeystoreHandler.KeystoreModel(request);
    }
}
