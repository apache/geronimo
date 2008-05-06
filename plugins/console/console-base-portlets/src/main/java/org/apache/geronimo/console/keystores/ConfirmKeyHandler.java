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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.KeystoreException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Handler for entering a password to unlock a keystore
 *
 * @version $Rev$ $Date$
 */
public class ConfirmKeyHandler extends BaseKeystoreHandler {
    private static final Logger log = LoggerFactory.getLogger(ConfirmKeyHandler.class);

    public ConfirmKeyHandler() {
        super(CONFIRM_KEY, "/WEB-INF/view/keystore/confirmKey.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String keystore = request.getParameter("keystore");
        String alias = request.getParameter("alias");
        String password = request.getParameter("password");
        String keySize = request.getParameter("keySize");
        String algorithm = request.getParameter("algorithm");
        String valid = request.getParameter("valid");
        String certCN = request.getParameter("certCN");
        String certOU = request.getParameter("certOU");
        String certO = request.getParameter("certO");
        String certL = request.getParameter("certL");
        String certST = request.getParameter("certST");
        String certC = request.getParameter("certC");
        request.setAttribute("keystore", keystore);
        request.setAttribute("alias", alias);
        request.setAttribute("password", password);
        request.setAttribute("keySize", keySize);
        request.setAttribute("algorithm", algorithm);
        request.setAttribute("valid", valid);
        request.setAttribute("validFrom", sdf.format(new Date()));
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(valid));
        request.setAttribute("validTo", sdf.format(cal.getTime()));
        request.setAttribute("certCN", certCN);
        request.setAttribute("certOU", certOU);
        request.setAttribute("certO", certO);
        request.setAttribute("certL", certL);
        request.setAttribute("certST", certST);
        request.setAttribute("certC", certC);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String keystore = request.getParameter("keystore");
        String alias = request.getParameter("alias");
        String password = request.getParameter("password");
        String keySize = request.getParameter("keySize");
        String algorithm = request.getParameter("algorithm");
        String valid = request.getParameter("valid");
        String certCN = request.getParameter("certCN");
        String certOU = request.getParameter("certOU");
        String certO = request.getParameter("certO");
        String certL = request.getParameter("certL");
        String certST = request.getParameter("certST");
        String certC = request.getParameter("certC");

        KeystoreData data = ((KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + keystore));
        try {
            data.createKeyPair(alias, password, "RSA", Integer.parseInt(keySize), algorithm, Integer.parseInt(valid),
                    certCN, certOU, certO, certL, certST, certC);
        } catch (NumberFormatException e) {
            throw new PortletException(e);
        } catch (KeystoreException e) {
            throw new PortletException(e);
        }
        response.setRenderParameter("id", keystore);
        return VIEW_KEYSTORE+BEFORE_ACTION;
    }
}
