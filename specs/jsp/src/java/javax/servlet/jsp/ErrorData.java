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
 * Contains information about an error, for error pages.
 * The information contained in this instance is meaningless if not used
 * in the context of an error page.  To indicate a JSP is an error page,
 * the page author must set the isErrorPage attribute of the page directive
 * to "true".
 *
 * @see PageContext#getErrorData
 * @since 2.0
 */

public final class ErrorData {

    private Throwable throwable;
    private int statusCode;
    private String uri;
    private String servletName;

    /**
     * Creates a new ErrorData object.
     *
     * @param throwable The Throwable that is the cause of the error
     * @param statusCode The status code of the error
     * @param uri The request URI
     * @param servletName The name of the servlet invoked
     */
    public ErrorData( Throwable throwable, int statusCode, String uri, 
	String servletName )
    {
	this.throwable = throwable;
	this.statusCode = statusCode;
	this.uri = uri;
	this.servletName = servletName;
    }

    /**
     * Returns the Throwable that caused the error.
     *
     * @return The Throwable that caused the error
     */
    public Throwable getThrowable() {
	return this.throwable;
    }

    /**
     * Returns the status code of the error.
     *
     * @return The status code of the error
     */
    public int getStatusCode() {
	return this.statusCode;
    }

    /**
     * Returns the request URI.
     *
     * @return The request URI
     */
    public String getRequestURI() {
	return this.uri;
    }

    /**
     * Returns the name of the servlet invoked.
     *
     * @return The name of the servlet invoked
     */
    public String getServletName() {
	return this.servletName;
    }
}
