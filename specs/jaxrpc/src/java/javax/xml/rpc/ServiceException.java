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
 * The <code>javax.xml.rpc.ServiceException</code> is thrown from the
 * methods in the <code>javax.xml.rpc.Service</code> interface and
 * <code>ServiceFactory</code> class.
 *
 *
 * @version 1.0
 */
public class ServiceException extends Exception {
    
    // fixme: could we refactor this to use the jdk1.4 exception wrapping stuff?
    
    /** The cause of this exception. */
    Throwable cause;
    
    /**
     * Constructs a new exception with <code>null</code> as its
     * detail message. The cause is not initialized.
     */
    public ServiceException() {}
    
    /**
     * Constructs a new exception with the specified detail
     * message.  The cause is not initialized.
     *
     * @param message The detail message which is later
     *            retrieved using the <code>getMessage</code> method
     */
    public ServiceException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new exception with the specified detail
     * message and cause.
     *
     * @param message the detail message which is later retrieved
     *            using the <code>getMessage</code> method
     * @param cause the cause which is saved for the later
     *            retrieval throw by the <code>getCause</code>
     *            method
     */
    public ServiceException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }
    
    /**
     * Constructs a new exception with the specified cause
     * and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt> (which typically contains the
     * class and detail message of <tt>cause</tt>).
     *
     * @param cause the cause which is saved for the later
     *            retrieval throw by the getCause method.
     *            (A <tt>null</tt> value is permitted, and
     *            indicates that the cause is nonexistent or
     *          unknown.)
     */
    public ServiceException(Throwable cause) {
        super( (cause == null) ? null : cause.toString() );
        this.cause = cause;
    }
    
    /**
     * Gets the linked cause.
     *
     * @return the cause of this Exception or <code>null</code>
     *     if the cause is noexistent or unknown
     */
    public Throwable getLinkedCause() {
        return cause;
    }
    
}
