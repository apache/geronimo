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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.activation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @version $Revision: 1.2 $ $Date: 2003/12/07 16:11:44 $
 */
public class MimeType implements Externalizable {
    private final static String TYPE_SEPARATOR = "/";
    private final static String PARAMETER_SEPARATOR = ";";
    private final static String STAR_SUB_TYPE = "*";

    private String primaryType = "text";
    private String subType = "plain";
    private MimeTypeParameterList parameterList = new MimeTypeParameterList();;

    public MimeType() {
    }

    public MimeType(String rawdata) throws MimeTypeParseException {
        parseMimeType(rawdata);
    }

    public MimeType(String primary, String sub) throws MimeTypeParseException {
        setPrimaryType(primary);
        setSubType(sub);
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(String primary) throws MimeTypeParseException {
        primaryType = parseToken(primary);
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String sub) throws MimeTypeParseException {
        subType = parseToken(sub);
    }

    public MimeTypeParameterList getParameters() {
        return parameterList;
    }

    public String getParameter(String name) {
        return parameterList.get(name);
    }

    public void setParameter(String name, String value) {
        parameterList.set(name, value);
    }

    public void removeParameter(String name) {
        parameterList.remove(name);
    }

    public String toString() {
        return getBaseType() +
                (parameterList == null
                ? ""
                : PARAMETER_SEPARATOR + parameterList.toString());
    }

    public String getBaseType() {
        return getPrimaryType() + TYPE_SEPARATOR + getSubType();
    }

    public boolean match(MimeType type) {
        return (
                getPrimaryType().equals(type.getPrimaryType())
                && (getSubType().equals(STAR_SUB_TYPE)
                || type.getSubType().equals(STAR_SUB_TYPE)
                || getSubType().equals(type.getSubType())));
    }

    public boolean match(String rawdata) throws MimeTypeParseException {
        return match(new MimeType(rawdata));
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(toString());
        out.flush();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            parseMimeType(in.readUTF());
        } catch (MimeTypeParseException mtpex) {
            throw new IOException(mtpex.getMessage());
        }
    }

    private void parseMimeType(String rawData) throws MimeTypeParseException {
        int typeSeparatorPos = rawData.indexOf(TYPE_SEPARATOR);
        int parameterSeparatorPos = rawData.indexOf(PARAMETER_SEPARATOR);

        if (typeSeparatorPos < 0) {
            throw new MimeTypeParseException("Unable to find subtype");
        }

        setPrimaryType(rawData.substring(0, typeSeparatorPos));
        if (parameterSeparatorPos < 0) {
            setSubType(rawData.substring(typeSeparatorPos + 1));
        } else {
            setSubType(rawData.substring(typeSeparatorPos + 1, parameterSeparatorPos));
            parameterList = new MimeTypeParameterList(rawData.substring(parameterSeparatorPos + 1));
        }
    }

    private static String parseToken(String tokenString) {
        // TODO it seems to have unauthorized chars
        return tokenString;
    }

}
