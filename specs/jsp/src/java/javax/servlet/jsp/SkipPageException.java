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
 * Exception to indicate the calling page must cease evaluation.
 * Thrown by a simple tag handler to indicate that the remainder of 
 * the page must not be evaluated.  The result is propagated back to
 * the pagein the case where one tag invokes another (as can be
 * the case with tag files).  The effect is similar to that of a 
 * Classic Tag Handler returning Tag.SKIP_PAGE from doEndTag().
 * Jsp Fragments may also throw this exception.  This exception
 * should not be thrown manually in a JSP page or tag file - the behavior is
 * undefined.  The exception is intended to be thrown inside 
 * SimpleTag handlers and in JSP fragments.
 * 
 * @see javax.servlet.jsp.tagext.SimpleTag#doTag
 * @see javax.servlet.jsp.tagext.JspFragment#invoke
 * @see javax.servlet.jsp.tagext.Tag#doEndTag
 * @since 2.0
 */
public class SkipPageException
    extends JspException
{
    /**
     * Creates a SkipPageException with no message.
     */
    public SkipPageException() {
        super();
    }
    
    /**
     * Creates a SkipPageException with the provided message.
     *
     * @param message the detail message
     */
    public SkipPageException( String message ) {
        super( message );
    }

    /**
     * Creates a SkipPageException with the provided message and root cause.
     *
     * @param message the detail message
     * @param rootCause the originating cause of this exception
     */
    public SkipPageException( String message, Throwable rootCause ) {
	super( message, rootCause );
    }

    /**
     * Creates a SkipPageException with the provided root cause.
     *
     * @param rootCause the originating cause of this exception
     */
    public SkipPageException( Throwable rootCause ) {
	super( rootCause );
    }
    
}


