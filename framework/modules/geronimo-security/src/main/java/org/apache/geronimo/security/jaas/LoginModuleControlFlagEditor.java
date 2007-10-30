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

package org.apache.geronimo.security.jaas;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.common.propertyeditor.TextPropertyEditorSupport;


/**
 * A property editor for login module flags.  This is used by GBeans when flags
 * are specified in config files.
 *
 * @version $Rev$ $Date$
 */
public class LoginModuleControlFlagEditor extends TextPropertyEditorSupport {

    public LoginModuleControlFlagEditor() {
        super();
    }

    public LoginModuleControlFlagEditor(final Object source) {
        super(source);
    }

    public Object getValue() {
        if ("REQUIRED".equals(getAsText())) {
            return LoginModuleControlFlag.REQUIRED;
        } else if ("REQUISITE".equals(getAsText())) {
            return LoginModuleControlFlag.REQUISITE;
        } else if ("SUFFICIENT".equals(getAsText())) {
            return LoginModuleControlFlag.SUFFICIENT;
        } else if ("OPTIONAL".equals(getAsText())) {
            return LoginModuleControlFlag.OPTIONAL;
        }
        throw new PropertyEditorException("Illegal value: '" + getAsText() + "'");
    }
}
