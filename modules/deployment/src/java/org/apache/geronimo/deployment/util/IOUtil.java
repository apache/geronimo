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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @version $Revision$ $Date$
 */
public class IOUtil {
    public static void close(InputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(OutputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(Reader thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(Writer thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static String readAll(Reader thing) throws IOException {
        char[] buffer = new char[4000];
        StringBuffer out = new StringBuffer();

        for(int count = thing.read(buffer); count >= 0; count = thing.read(buffer)) {
            out.append(buffer, 0, count);
        }
        return out.toString();
    }

    public static String readAll(URL url) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(url.openStream());
            return IOUtil.readAll(reader);
        } finally {
            IOUtil.close(reader);
        }
    }
}
