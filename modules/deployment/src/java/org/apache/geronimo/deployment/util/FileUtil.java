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

package org.apache.geronimo.deployment.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.net.URL;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class FileUtil {
    private static int i;

    public static File createTempFile() throws IOException {
        File tempFile = File.createTempFile("geronimodeployment" + i++, "tmp");
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static File toTempFile(URL url) throws IOException {
        InputStream is = null;
        try {
            is = url.openStream();
            return toTempFile(is, false);
        } finally {
            IOUtil.close(is);
        }
    }

    public static File toTempFile(InputStream is) throws IOException {
        return toTempFile(is, false);
    }

    public static File toTempFile(InputStream in, boolean close) throws IOException {
        OutputStream out = null;
        try {
            File tempFile = createTempFile();
            out = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }

            out.flush();
            return tempFile;
        } finally {
            if (close) {
                in.close();
            }
            out.close();
        }
    }

    public static void recursiveDelete(File root) {
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        recursiveDelete(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        root.delete();
    }

    public static Collection listRecursiveFiles(File file) {
        LinkedList list = new LinkedList();
        listRecursiveFiles(file, list);
        return Collections.unmodifiableCollection(list);
    }

    public static void listRecursiveFiles(File file, Collection collection) {
        File[] files = file.listFiles();
        if ( null == files ) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                listRecursiveFiles(files[i], collection);
            } else {
                collection.add(files[i]);
            }
        }
    }
}
