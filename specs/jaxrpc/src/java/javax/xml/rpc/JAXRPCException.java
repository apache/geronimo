/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.xml.rpc;

/**
 * The <code>javax.xml.rpc.JAXRPCException</code> is thrown from
 * the core JAX-RPC APIs to indicate an exception related to the
 * JAX-RPC runtime mechanisms.
 *
 * @version 1.0
 */
public class JAXRPCException extends RuntimeException {

    // fixme: Why doesn't this use the jdk1.4 exception wrapping APIs?

    /** The cause of this error. */
    Throwable cause;
    
    /**
     * Constructs a new exception with <code>null</code> as its
     * detail message. The cause is not initialized.
     */
    public JAXRPCException() {}
    
    /**
     * Constructs a new exception with the specified detail
     * message.  The cause is not initialized.
     *
     * @param message The detail message which is later
     *            retrieved using the getMessage method
     */
    public JAXRPCException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new exception with the specified detail
     * message and cause.
     *
     * @param message The detail message which is later retrieved
     *            using the getMessage method
     * @param cause The cause which is saved for the later
     *            retrieval throw by the getCause method
     */
    public JAXRPCException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    
    /**
     * Constructs a new JAXRPCException with the specified cause
     * and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt> (which typically contains the
     * class and detail message of <tt>cause</tt>).
     *
     * @param cause The cause which is saved for the later
     *            retrieval throw by the getCause method.
     *            (A <tt>null</tt> value is permitted, and
     *            indicates that the cause is nonexistent or
     *          unknown.)
     */
    public JAXRPCException(Throwable cause) {
        super( (cause == null) ? null : cause.toString() );
        this.cause = cause;
    }
    
    /**
     * Gets the linked cause.
     *
     * @return The cause of this Exception or <code>null</code>
     *     if the cause is noexistent or unknown
     */
    public Throwable getLinkedCause() {
        return cause;
    }
    
}
