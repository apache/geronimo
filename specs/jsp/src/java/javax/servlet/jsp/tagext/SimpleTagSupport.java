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
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * A base class for defining tag handlers implementing SimpleTag.
 * <p>
 * The SimpleTagSupport class is a utility class intended to be used
 * as the base class for new simple tag handlers.  The SimpleTagSupport
 * class implements the SimpleTag interface and adds additional
 * convenience methods including getter methods for the properties in
 * SimpleTag.
 *
 * @since 2.0
 */
public class SimpleTagSupport 
    implements SimpleTag
{
    /** Reference to the enclosing tag. */
    private JspTag parentTag;
    
    /** The JSP context for the upcoming tag invocation. */
    private JspContext jspContext;
    
    /** The body of the tag. */
    private JspFragment jspBody;
    
    /**
     * Sole constructor. (For invocation by subclass constructors, 
     * typically implicit.)
     */
    public SimpleTagSupport() {
    }
    
    /** 
     * Default processing of the tag does nothing.
     *
     * @throws JspException Subclasses can throw JspException to indicate
     *     an error occurred while processing this tag.
     * @throws javax.servlet.jsp.SkipPageException If the page that
     *     (either directly or indirectly) invoked this tag is to
     *     cease evaluation.  A Simple Tag Handler generated from a 
     *     tag file must throw this exception if an invoked Classic 
     *     Tag Handler returned SKIP_PAGE or if an invoked Simple
     *     Tag Handler threw SkipPageException or if an invoked Jsp Fragment
     *     threw a SkipPageException.
     * @throws IOException Subclasses can throw IOException if there was
     *     an error writing to the output stream
     * @see SimpleTag#doTag()
     */ 
    public void doTag() 
        throws JspException, IOException
    {
    }
    
    /**
     * Sets the parent of this tag, for collaboration purposes.
     * <p>
     * The container invokes this method only if this tag invocation is
     * nested within another tag invocation.
     *
     * @param parent the tag that encloses this tag
     */
    public void setParent( JspTag parent ) {
        this.parentTag = parent;
    }
    
    /**
     * Returns the parent of this tag, for collaboration purposes.
     *
     * @return the parent of this tag
     */ 
    public JspTag getParent() {
        return this.parentTag;
    }
    
    /**
     * Stores the provided JSP context in the private jspContext field.
     * Subclasses can access the <code>JspContext</code> via 
     * <code>getJspContext()</code>.
     * 
     * @param pc the page context for this invocation
     * @see SimpleTag#setJspContext
     */
    public void setJspContext( JspContext pc ) {
        this.jspContext = pc;
    }
    
    /**
     * Returns the page context passed in by the container via 
     * setJspContext.
     *
     * @return the page context for this invocation
     */
    protected JspContext getJspContext() {
        return this.jspContext;
    }
                
    /** 
     * Stores the provided JspFragment.
     *
     * @param jspBody The fragment encapsulating the body of this tag.
     *     If the action element is empty in the page, this method is 
     *     not called at all.
     * @see SimpleTag#setJspBody
     */ 
    public void setJspBody( JspFragment jspBody ) {
        this.jspBody = jspBody;
    }
    
    /**
     * Returns the body passed in by the container via setJspBody.
     *
     * @return the fragment encapsulating the body of this tag, or
     *    null if the action element is empty in the page.
     */
    protected JspFragment getJspBody() {
        return this.jspBody;
    }

    /**
     * Find the instance of a given class type that is closest to a given
     * instance.
     * This method uses the getParent method from the Tag and/or SimpleTag
     * interfaces.  This method is used for coordination among 
     * cooperating tags.
     *
     * <p> For every instance of TagAdapter
     * encountered while traversing the ancestors, the tag handler returned by
     * <tt>TagAdapter.getAdaptee()</tt> - instead of the TagAdpater itself -
     * is compared to <tt>klass</tt>. If the tag handler matches, it - and
     * not its TagAdapter - is returned.
     *
     * <p>
     * The current version of the specification only provides one formal
     * way of indicating the observable type of a tag handler: its
     * tag handler implementation class, described in the tag-class
     * subelement of the tag element.  This is extended in an
     * informal manner by allowing the tag library author to
     * indicate in the description subelement an observable type.
     * The type should be a subtype of the tag handler implementation
     * class or void.
     * This addititional constraint can be exploited by a
     * specialized container that knows about that specific tag library,
     * as in the case of the JSP standard tag library.
     *
     * <p>
     * When a tag library author provides information on the
     * observable type of a tag handler, client programmatic code
     * should adhere to that constraint.  Specifically, the Class
     * passed to findAncestorWithClass should be a subtype of the
     * observable type.
     * 
     *
     * @param from The instance from where to start looking.
     * @param klass The subclass of JspTag or interface to be matched
     * @return the nearest ancestor that implements the interface
     * or is an instance of the class specified
     */
    public static final JspTag findAncestorWithClass(
	JspTag from, Class klass) 
    {
	boolean isInterface = false;

	if (from == null || klass == null
	        || (!JspTag.class.isAssignableFrom(klass)
		    && !(isInterface = klass.isInterface()))) {
	    return null;
	}

	for (;;) {
	    JspTag parent = null;
	    if( from instanceof SimpleTag ) {
		parent = ((SimpleTag)from).getParent();
	    }
	    else if( from instanceof Tag ) {
		parent = ((Tag)from).getParent();
	    }
	    if (parent == null) {
		return null;
	    }

	    if (parent instanceof TagAdapter) {
		parent = ((TagAdapter) parent).getAdaptee();
	    }

	    if ((isInterface && klass.isInstance(parent))
		    || klass.isAssignableFrom(parent.getClass())) {
		return parent;
	    }

	    from = parent;
	}
    }    
}
