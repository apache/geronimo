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

package org.apache.geronimo.deployment.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.net.URL;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/27 07:35:03 $
 *
 * */
public class XmlBeansUtil {

    private XmlBeansUtil() {}

    public static XmlObject getPlan(URL planURL, SchemaType type) {
        InputStream is;
        try {
            is = planURL.openStream();
            try {
                return parse(is, type);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            return null;
        } catch (XmlException e) {
            return null;
        }
    }

    public static XmlObject parse(InputStream is, SchemaType type) throws IOException, XmlException {
        ArrayList errors = new ArrayList();
        SchemaTypeLoader loader = XmlBeans.getContextTypeLoader();
        XmlOptions options = new XmlOptions();
        options.setLoadLineNumbers();
        options.setErrorListener(errors);
        return loader.parse(is, type, options);
    }

}
