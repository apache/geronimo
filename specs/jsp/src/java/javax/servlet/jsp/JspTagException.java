/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 */
package javax.servlet.jsp;

/**
 * Exception to be used by a Tag Handler to indicate some unrecoverable
 * error.
 * This error is to be caught by the top level of the JSP page and will result
 * in an error page.
 */

public class JspTagException extends JspException {
    /**
     * Constructs a new JspTagException with the specified message.
     * The message can be written to the server log and/or displayed
     * for the user.
     * 
     * @param msg a <code>String</code> specifying the text of 
     *     the exception message
     */
    public JspTagException(String msg) {
	super( msg );
    }

    /**
     * Constructs a new JspTagException with no message.
     */
    public JspTagException() {
	super();
    }

    /**
     * Constructs a new JspTagException when the JSP Tag
     * needs to throw an exception and include a message 
     * about the "root cause" exception that interfered with its 
     * normal operation, including a description message.
     *
     *
     * @param message 		a <code>String</code> containing 
     *				the text of the exception message
     *
     * @param rootCause		the <code>Throwable</code> exception 
     *				that interfered with the JSP Tag's
     *				normal operation, making this JSP Tag
     *				exception necessary
     *
     * @since 2.0
     */
    public JspTagException(String message, Throwable rootCause) {
	super( message, rootCause );
    }


    /**
     * Constructs a new JSP Tag exception when the JSP Tag
     * needs to throw an exception and include a message
     * about the "root cause" exception that interfered with its
     * normal operation.  The exception's message is based on the localized
     * message of the underlying exception.
     *
     * <p>This method calls the <code>getLocalizedMessage</code> method
     * on the <code>Throwable</code> exception to get a localized exception
     * message. When subclassing <code>JspTagException</code>, 
     * this method can be overridden to create an exception message 
     * designed for a specific locale.
     *
     * @param rootCause 	the <code>Throwable</code> exception
     * 				that interfered with the JSP Tag's
     *				normal operation, making the JSP Tag 
     *                          exception necessary
     *
     * @since 2.0
     */

    public JspTagException(Throwable rootCause) {
	super( rootCause );
    }

}
