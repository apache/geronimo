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
package javax.mail.internet;
// http://www.faqs.org/rfcs/rfc2183.html
/**
 * @version $Revision: 1.2 $ $Date: 2003/09/04 02:14:40 $
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
