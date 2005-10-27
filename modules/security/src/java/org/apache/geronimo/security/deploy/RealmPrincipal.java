/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.security.deploy;

import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.common.propertyeditor.TextPropertyEditorSupport;


/**
 * @version $Rev: 154957 $ $Date: 2005-02-22 21:07:36 -0800 (Tue, 22 Feb 2005) $
 */
public class RealmPrincipal extends LoginDomainPrincipal {

    static {
        PropertyEditorManager.registerEditor(RealmPrincipal.class, RealmPrincipalEditor.class);
    }

    private final String realm;

    public RealmPrincipal(String realm, String domainName, String className, String principalName, boolean designatedRunAs) {
        super(domainName, className, principalName, designatedRunAs);
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

    public static class RealmPrincipalEditor extends TextPropertyEditorSupport {

        public void setAsText(String text) {
            if (text != null) {
                String[] parts = text.split(",");
                if (parts.length != 5) {
                    throw new PropertyEditorException("Principal should have the form 'domain,realm,class,name,run-as'");
                }
                RealmPrincipal principal = new RealmPrincipal(parts[0], parts[1], parts[2], parts[3], Boolean.valueOf(parts[4]).booleanValue());
                setValue(principal);
            } else {
                setValue(null);
            }
        }

        public String getAsText() {
            RealmPrincipal principal = (RealmPrincipal) getValue();
            if (principal == null) {
                return null;
            }
            return principal.getPrincipalName() + "," + principal.getClassName() + "," + principal.isDesignatedRunAs() + "," + principal.getDomain() + "," + principal.getRealm();
        }
    }
}
