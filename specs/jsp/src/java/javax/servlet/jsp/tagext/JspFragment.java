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
 
package javax.servlet.jsp.tagext;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.jsp.*;

/**
 * Encapsulates a portion of JSP code in an object that 
 * can be invoked as many times as needed.  JSP Fragments are defined 
 * using JSP syntax as the body of a tag for an invocation to a SimpleTag 
 * handler, or as the body of a &lt;jsp:attribute&gt; standard action
 * specifying the value of an attribute that is declared as a fragment,
 * or to be of type JspFragment in the TLD.
 * <p>
 * The definition of the JSP fragment must only contain template 
 * text and JSP action elements.  In other words, it must not contain
 * scriptlets or scriptlet expressions.  At translation time, the 
 * container generates an implementation of the JspFragment abstract class
 * capable of executing the defined fragment.
 * <p>
 * A tag handler can invoke the fragment zero or more times, or 
 * pass it along to other tags, before returning.  To communicate values
 * to/from a JSP fragment, tag handlers store/retrieve values in 
 * the JspContext associated with the fragment.
 * <p>
 * Note that tag library developers and page authors should not generate
 * JspFragment implementations manually.
 * <p>
 * <i>Implementation Note</i>: It is not necessary to generate a 
 * separate class for each fragment.  One possible implementation is 
 * to generate a single helper class for each page that implements 
 * JspFragment. Upon construction, a discriminator can be passed to 
 * select which fragment that instance will execute.
 *
 * @since 2.0
 */
public abstract class JspFragment {

    /**
     * Executes the fragment and directs all output to the given Writer,
     * or the JspWriter returned by the getOut() method of the JspContext
     * associated with the fragment if out is null.
     *
     * @param out The Writer to output the fragment to, or null if 
     *     output should be sent to JspContext.getOut().
     * @throws javax.servlet.jsp.JspException Thrown if an error occured
     *     while invoking this fragment.
     * @throws javax.servlet.jsp.SkipPageException Thrown if the page
     *     that (either directly or indirectly) invoked the tag handler that
     *     invoked this fragment is to cease evaluation.  The container
     *     must throw this exception if a Classic Tag Handler returned
     *     Tag.SKIP_PAGE or if a Simple Tag Handler threw SkipPageException.
     * @throws java.io.IOException If there was an error writing to the 
     *     stream.
     */
    public abstract void invoke( Writer out )
        throws JspException, IOException;

    /**
     * Returns the JspContext that is bound to this JspFragment.
     *
     * @return The JspContext used by this fragment at invocation time.
     */
    public abstract JspContext getJspContext();

}
