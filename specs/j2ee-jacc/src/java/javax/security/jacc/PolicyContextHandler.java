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
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
 
package javax.security.jacc;

/**
 * This interface defines the methods that must be implemented by handlers that
 * are to be registered and activated by the <code>PolicyContext</code> class.
 * The <code>PolicyContext</code> class provides methods for containers to
 * register and activate container-specific <code>PolicyContext</code> handlers.
 * <code>Policy</code> providers use the <code>PolicyContext</code> class to
 * activate handlers to obtain (from the container) additional policy relevant
 * context to apply in their access decisions. All handlers registered and
 * activated via the <code>PolicyContext</code> class must implement the
 * <code>PolicyContextHandler</code> interface. 
 * @version $Revision: 1.2 $ $Date: 2003/11/18 05:30:16 $
 */
public interface PolicyContextHandler {

    /**
     * This public method returns a boolean result indicating whether or not
     * the handler supports the context object identified by the
     * (case-sensitive) key value.
     * @param key a <code>String</code< value identifying a context object
     * that could be supported by the handler. The value of this parameter
     * must not be null.
     * @return a boolean indicating whether or not the context object
     * corresponding to the argument key is handled by the handler.
     * @throws PolicyContextException if the implementation throws a checked
     * exception that has not been accounted for by the method signature. The
     * exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown PolicyContextException
     */
    public boolean supports(String key) throws PolicyContextException;

    /**
     * This public method returns the keys identifying the context objects
     * supported by the handler. The value of each key supported by a handler
     * must be a non-null String value.
     * @return an array containing String values identifing the context objects
     * supported by the handler. The array must not contain duplicate key
     * values. In the unlikely case that the Handler supports no keys, the
     * handler must return a zero length array. The value null must never be
     * returned by this method.
     * @throws PolicyContextException if the implementation throws a checked
     * exception that has not been accounted for by the method signature. The
     * exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown PolicyContextException
     */
    public String[] getKeys() throws PolicyContextException;

    /**
     * This public method is used by the <code>PolicyContext/<code> class to
     * activate the handler and obtain from it the the context object
     * identified by the (case-sensitive) key. In addition to the key, the
     * handler will be activated with the handler data value associated within
     * the <code>PolicyContext</code> class with the thread on which the call
     * to this method is made.<p>
     *
     * Note that the policy context identifier associated with a thread is
     * available to the handler by calling PolicyContext.getContextID().
     * @param key a String that identifies the context object to be returned by
     * the handler. The value of this paramter must not be null.
     * @param data the handler data <code>Object</code> associated with the
     * thread on which the call to this method has been made. Note that the
     * value passed through this parameter may be null.
     * @return The container and handler specific <code>Object</code>
     * containing the desired context. A null value may be returned if the
     * value of the corresponding context is null.
     * @throws PolicyContextException if the implementation throws a checked
     * exception that has not been accounted for by the method signature. The
     * exception thrown by the implementation class will be encapsulated
     * (during construction) in the thrown PolicyContextException
     */
    public Object getContext(String key, Object data) throws PolicyContextException;
}
