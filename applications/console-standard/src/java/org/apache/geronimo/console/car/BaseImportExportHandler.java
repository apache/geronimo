/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.keystores.BaseKeystoreHandler;
import org.apache.geronimo.security.keystore.KeystoreInstance;
import org.apache.geronimo.util.CertificateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.PortletRequest;
import javax.portlet.ActionResponse;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * The base class for all handlers for this portlet
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public abstract class BaseImportExportHandler extends MultiPageAbstractHandler {
    private final static Log log = LogFactory.getLog(BaseImportExportHandler.class);
    protected static final String INDEX_MODE = "index";
    protected static final String LIST_MODE = "list";
    protected static final String DOWNLOAD_MODE = "download";
    protected static final String RESULTS_MODE = "results";

    protected BaseImportExportHandler(String mode, String viewName) {
        super(mode, viewName);
    }
}
