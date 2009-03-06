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

import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * Handler for Setup CA screen to get CA details from user.
 *
 * @version $Rev$ $Date$
 */
public class SetupCAHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(SetupCAHandler.class);
    
    public SetupCAHandler(BasePortlet portlet) {
        super(SETUPCA_MODE, "/WEB-INF/view/ca/setupCA.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"caCN", "caOU", "caO", "caL", "caST", "caC", "alias", "keyAlgorithm", "keySize", "algorithm", "validFrom", "validTo", "sNo"};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {"caCN", "caOU", "caO", "caL", "caST", "caC", "alias", "keyAlgorithm", "keySize", "algorithm", "validFrom", "validTo", "sNo"};
        for(int i = 0; i < params.length; ++i) {
            Object value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        try {
            // Validate the Serial Number
            String sNo = request.getParameter("sNo");
            new BigInteger(sNo.trim());
            
            // Validate the from and to dates
            String validFrom = request.getParameter("validFrom");
            String validTo = request.getParameter("validTo");
            // Check if the from date format is MM/DD/YYYY
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date validFromDate = df.parse(validFrom);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(validFromDate);
            String mmddyyyy = (calendar.get(Calendar.MONTH) < 9 ? "0":"") + (calendar.get(Calendar.MONTH)+1);
            mmddyyyy += "/"+(calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0":"") + (calendar.get(Calendar.DAY_OF_MONTH));
            mmddyyyy += "/"+calendar.get(Calendar.YEAR);
            if(!mmddyyyy.equals(validFrom)) {
                throw new Exception("validFrom must be a date in MM/DD/YYYY format.");
            }
            // Check if the to date format is MM/DD/YYYY
            Date validToDate = df.parse(validTo);
            calendar.setTime(validToDate);
            mmddyyyy = (calendar.get(Calendar.MONTH) < 9 ? "0":"") + (calendar.get(Calendar.MONTH)+1);
            mmddyyyy += "/"+(calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0":"") + (calendar.get(Calendar.DAY_OF_MONTH));
            mmddyyyy += "/"+calendar.get(Calendar.YEAR);
             if(!mmddyyyy.equals(validTo)) {
                throw new Exception("validTo must be a date in MM/DD/YYYY format.");
            }
            // Check if the from date is before the to date
            if(validFromDate.after(validToDate)) {
                throw new Exception("Validity: From date '"+validFrom+"' is before the To date '"+validTo+"'.");
            }

            // Load page to confirm CA details
            return CONFIRM_CA_MODE+BEFORE_ACTION;
        } catch(Exception e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg19"), e.getMessage());
            log.error("", e);
        }
        return getMode()+BEFORE_ACTION;
    }

}
