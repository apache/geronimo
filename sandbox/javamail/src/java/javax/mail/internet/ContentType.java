/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package javax.mail.internet;
// can be in the form major/minor; charset=jobby
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public class ContentType {
    private ParameterList _list;
    private String _minor;
    private String _major;
    public ContentType() {
        this("text", "plain", new ParameterList());
    }
    public ContentType(String major, String minor, ParameterList list) {
        _major = major;
        _minor = minor;
        _list = list;
    }
    public ContentType(String type) throws ParseException {
        final int slash = type.indexOf("/");
        final int semi = type.indexOf(";");
        try {
            _major = type.substring(0, slash);
            if (semi == -1) {
                _minor = type.substring(slash + 1);
            } else {
                _minor = type.substring(slash + 1, semi);
                _list = new ParameterList(type.substring(semi + 1));
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParseException("Type invalid: " + type);
        }
    }
    public String getPrimaryType() {
        return _major;
    }
    public String getSubType() {
        return _minor;
    }
    public String getBaseType() {
        return _major + "/" + _minor;
    }
    public String getParameter(String name) {
        return (_list == null ? null : _list.get(name));
    }
    public ParameterList getParameterList() {
        return _list;
    }
    public void setPrimaryType(String major) {
        _major = major;
    }
    public void setSubType(String minor) {
        _minor = minor;
    }
    public void setParameter(String name, String value) {
        if (_list == null) {
            _list = new ParameterList();
        }
        _list.set(name, value);
    }
    public void setParameterList(ParameterList list) {
        _list = list;
    }
    public String toString() {
        return getBaseType() + (_list == null ? "" : ";" + _list.toString());
    }
    public boolean match(ContentType other) {
        return _major.equals(other._major)
            && (_minor.equals(other._minor)
                || _minor.equals("*")
                || other._minor.equals("*"));
    }
    public boolean match(String contentType) {
        try {
            return match(new ContentType(contentType));
        } catch (ParseException e) {
            return false;
        }
    }
}
