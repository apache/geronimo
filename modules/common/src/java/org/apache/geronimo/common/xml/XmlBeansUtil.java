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

package org.apache.geronimo.common.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class XmlBeansUtil {

    private XmlBeansUtil() {}

    public static XmlObject getXmlObject(URL url, SchemaType type) throws XmlException {
        InputStream is;
        try {
            is = url.openStream();
            try {
                return parse(is, type);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static XmlObject parse(InputStream is, SchemaType type) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        SchemaTypeLoader loader = XmlBeans.getContextTypeLoader();
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        XmlObject parsed = loader.parse(is, type, options);
        if (errors.size() != 0) {
            throw new XmlException(errors.toArray().toString());
        }
        return parsed;
    }

}
