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

/**
 * Optional class provided by the tag library author to describe additional
 * translation-time information not described in the TLD.
 * The TagExtraInfo class is mentioned in the Tag Library Descriptor file (TLD).
 *
 * <p>
 * This class can be used:
 * <ul>
 * <li> to indicate that the tag defines scripting variables
 * <li> to perform translation-time validation of the tag attributes.
 * </ul>
 *
 * <p>
 * It is the responsibility of the JSP translator that the initial value
 * to be returned by calls to getTagInfo() corresponds to a TagInfo
 * object for the tag being translated. If an explicit call to
 * setTagInfo() is done, then the object passed will be returned in
 * subsequent calls to getTagInfo().
 * 
 * <p>
 * The only way to affect the value returned by getTagInfo()
 * is through a setTagInfo() call, and thus, TagExtraInfo.setTagInfo() is
 * to be called by the JSP translator, with a TagInfo object that
 * corresponds to the tag being translated. The call should happen before
 * any invocation on validate() and before any invocation on
 * getVariableInfo().
 *
 * <p>
 * <tt>NOTE:</tt> It is a (translation time) error for a tag definition
 * in a TLD with one or more variable subelements to have an associated
 * TagExtraInfo implementation that returns a VariableInfo array with
 * one or more elements from a call to getVariableInfo().
 */

public abstract class TagExtraInfo {

    /**
     * Sole constructor. (For invocation by subclass constructors, 
     * typically implicit.)
     */
    public TagExtraInfo() {
    }
    
    /**
     * information on scripting variables defined by the tag associated with
     * this TagExtraInfo instance.
     * Request-time attributes are indicated as such in the TagData parameter.
     *
     * @param data The TagData instance.
     * @return An array of VariableInfo data, or null or a zero length array
     *         if no scripting variables are to be defined.
     */
    public VariableInfo[] getVariableInfo(TagData data) {
	return ZERO_VARIABLE_INFO;
    }

    /**
     * Translation-time validation of the attributes. 
     * Request-time attributes are indicated as such in the TagData parameter.
     * Note that the preferred way to do validation is with the validate()
     * method, since it can return more detailed information.
     *
     * @param data The TagData instance.
     * @return Whether this tag instance is valid.
     * @see TagExtraInfo#validate
     */

    public boolean isValid(TagData data) {
	return true;
    }

    /**
     * Translation-time validation of the attributes.
     * Request-time attributes are indicated as such in the TagData parameter.
     * Because of the higher quality validation messages possible, 
     * this is the preferred way to do validation (although isValid() 
     * still works).  
     * 
     * <p>JSP 2.0 and higher containers call validate() instead of isValid().
     * The default implementation of this method is to call isValid().  If 
     * isValid() returns false, a generic ValidationMessage[] is returned
     * indicating isValid() returned false.</p>
     *
     * @param data The TagData instance.
     * @return A null object, or zero length array if no errors, an 
     *     array of ValidationMessages otherwise.
     * @since 2.0
     */
    public ValidationMessage[] validate( TagData data ) {
	ValidationMessage[] result = null;

	if( !isValid( data ) ) {
	    result = new ValidationMessage[] {
		new ValidationMessage( data.getId(), "isValid() == false" ) };
	}

	return result;
    }

    /**
     * Set the TagInfo for this class.
     *
     * @param tagInfo The TagInfo this instance is extending
     */
    public final void setTagInfo(TagInfo tagInfo) {
	this.tagInfo = tagInfo;
    }

    /**
     * Get the TagInfo for this class.
     *
     * @return the taginfo instance this instance is extending
     */
    public final TagInfo getTagInfo() {
	return tagInfo;
    }
    
    // private data
    private TagInfo tagInfo;

    // zero length VariableInfo array
    private static final VariableInfo[] ZERO_VARIABLE_INFO = { };
}

