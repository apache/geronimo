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

package javax.mail.internet;
// http://www.faqs.org/rfcs/rfc2183.html
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:28 $
 */
public class ContentDisposition {
    private String _disposition;
    private ParameterList _list;
    public ContentDisposition() {
        setDisposition(null);
        setParameterList(null);
    }
    public ContentDisposition(String disposition) throws ParseException {
        ParameterList list = null;
        int semicolon;
        if (disposition != null && (semicolon = disposition.indexOf(";")) != -1) {
            list = new ParameterList(disposition.substring(semicolon + 1));
            disposition = disposition.substring(0, semicolon);
        }
        setDisposition(disposition);
        setParameterList(list);
    }
    public ContentDisposition(String disposition, ParameterList list) {
        setDisposition(disposition);
        setParameterList(list);
    }
    public String getDisposition() {
        return _disposition;
    }
    public String getParameter(String name) {
        if (_list == null) {
            return null;
        } else {
            return _list.get(name);
        }
    }
    public ParameterList getParameterList() {
        return _list;
    }
    public void setDisposition(String string) {
        _disposition = string;
    }
    public void setParameter(String name, String value) {
        _list = new ParameterList();
        _list.set(name, value);
    }
    public void setParameterList(ParameterList list) {
        if (list == null) {
            _list = new ParameterList();
        } else {
            _list = list;
        }
    }
    public String toString() {
        if (_disposition == null && _list.size() == 0) {
            return null;
        }
        return (_disposition == null ? "" : _disposition)
            + (_list.size() == 0 ? "" : _list.toString());
    }
}
