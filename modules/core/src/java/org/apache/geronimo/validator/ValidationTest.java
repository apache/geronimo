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

import org.apache.xmlbeans.SchemaType;

/**
 * The base class for all validation tests.  They will work similarly to JUnit
 * tests, in that any method beginning with "test" will be executed.  The test
 * methods should return a ValidationResult, though they may also indicate
 * problems by throwing a ValidationException.  The difference is that a
 * ValidationException is considered to be fatal, whereas a ValidationResult
 * may indicate a warning or a non-fatal error as well.
 *
 * Each test class may specify a deployment descriptor and XPath, in which case
 * the same test class will have its tests executed multiple times with a
 * different context each time (once for each DD and/or hit on the XPath).
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/12 08:19:27 $
 */
public abstract class ValidationTest {
    protected ValidationContext context;

    /**
     * Any given test class is typically associated with one particular
     * deployment descriptor.  If the returns a non-null value, this test will
     * be invoked with a context specific to the provided deployment
     * descriptor.  If it returns null, this test will be invoked once for the
     * module as a whole.
     *
     * @return A standard deployment descriptor name, with no directory, (such
     *          as "ejb-jar.xml"), or <tt>null</tt> to be invoked with
     *         no particular DD context.
     */
    public abstract SchemaType getSchemaType();

    /**
     * Any given test class can be associated with a specific XPath in its
     * deployment descriptor.  It wil be invoked one for every hit on that
     * XPath, with a context set to the node that matches the XPath.  For
     * example, a test class that returns a standard DD name of
     * "ejb-jar.xml" and an XPath of "/ejb-jar/enterprise-beans/session"
     * would be invoked once on every session bean.
     *
     * @return An Xpath of interest to this test class, or <tt>null</tt> if
     *         the class should be invoked once on the entire deployment
     *         descriptor or module.  If getSchemaType returns null,
     *         this method nust return null as well.
     */
    public abstract String getXpath();

    /**
     * Called before any test methods are run in order to establish the
     * context for the test.  This method will typically read the current
     * node (set corresponding to the XPath of interest) and cast it to
     * some specific object type (from XmlObject to SessionBeanType, for
     * example).
     *
     * @param context The context for all test methods in this class to use.
     *
     * @return The result of the initialization.  Usually returns success,
     *         but may not in a case, for example, where during initialization
     *         it is determined that no tests could run successfully (due to
     *         missing classes or whatever).  If this method does not return
     *         PASSED or PASSED_WITH_WARNINGS, then no test methods on this
     *         class will be run.
     */
    public abstract ValidationResult initialize(ValidationContext context);

    /**
     * Loads a class from the application module.
     *
     * @param className   The fully-qualified Java class name of the class to
     *                    load.
     * @param description A description of the why this class is being loaded,
     *                    used in an error message if it cannot be loaded.
     *                    ("Class [className] could not be loaded ([description])")
     *
     * @return A class.
     *
     * @throws ValidationException occurs when the class can't be loaded.
     *         Don't both to catch this exception unless you don't want a
     *         failure to load the class to be interpreted as a fatal error.
     */
    protected Class loadClass(String className, String description) throws ValidationException {
        try {
            return context.loader.loadClass(className);
        } catch(ClassNotFoundException e) {
            throw new ValidationException("Class "+className+" could not be loaded ("+description+")", e);
        }
    }

    protected ValidationResult warn(String messageCode, ValidationResult oldState) {
        //todo: for all of these, internationalize the introductory text
        context.out.println("Validator WARNING (in "+context.moduleName+") "+getMessage(messageCode, null));
        if(oldState == ValidationResult.PASSED) {
            return ValidationResult.PASSED_WITH_WARNINGS;
        } else {
            return oldState;
        }
    }

    protected ValidationResult warn(String messageCode, ValidationResult oldState, Object param) {
        context.out.println("Validator WARNING (in "+context.moduleName+") "+getMessage(messageCode, new Object[]{param}));
        if(oldState == ValidationResult.PASSED) {
            return ValidationResult.PASSED_WITH_WARNINGS;
        } else {
            return oldState;
        }
    }

    protected ValidationResult warn(String messageCode, ValidationResult oldState, Object[] params) {
        context.out.println("Validator WARNING (in "+context.moduleName+") "+getMessage(messageCode, params));
        if(oldState == ValidationResult.PASSED) {
            return ValidationResult.PASSED_WITH_WARNINGS;
        } else {
            return oldState;
        }
    }

    protected ValidationResult warn(String messageCode) {
        //todo: for all of these, internationalize the introductory text
        context.out.println("Validator WARNING (in "+context.moduleName+") "+getMessage(messageCode, null));
        return ValidationResult.PASSED_WITH_WARNINGS;
    }

    protected ValidationResult warn(String messageCode, Object param) {
        context.out.println("Validator WARNING (in "+context.moduleName+") "+getMessage(messageCode, new Object[]{param}));
        return ValidationResult.PASSED_WITH_WARNINGS;
    }

    protected ValidationResult warn(String messageCode, Object[] params) {
        context.out.println("Validator WARNING (in "+context.moduleName+") "+getMessage(messageCode, params));
        return ValidationResult.PASSED_WITH_WARNINGS;
    }

    protected ValidationResult error(String messageCode, ValidationResult oldState) {
        context.out.println("Validator ERROR (in "+context.moduleName+") "+getMessage(messageCode, null));
        if(oldState == ValidationResult.ABORTED) {
            return oldState;
        } else {
            return ValidationResult.FAILED;
        }
    }

    protected ValidationResult error(String messageCode, ValidationResult oldState, Object param) {
        context.out.println("Validator ERROR (in "+context.moduleName+") "+getMessage(messageCode, new Object[]{param}));
        if(oldState == ValidationResult.ABORTED) {
            return oldState;
        } else {
            return ValidationResult.FAILED;
        }
    }

    protected ValidationResult error(String messageCode, ValidationResult oldState, Object[] params) {
        context.out.println("Validator ERROR (in "+context.moduleName+") "+getMessage(messageCode, params));
        if(oldState == ValidationResult.ABORTED) {
            return oldState;
        } else {
            return ValidationResult.FAILED;
        }
    }

    protected ValidationResult error(String messageCode) {
        context.out.println("Validator ERROR (in "+context.moduleName+") "+getMessage(messageCode, null));
        return ValidationResult.FAILED;
    }

    protected ValidationResult error(String messageCode, Object param) {
        context.out.println("Validator ERROR (in "+context.moduleName+") "+getMessage(messageCode, new Object[]{param}));
        return ValidationResult.FAILED;
    }

    protected ValidationResult error(String messageCode, Object[] params) {
        context.out.println("Validator ERROR (in "+context.moduleName+") "+getMessage(messageCode, params));
        return ValidationResult.FAILED;
    }

    private String getMessage(String messageCode, Object[] params) {
        return messageCode+" "+params; //todo: format the message from a resource file
    }

    ValidationResult initializeTest(ValidationContext context) {
        this.context = context;
        return initialize(context);
    }
}
