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

import java.io.Serializable;
import java.beans.PropertyEditorManager;

import org.apache.geronimo.common.propertyeditor.TextPropertyEditorSupport;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;


/**
 * @version $Rev$ $Date$
 */
public class Principal implements Serializable {

    static {
        PropertyEditorManager.registerEditor(Principal.class, PrincipalEditor.class);
    }

    private String className;
    private String principalName;
    private boolean designatedRunAs;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public boolean isDesignatedRunAs() {
        return designatedRunAs;
    }

    public void setDesignatedRunAs(boolean designatedRunAs) {
        this.designatedRunAs = designatedRunAs;
    }

    public static class PrincipalEditor extends TextPropertyEditorSupport {

        public void setAsText(String text) {
            if (text != null) {
                String[] parts = text.split("=");
                if (parts.length != 2) {
                    throw new PropertyEditorException("Principal should have the form 'name=class'");
                }
                Principal principal = new Principal();
                principal.setPrincipalName(parts[0]);
                principal.setClassName(parts[1]);
                setValue(principal);
            } else {
                setValue(null);
            }
        }

        public String getAsText() {
            Principal principal = (Principal) getValue();
            if (principal == null) {
                return null;
            }
            return new StringBuffer(principal.getPrincipalName()).append("=").append(principal.getClassName()).toString();
        }
    }
}
