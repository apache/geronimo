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

import javax.servlet.jsp.*;


/**
 * Wraps any SimpleTag and exposes it using a Tag interface.  This is used
 * to allow collaboration between classic Tag handlers and SimpleTag
 * handlers.
 * <p>
 * Because SimpleTag does not extend Tag, and because Tag.setParent()
 * only accepts a Tag instance, a classic tag handler (one
 * that implements Tag) cannot have a SimpleTag as its parent.  To remedy
 * this, a TagAdapter is created to wrap the SimpleTag parent, and the
 * adapter is passed to setParent() instead.  A classic Tag Handler can
 * call getAdaptee() to retrieve the encapsulated SimpleTag instance.
 *
 * @since 2.0
 */
public class TagAdapter 
    implements Tag
{
    /** The simple tag that's being adapted. */
    private SimpleTag simpleTagAdaptee;

    /** The parent, of this tag, converted (if necessary) to be of type Tag. */
    private Tag parent;

    // Flag indicating whether we have already determined the parent
    private boolean parentDetermined;

    /**
     * Creates a new TagAdapter that wraps the given SimpleTag and 
     * returns the parent tag when getParent() is called.
     *
     * @param adaptee The SimpleTag being adapted as a Tag.
     */
    public TagAdapter( SimpleTag adaptee ) {
        if( adaptee == null ) {
	    // Cannot wrap a null adaptee.
	    throw new IllegalArgumentException();
        }
        this.simpleTagAdaptee = adaptee;
    }
    
    /**
     * Must not be called.
     *
     * @param pc ignored.
     * @throws UnsupportedOperationException Must not be called
     */
    public void setPageContext(PageContext pc) {
        throw new UnsupportedOperationException( 
            "Illegal to invoke setPageContext() on TagAdapter wrapper" );
    }


    /**
     * Must not be called.  The parent of this tag is always 
     * getAdaptee().getParent().
     *
     * @param parentTag ignored.
     * @throws UnsupportedOperationException Must not be called.
     */
    public void setParent( Tag parentTag ) {
        throw new UnsupportedOperationException( 
            "Illegal to invoke setParent() on TagAdapter wrapper" );
    }


    /**
     * Returns the parent of this tag, which is always
     * getAdaptee().getParent().  
     *
     * This will either be the enclosing Tag (if getAdaptee().getParent()
     * implements Tag), or an adapter to the enclosing Tag (if 
     * getAdaptee().getParent() does not implement Tag).
     *
     * @return The parent of the tag being adapted.
     */
    public Tag getParent() {
	if (!parentDetermined) {
	    JspTag adapteeParent = simpleTagAdaptee.getParent();
	    if (adapteeParent != null) {
		if (adapteeParent instanceof Tag) {
		    this.parent = (Tag) adapteeParent;
		} else {
		    // Must be SimpleTag - no other types defined.
		    this.parent = new TagAdapter((SimpleTag) adapteeParent);
		}
	    }
	    parentDetermined = true;
	}

	return this.parent;
    }
    
    /**
     * Gets the tag that is being adapted to the Tag interface.
     * This should be an instance of SimpleTag in JSP 2.0, but room
     * is left for other kinds of tags in future spec versions.
     *
     * @return the tag that is being adapted
     */
    public JspTag getAdaptee() {
        return this.simpleTagAdaptee;
    }

    /**
     * Must not be called.
     *
     * @return always throws UnsupportedOperationException
     * @throws UnsupportedOperationException Must not be called
     * @throws JspException never thrown
     */
    public int doStartTag() throws JspException {
        throw new UnsupportedOperationException( 
            "Illegal to invoke doStartTag() on TagAdapter wrapper" );
    }
 
    /**
     * Must not be called.
     *
     * @return always throws UnsupportedOperationException
     * @throws UnsupportedOperationException Must not be called
     * @throws JspException never thrown
     */
    public int doEndTag() throws JspException {
        throw new UnsupportedOperationException( 
            "Illegal to invoke doEndTag() on TagAdapter wrapper" );
    }

    /**
     * Must not be called.
     *
     * @throws UnsupportedOperationException Must not be called
     */
    public void release() {
        throw new UnsupportedOperationException( 
            "Illegal to invoke release() on TagAdapter wrapper" );
    }
}
