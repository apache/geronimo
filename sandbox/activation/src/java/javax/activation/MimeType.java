/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.activation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * @version $Rev$ $Date$
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
