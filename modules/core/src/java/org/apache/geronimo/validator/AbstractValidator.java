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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;

/**
 * The base class for actual validators.  Each validator groups all the tests
 * that apply to a single module type (so we'd expect to have an EJB validator,
 * a web app validator, etc.).
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/12 08:19:27 $
 */
public abstract class AbstractValidator implements Validator {
    private static final Log log = LogFactory.getLog(AbstractValidator.class);
    private ValidationContext context;

    /**
     * The subclass should return an array of ValidationTest subclasses.  This
     * class will execute all the tests in those classes according to their
     * DD and XPath configuration.
     */
    public abstract Class[] getTestClasses();

    /**
     * Subclasses may override this to double-check the module type and so on,
     * but they should call this implementation some time in their overridden
     * version.
     *
     * @return <tt>true</tt>, since this implementation doesn't actually check
     *         anything.
     */
    public boolean initialize(PrintWriter out, String moduleName, ClassLoader loader, ModuleType type, XmlObject[] standardDD, Object[] serverDD) {
        context = new ValidationContext(loader, moduleName, out, serverDD, standardDD, type);
        return true;
    }

    public ValidationResult validate() {
        ValidationResult result;
        try {
            result = validateAllModules();
        } catch(ValidationException e) {
            context.out.println("Validator ERROR (in "+context.moduleName+"): validation aborted with fatal error: "+e.getMessage());
            result = ValidationResult.ABORTED;
        } catch(Throwable t) {
            log.error("Unexpected failure during validation", t);
            context.out.println("Validator ERROR (in "+context.moduleName+"): validation failed due to unexpected error: "+t);
            result = ValidationResult.ABORTED;
        }
        return result;
    }

    private ValidationResult validateAllModules() {
        ValidationResult result = ValidationResult.PASSED;
        Class[] classes = getTestClasses();
        Map map = new HashMap();
        for(int i = 0; i < context.standardDD.length; i++) {
            XmlObject object = context.standardDD[i];
            map.put(object.schemaType(), object);
        }
        Set missing = new HashSet();
        masterLoop:
        for(int i = 0; i < classes.length; i++) {
            Class cls = classes[i];
            if(!ValidationTest.class.isAssignableFrom(cls)) {
                throw new IllegalArgumentException("Class "+cls.getName()+" for Validator "+getClass().getName()+" is not a ValidationTest!");
            }
            try {
                ValidationTest test = (ValidationTest)cls.newInstance();
                SchemaType dd = test.getSchemaType();
                String xpath = test.getXpath();
                if(dd == null) { // Call this once per module
                    ValidationResult temp = executeTest(cls, test, context);
                    result = resolveResult(result, temp);
                    if(result == ValidationResult.ABORTED) {
                        break;
                    }
                } else if(xpath == null) {
                    Object ddRoot = map.get(dd);
                    if(ddRoot == null) { // Call this once on a specific DD
                        if(!missing.contains(dd)) {
                            log.info("No "+dd+" available for validation");
                            missing.add(dd);
                        }
                    } else {
                        context.setCurrentStandardDD(ddRoot);
                        context.setCurrentNode(null);
                        ValidationResult temp = executeTest(cls, test, context);
                        result = resolveResult(result, temp);
                        if(result == ValidationResult.ABORTED) {
                            break;
                        }
                    }
                } else { // looking for specific XPaths, call this once per match
                    Object ddRoot = map.get(dd);
                    if(ddRoot == null) { // Call this once on a specific DD
                        if(!missing.contains(dd)) {
                            log.info("No "+dd+" available for validation");
                            missing.add(dd);
                        }
                    } else {
                        JXPathContext ctx = JXPathContext.newContext(ddRoot);
                        xpath = javify(xpath);
                        log.debug("Looking for XPath "+xpath+" on bean "+ddRoot.getClass().getName());
                        for(Iterator it = ctx.iterate(xpath); it.hasNext();) {
                            context.setCurrentStandardDD(ddRoot);
                            context.setCurrentNode(it.next());
                            ValidationResult temp = executeTest(cls, test, context);
                            result = resolveResult(result, temp);
                            if(result == ValidationResult.ABORTED) {
                                break masterLoop;
                            }
                        }
                    }
                }
            } catch(InstantiationException e) {
                log.error("Class "+cls.getName()+" for Validator "+getClass().getName()+" cannot be instantiated", e);
                throw new IllegalArgumentException("Class "+cls.getName()+" for Validator "+getClass().getName()+" cannot be instantiated");
            } catch(IllegalAccessException e) {
                log.error("Class "+cls.getName()+" for Validator "+getClass().getName()+" cannot be instantiated", e);
                throw new IllegalArgumentException("Class "+cls.getName()+" for Validator "+getClass().getName()+" cannot be instantiated");
            }
        }
        return result;
    }

