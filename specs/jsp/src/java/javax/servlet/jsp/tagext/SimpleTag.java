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

import javax.servlet.jsp.JspContext;

/**
 * Interface for defining Simple Tag Handlers.
 * 
 * <p>Simple Tag Handlers differ from Classic Tag Handlers in that instead 
 * of supporting <code>doStartTag()</code> and <code>doEndTag()</code>, 
 * the <code>SimpleTag</code> interface provides a simple 
 * <code>doTag()</code> method, which is called once and only once for any 
 * given tag invocation.  All tag logic, iteration, body evaluations, etc. 
 * are to be performed in this single method.  Thus, simple tag handlers 
 * have the equivalent power of <code>BodyTag</code>, but with a much 
 * simpler lifecycle and interface.</p>
 *
 * <p>To support body content, the <code>setJspBody()</code> 
 * method is provided.  The container invokes the <code>setJspBody()</code> 
 * method with a <code>JspFragment</code> object encapsulating the body of 
 * the tag.  The tag handler implementation can call 
 * <code>invoke()</code> on that fragment to evaluate the body as
 * many times as it needs.</p>
 *
 * <p>A SimpleTag handler must have a public no-args constructor.  Most
 * SimpleTag handlers should extend SimpleTagSupport.</p>
 * 
 * <p><b>Lifecycle</b></p>
 *
 * <p>The following is a non-normative, brief overview of the 
 * SimpleTag lifecycle.  Refer to the JSP Specification for details.</p>
 *
 * <ol>
 *   <li>A new tag handler instance is created each time by the container 
 *       by calling the provided zero-args constructor.  Unlike classic
 *       tag handlers, simple tag handlers are never cached and reused by
 *       the JSP container.</li>
 *   <li>The <code>setJspContext()</code> and <code>setParent()</code> 
 *       methods are called by the container.  The <code>setParent()</code>
 *       method is only called if the element is nested within another tag 
 *       invocation.</li>
 *   <li>The setters for each attribute defined for this tag are called
 *       by the container.</li>
 *   <li>If a body exists, the <code>setJspBody()</code> method is called 
 *       by the container to set the body of this tag, as a 
 *       <code>JspFragment</code>.  If the action element is empty in
 *       the page, this method is not called at all.</li>
 *   <li>The <code>doTag()</code> method is called by the container.  All
 *       tag logic, iteration, body evaluations, etc. occur in this 
 *       method.</li>
 *   <li>The <code>doTag()</code> method returns and all variables are
 *       synchronized.</li>
 * </ol>
 * 
 * @see SimpleTagSupport
 * @since 2.0
 */
public interface SimpleTag extends JspTag {
    
    /** 
     * Called by the container to invoke this tag.
     * The implementation of this method is provided by the tag library
     * developer, and handles all tag processing, body iteration, etc.
     *
     * <p>
     * The JSP container will resynchronize any AT_BEGIN and AT_END
     * variables (defined by the associated tag file, TagExtraInfo, or TLD)
     * after the invocation of doTag().
     * 
     * @throws javax.servlet.jsp.JspException If an error occurred 
     *     while processing this tag.
     * @throws javax.servlet.jsp.SkipPageException If the page that
     *     (either directly or indirectly) invoked this tag is to
     *     cease evaluation.  A Simple Tag Handler generated from a 
     *     tag file must throw this exception if an invoked Classic 
     *     Tag Handler returned SKIP_PAGE or if an invoked Simple
     *     Tag Handler threw SkipPageException or if an invoked Jsp Fragment
     *     threw a SkipPageException.
     * @throws java.io.IOException If there was an error writing to the
     *     output stream.
     */ 
    public void doTag() 
        throws javax.servlet.jsp.JspException, java.io.IOException;
    
    /**
     * Sets the parent of this tag, for collaboration purposes.
     * <p>
     * The container invokes this method only if this tag invocation is 
     * nested within another tag invocation.
     *
     * @param parent the tag that encloses this tag
     */
    public void setParent( JspTag parent );
    
    /**
     * Returns the parent of this tag, for collaboration purposes.
     *
     * @return the parent of this tag
     */ 
    public JspTag getParent();
    
    /**
     * Called by the container to provide this tag handler with
     * the <code>JspContext</code> for this invocation.
     * An implementation should save this value.
     * 
     * @param pc the page context for this invocation
     * @see Tag#setPageContext
     */
    public void setJspContext( JspContext pc );
                
    /** 
     * Provides the body of this tag as a JspFragment object, able to be 
     * invoked zero or more times by the tag handler. 
     * <p>
     * This method is invoked by the JSP page implementation 
     * object prior to <code>doTag()</code>.  If the action element is
     * empty in the page, this method is not called at all.
     * 
     * @param jspBody The fragment encapsulating the body of this tag.
     */ 
    public void setJspBody( JspFragment jspBody );

    
}
