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

package org.apache.geronimo.common.propertyeditor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A property editor for {@link java.net.URL}.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:26 $
 */
public class URLEditor extends TextPropertyEditorSupport {
    /**
     * Returns a URL for the input object converted to a string.
     *
     * @return a URL object
     *
     * @throws PropertyEditorException   An MalformedURLException occured.
     */
    public Object getValue() {
        try {
            String urlspec = getAsText().trim();
            URL url;
            try {
                url = new URL(urlspec);
                if (url.getProtocol().equals("file")) {
                    url = createFromFilespec(url.getFile());
                }
            } catch (Exception e) {
                url = createFromFilespec(urlspec);
            }

            return url;

        } catch (MalformedURLException e) {
            throw new PropertyEditorException(e);
        }
    }

    private static URL createFromFilespec(final String filespec) throws MalformedURLException {
        return new File(filespec).toURI().toURL();
    }
}