    private String javify(String xpath) {
        StringBuffer buf = new StringBuffer();
        int last = 0;
        int pos = xpath.indexOf('-');
        while(pos > -1) {
            buf.append(xpath.substring(last, pos));
            buf.append(Character.toUpperCase(xpath.charAt(pos+1)));
            last = pos+2;
            pos = xpath.indexOf('-', last);
        }
        buf.append(xpath.substring(last));
        return buf.toString();
    }

    private ValidationResult resolveResult(ValidationResult current, ValidationResult latest) {
        if(latest == ValidationResult.ABORTED || latest == ValidationResult.FAILED) {
            return latest;
        } else if(latest == ValidationResult.PASSED_WITH_WARNINGS) {
            if(current == ValidationResult.FAILED) {
                return current;
            } else {
                return latest;
            }
        } else if(latest == ValidationResult.PASSED) {
            return current;
        } else {
            throw new IllegalArgumentException("Unexpected ValidationResult "+latest);
        }
    }

    private ValidationResult executeTest(Class cls, ValidationTest test, ValidationContext context) {
        if(!cls.getName().equals(test.getClass().getName())) {
            throw new IllegalArgumentException("Unexpected test/class mismatch ("+cls.getName()+"<>"+test.getClass().getName()+")");
        }
        log.debug("Executing tests on "+cls.getName());
        ValidationResult result = ValidationResult.PASSED;
        ValidationResult temp = test.initializeTest(context);
        result = resolveResult(result, temp);
        if(result == ValidationResult.ABORTED) {
            return result;
        } else if(temp == ValidationResult.FAILED) { // If initialization failed, skip the tests
            return result;
        }
        Method ms[] = cls.getMethods();
        for(int i = 0; i < ms.length; i++) {
            Method m = ms[i];
            if(m.getName().startsWith("test") && Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0) {
                try {
                    log.debug("Running test: "+m.getName());
                    Object o = m.invoke(test, new Object[0]);
                    if(m.getReturnType().equals(ValidationResult.class)) {
                        result = resolveResult(result, (ValidationResult)o);
                        if(result == ValidationResult.ABORTED) {
                            return result;
                        }
                    }
                } catch(IllegalAccessException e) {
                    log.error("Cannot invoke method "+m.getName()+" on test class "+cls.getName()+" for Validator "+getClass().getName(), e);
                    throw new IllegalArgumentException("Cannot invoke method "+m.getName()+" on test class "+cls.getName()+" for Validator "+getClass().getName());
                } catch(IllegalArgumentException e) {
                    log.error("Cannot invoke method "+m.getName()+" on test class "+cls.getName()+" for Validator "+getClass().getName(), e);
                    throw new IllegalArgumentException("Cannot invoke method "+m.getName()+" on test class "+cls.getName()+" for Validator "+getClass().getName());
                } catch(InvocationTargetException e) {
                    if(e.getTargetException() instanceof RuntimeException) {
                        throw (RuntimeException)e.getTargetException();
                    } else {
                        log.error("Unexpected exception invoking method "+m.getName()+" on test class "+cls.getName()+" for Validator "+getClass().getName(), e);
                        throw new ValidationException("Unexpected exception from a test method: "+e.getClass().getName());
                    }
                }
            }
        }
        return result;
    }
}
