/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.validator;

import java.io.PrintWriter;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.xmlbeans.XmlObject;

/**
 * The main interface for all application validators.  To use one, you
 * initialize it with the application module it should validate, and then
 * call the validate method.  In general, a single validator can validate
 * many application modules over its lifetime with repeated calls to
 * initialize and validate, but it cannot validate two application modules
 * simultaneously.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:44 $
 */
public interface Validator {
    /**
     * Prepares the validator to validate an application module.
     *
     * @param out        The writer used by the validator to present messages
     *                   to the user.
     * @param moduleName The display name for this module, used to identify
     *                   where errors originated from.
     * @param loader     The ClassLoader used to access module classes
     * @param type       The type of module in question (WAR, EAR, etc.).  In
     *                   general this is a sanity check as validators will be
     *                   devoted to handling a specific module.
     * @param standardDD The metadata representation of the standard deployment
     *                   descriptors for the module.  There will always be at
     *                   least one, and may be more.  Note that the file name
     *                   is available from the XmlDocumentProperties.
     * @param serverDD   The metadata representation of the app-server-specific
     *                   deployment descriptors for the module.
     *
     * @return <tt>true</tt> if the validator is going to be able to validate
     *         this application module.  May be false, for example, if this
     *         validator does not handle this module type, the expected
     *         deployment descriptors are missing, etc.
     */
    public boolean initialize(PrintWriter out, String moduleName, ClassLoader loader, ModuleType type, XmlObject[] standardDD, Object[] serverDD);

    /**
     *
     * @return An indicator of whether the validation succeeded, succeeded with
     *         warnings, or failed.
     */
    public ValidationResult validate();
}
