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
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Collection;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/08/04 07:21:54 $
 */
public class FileUtil {

    private static int i;

    public static File toTempFile(InputStream is) throws IOException {
        File tmp = File.createTempFile("geronimodeployment" + i++, "tmp");
        FileOutputStream fos = new FileOutputStream(tmp);
        byte[] buffer = new byte[4096];
        int count;
        while ((count = is.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        fos.flush();
        fos.close();
        return tmp;
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

    public static void listRecursiveFiles(File aFile, Collection aColl) {
        File[] files = aFile.listFiles();
        if ( null == files ) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if ( files[i].isFile() ) {
                aColl.add(files[i]);
            } else {
                listRecursiveFiles(files[i], aColl);
            }
        }
    }
}
