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

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagFileInfo;

/**
 * Translation-time information associated with a taglib directive, and its
 * underlying TLD file.
 *
 * Most of the information is directly from the TLD, except for
 * the prefix and the uri values used in the taglib directive
 *
 *
 */

abstract public class TagLibraryInfo {

    /**
     * Constructor.
     *
     * This will invoke the constructors for TagInfo, and TagAttributeInfo
     * after parsing the TLD file.
     *
     * @param prefix the prefix actually used by the taglib directive
     * @param uri the URI actually used by the taglib directive
     */
    protected TagLibraryInfo(String prefix, String uri) {
	this.prefix = prefix;
	this.uri    = uri;
    }

    // ==== methods accessing taglib information =======

    /**
     * The value of the uri attribute from the taglib directive for 
     * this library.
     *
     * @return the value of the uri attribute
     */
   
    public String getURI() {
        return uri;
    }

    /**
     * The prefix assigned to this taglib from the taglib directive
     *
     * @return the prefix assigned to this taglib from the taglib directive
     */

    public String getPrefixString() {
	return prefix;
    }

    // ==== methods using the TLD data =======

    /**
     * The preferred short name (prefix) as indicated in the TLD.
     * This may be used by authoring tools as the preferred prefix
     * to use when creating an taglib directive for this library.
     *
     * @return the preferred short name for the library
     */
    public String getShortName() {
        return shortname;
    }

    /**
     * The "reliable" URN indicated in the TLD (the uri element).
     * This may be used by authoring tools as a global identifier
     * to use when creating a taglib directive for this library.
     *
     * @return a reliable URN to a TLD like this
     */
    public String getReliableURN() {
        return urn;
    }


    /**
     * Information (documentation) for this TLD.
     *
     * @return the info string for this tag lib
     */
   
    public String getInfoString() {
        return info;
    }


    /**
     * A string describing the required version of the JSP container.
     * 
     * @return the (minimal) required version of the JSP container.
     * @see javax.servlet.jsp.JspEngineInfo
     */
   
    public String getRequiredVersion() {
        return jspversion;
    }


    /**
     * An array describing the tags that are defined in this tag library.
     *
     * @return the TagInfo objects corresponding to the tags defined by this
     *         tag library, or a zero length array if this tag library
     *         defines no tags
     */
    public TagInfo[] getTags() {
        return tags;
    }

    /**
     * An array describing the tag files that are defined in this tag library.
     *
     * @return the TagFileInfo objects corresponding to the tag files defined
     *         by this tag library, or a zero length array if this
     *         tag library defines no tags files
     * @since 2.0
     */
    public TagFileInfo[] getTagFiles() {
        return tagFiles;
    }


    /**
     * Get the TagInfo for a given tag name, looking through all the
     * tags in this tag library.
     *
     * @param shortname The short name (no prefix) of the tag
     * @return the TagInfo for the tag with the specified short name, or
     *         null if no such tag is found
     */

    public TagInfo getTag(String shortname) {
        TagInfo tags[] = getTags();

        if (tags == null || tags.length == 0) {
            return null;
        }

        for (int i=0; i < tags.length; i++) {
            if (tags[i].getTagName().equals(shortname)) {
                return tags[i];
            }
        }
        return null;
    }

    /**
     * Get the TagFileInfo for a given tag name, looking through all the
     * tag files in this tag library.
     *
     * @param shortname The short name (no prefix) of the tag
     * @return the TagFileInfo for the specified Tag file, or null
     *         if no Tag file is found
     * @since 2.0
     */
    public TagFileInfo getTagFile(String shortname) {
        TagFileInfo tagFiles[] = getTagFiles();

        if (tagFiles == null || tagFiles.length == 0) {
            return null;
        }

        for (int i=0; i < tagFiles.length; i++) {
            if (tagFiles[i].getName().equals(shortname)) {
                return tagFiles[i];
            }
        }
        return null;
    }

    /**
     * An array describing the functions that are defined in this tag library.
     *
     * @return the functions defined in this tag library, or a zero
     *         length array if the tag library defines no functions.
     * @since 2.0
     */
    public FunctionInfo[] getFunctions() {
        return functions;
    }


    /**
     * Get the FunctionInfo for a given function name, looking through all the
     * functions in this tag library.
     *
     * @param name The name (no prefix) of the function
     * @return the FunctionInfo for the function with the given name, or null
     *         if no such function exists
     * @since 2.0
     */
    public FunctionInfo getFunction(String name) {

        if (functions == null || functions.length == 0) {
            System.err.println("No functions");
            return null;
        }

        for (int i=0; i < functions.length; i++) {
            if (functions[i].getName().equals(name)) {
                return functions[i];
            }
        }
        return null;
    }


    // Protected fields

    /**
     * The prefix assigned to this taglib from the taglib directive.
     */
    protected String        prefix;
    
    /**
     * The value of the uri attribute from the taglib directive for 
     * this library.
     */
    protected String        uri;
    
    /**
     * An array describing the tags that are defined in this tag library.
     */
    protected TagInfo[]     tags;
    
    /**
     * An array describing the tag files that are defined in this tag library.
     *
     * @since 2.0
     */
    protected TagFileInfo[] tagFiles;
    
    /**
     * An array describing the functions that are defined in this tag library.
     *
     * @since 2.0
     */
    protected FunctionInfo[] functions;

    // Tag Library Data
    
    /**
     * The version of the tag library.
     */
    protected String tlibversion; // required
    
    /**
     * The version of the JSP specification this tag library is written to.
     */
    protected String jspversion;  // required
    
    /**
     * The preferred short name (prefix) as indicated in the TLD.
     */
    protected String shortname;   // required
    
    /**
     * The "reliable" URN indicated in the TLD.
     */
    protected String urn;         // required
    
    /**
     * Information (documentation) for this TLD.
     */
    protected String info;        // optional
}
