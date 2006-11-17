/**
 *
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
package org.apache.geronimo.console.ca;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.MultiPagePortlet;

/**
 * A portlet for Certification Authority.
 *
 * @version $Rev$ $Date$
 */
public class CertificationAuthorityPortlet extends MultiPagePortlet {
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new IntroHandler(), config);
        addHelper(new SetupCAHandler(), config);
        addHelper(new ConfirmCAHandler(), config);
        addHelper(new CADetailsHandler(), config);
        addHelper(new UnlockCAHandler(), config);
        addHelper(new ProcessCSRHandler(), config);
        addHelper(new CertReqDetailsHandler(), config);
        addHelper(new ConfirmClientCertHandler(), config);
        addHelper(new ViewCertificateHandler(), config);
        addHelper(new ListRequestsIssueHandler(), config);
        addHelper(new ListRequestsVerifyHandler(), config);
        addHelper(new ConfirmCertReqHandler(), config);
    }

    protected String getModelJSPVariableName() {
        return "model";
    }

    protected MultiPageModel getModel(PortletRequest request) {
        return new BaseCAHandler.CAModel(request);
    }
}
