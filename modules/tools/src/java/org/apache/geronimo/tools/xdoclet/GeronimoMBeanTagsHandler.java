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

package org.apache.geronimo.tools.xdoclet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import xdoclet.XDocletException;
import xdoclet.XDocletTagSupport;
import xjavadoc.XMethod;

/**
 * @xdoclet.taghandler namespace="Geronimo"
 * 
 * @version $Revision: 1.1 $ $Date: 2003/12/10 10:49:44 $
 */
public class GeronimoMBeanTagsHandler
    extends XDocletTagSupport
{

    /**
     * Gets the getter method associated to the current method. 
     * 
     * @param attributes
     * 
     * @return Getter.
     * 
     * @throws XDocletException Indicates that the method is never a getter nor
     * a setter.
     */
    public String getterName(Properties attributes) throws XDocletException {
        return getterName(getCurrentMethod());
    }
    
    /**
     * Gets the setter method associated to the current method. 
     * 
     * @param attributes
     * 
     * @return Setter.
     * 
     * @throws XDocletException Indicates that the method is never a getter nor
     * a setter.
     */
    public String setterName(Properties attributes) throws XDocletException {
        return setterName(getCurrentMethod());
    }

    /**
     * Gets the getter method associated to the specified method. 
     * 
     * @param aMethod Method whose getter is to be returned.
     * 
     * @return Getter.
     * 
     * @throws XDocletException Indicates that the method is never a getter nor
     * a setter.
     */
    private static String getterName(XMethod aMethod) throws XDocletException {
        String methodName = aMethod.getName(); 
        if ( methodName.startsWith("get") || methodName.startsWith("is") ) {
            return methodName;
        } else if ( methodName.startsWith("set") ) {
            String getterMethod1 = "get" + methodName.substring(3);
            String getterMethod2 = "is" + methodName.substring(3);
            Collection methods = aMethod.getContainingClass().getMethods();
            for (Iterator iter = methods.iterator(); iter.hasNext();) {
                XMethod method = (XMethod) iter.next();
                if ( method.getName().equals(getterMethod1) ||
                    method.getName().equals(getterMethod2)) {
                    return method.getName();
                }
            }
            return null;
        } else {
            throw new XDocletException("Method " + aMethod +
                " is never a getter nor a setter.");
        }
    }

    /**
     * Gets the setter method associated to the specified method. 
     * 
     * @param aMethod Method whose setter is to be returned.
     * 
     * @return Setter.
     * 
     * @throws XDocletException Indicates that the method is never a getter nor
     * a setter.
     */
    public static String setterName(XMethod aMethod) throws XDocletException {
        String methodName = aMethod.getName(); 
        if ( methodName.startsWith("set") ) {
            return methodName;
        } else if ( methodName.startsWith("get") || methodName.startsWith("is") ) {
            String setterMethod;
            if ( methodName.startsWith("get") ) {
                setterMethod = "set" + methodName.substring(3);
            } else {
                setterMethod = "set" + methodName.substring(2);
            }
            Collection methods = aMethod.getContainingClass().getMethods();
            for (Iterator iter = methods.iterator(); iter.hasNext();) {
                XMethod method = (XMethod) iter.next();
                if ( method.getName().equals(setterMethod) ) {
                    return method.getName();
                }
            }
            return null;
        } else {
            throw new XDocletException("Method " + aMethod +
                " is never a getter nor a setter.");
        }
    }

    /**
     * Indicates if the current method abstract a readable attribute.
     * 
     * @param template
     * @param attributes
     * 
     * @throws XDocletException Indicates that the method is never a getter nor
     * a setter. 
     */
    public void ifIsNotReadable(String template, Properties attributes)
        throws XDocletException {
        XMethod xMethod = getCurrentMethod();
            
        if ( null == getterName(xMethod) ) {
            generate(template);
        }
    }
    
    /**
     * Equivalent to !ifIsNotReadable.
     */
    public void ifIsReadable(String template, Properties attributes)
        throws XDocletException {
        XMethod xMethod = getCurrentMethod();
            
        if ( null != getterName(xMethod) ) {
            generate(template);
        }
    }

    /**
     * Indicates if the current method abstract a writable attribute.
     * 
     * @param template
     * @param attributes
     * 
     * @throws XDocletException Indicates that the method is never a getter nor
     * a setter. 
     */
    public void ifIsNotWritable(String template, Properties attributes)
        throws XDocletException {
        XMethod xMethod = getCurrentMethod();

        if ( null == setterName(xMethod) ) {
            generate(template);
        }
    }
    
    /**
     * Equivalent to !ifIsNotWritable.
     */
    public void ifIsWritable(String template, Properties attributes)
        throws XDocletException {
        XMethod xMethod = getCurrentMethod();

        if ( null != setterName(xMethod) ) {
            generate(template);
        }
    }
}
