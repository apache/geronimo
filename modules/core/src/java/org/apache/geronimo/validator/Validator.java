/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.2 $ $Date: 2004/02/12 08:19:27 $
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
