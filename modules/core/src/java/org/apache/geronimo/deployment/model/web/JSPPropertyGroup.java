/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 * ====================================================================
 */
package org.apache.geronimo.deployment.model.web;

import org.apache.geronimo.deployment.model.j2ee.Displayable;

/**
 * JavaBean for the web.xml tag jsp-property-group
 *
 * @version $Revision: 1.1 $
 */
public class JSPPropertyGroup extends Displayable {
    private String urlPattern;
    private Boolean elIgnored;
    private String pageEncoding;
    private Boolean scriptingInvalid;
    private Boolean isXml;
    private String[] includePrelude = new String[0];
    private String[] includeCoda = new String[0];

    public Boolean getELIgnored() {
        return elIgnored;
    }

    public void setELIgnored(Boolean elIgnored) {
        this.elIgnored = elIgnored;
    }

    public String[] getIncludeCoda() {
        return includeCoda;
    }

    public String getIncludeCoda(int i) {
        return includeCoda[i];
    }

    public void setIncludeCoda(String[] includeCoda) {
        this.includeCoda = includeCoda;
    }

    public void setIncludeCoda(int i, String includeCoda) {
        this.includeCoda[i] = includeCoda;
    }

    public String[] getIncludePrelude() {
        return includePrelude;
    }

    public String getIncludePrelude(int i) {
        return includePrelude[i];
    }

    public void setIncludePrelude(String[] includePrelude) {
        this.includePrelude = includePrelude;
    }

    public void setIncludePrelude(int i, String includePrelude) {
        this.includePrelude[i] = includePrelude;
    }

    public Boolean getXML() {
        return isXml;
    }

    public void setXML(Boolean xml) {
        isXml = xml;
    }

    public String getPageEncoding() {
        return pageEncoding;
    }

    public void setPageEncoding(String pageEncoding) {
        this.pageEncoding = pageEncoding;
    }

    public Boolean getScriptingInvalid() {
        return scriptingInvalid;
    }

    public void setScriptingInvalid(Boolean scriptingInvalid) {
        this.scriptingInvalid = scriptingInvalid;
    }

    public String getURLPattern() {
        return urlPattern;
    }

    public void setURLPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }
}
