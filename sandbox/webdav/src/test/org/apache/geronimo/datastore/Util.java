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

package org.apache.geronimo.datastore;

import java.io.File;
import java.io.IOException;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/29 13:14:11 $
 */
public class Util {
    
    public static void recursiveDelete(File aRoot) throws IOException {
        if ( !aRoot.isDirectory() ) {
            return;
        }
        File[] files = aRoot.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                recursiveDelete(file);
            } else {
                file.delete();
            }
        }
        aRoot.delete();
    }
    
}
