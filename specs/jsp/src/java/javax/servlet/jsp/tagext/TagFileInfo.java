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
 * Tag information for a tag file in a Tag Library;
 * This class is instantiated from the Tag Library Descriptor file (TLD)
 * and is available only at translation time.
 *
 * @since 2.0
 */
public class TagFileInfo {

    /**
     * Constructor for TagFileInfo from data in the JSP 2.0 format for TLD.
     * This class is to be instantiated only from the TagLibrary code
     * under request from some JSP code that is parsing a
     * TLD (Tag Library Descriptor).
     *
     * Note that, since TagLibibraryInfo reflects both TLD information
     * and taglib directive information, a TagFileInfo instance is
     * dependent on a taglib directive.  This is probably a
     * design error, which may be fixed in the future.
     *
     * @param name The unique action name of this tag
     * @param path Where to find the .tag file implementing this 
     *     action, relative to the location of the TLD file.
     * @param tagInfo The detailed information about this tag, as parsed
     *     from the directives in the tag file.
     */
    public TagFileInfo( String name, String path, TagInfo tagInfo ) {
        this.name = name;
        this.path = path;
        this.tagInfo = tagInfo;
    }

    /**
     * The unique action name of this tag.
     *
     * @return The (short) name of the tag.
     */
    public String getName() {
        return name;
    }

    /**
     * Where to find the .tag file implementing this action.
     *
     * @return The path of the tag file, relative to the TLD, or "." if 
     *     the tag file was defined in an implicit tag file.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns information about this tag, parsed from the directives 
     * in the tag file.
     *
     * @return a TagInfo object containing information about this tag
     */
    public TagInfo getTagInfo() {
        return tagInfo;
    }

    // private fields for 2.0 info
    private String name;
    private String path;
    private TagInfo tagInfo;
}
