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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

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
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:38 $
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
