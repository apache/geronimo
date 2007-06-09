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
package org.apache.geronimo.security.deploy;

import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.common.propertyeditor.TextPropertyEditorSupport;


/**
 * @version $Rev$ $Date$
 */
public class LoginDomainPrincipalInfo extends PrincipalInfo {

    static {
        PropertyEditorManager.registerEditor(LoginDomainPrincipalInfo.class, LoginDomainPrincipalEditor.class);
    }

    public LoginDomainPrincipalInfo(String domainName, String className, String principalName) {
        super(className, principalName);
        this.domainName = domainName;
    }

    private final String domainName;

    public String getDomain() {
        return domainName;
    }

    public static class LoginDomainPrincipalEditor extends TextPropertyEditorSupport {

        public void setAsText(String text) {
            if (text != null) {
                String[] parts = text.split(",");
                if (parts.length != 3) {
                    throw new PropertyEditorException("Principal should have the form 'domain,class,name'");
                }
                LoginDomainPrincipalInfo principal = new LoginDomainPrincipalInfo(parts[0], parts[1], parts[2]);
                setValue(principal);
            } else {
                setValue(null);
            }
        }

        public String getAsText() {
            LoginDomainPrincipalInfo principal = (LoginDomainPrincipalInfo) getValue();
            if (principal == null) {
                return null;
            }
            return principal.getPrincipalName() + "," + principal.getClassName() + "," + principal.getDomain();
        }
    }
}
