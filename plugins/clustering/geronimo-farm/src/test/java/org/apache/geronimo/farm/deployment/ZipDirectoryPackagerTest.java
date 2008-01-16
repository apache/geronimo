/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.farm.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.geronimo.testsupport.TestSupport;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ZipDirectoryPackagerTest extends TestSupport {

    public void testPackAndUnpack() throws Exception {
        ZipDirectoryPackager packager = new ZipDirectoryPackager();
        File packedDir = packager.pack(new File(BASEDIR, "src/test/resources/folderToZip"));
        
        File unpackedDir = packager.unpack(packedDir);
        
        assertFile(unpackedDir, "folder1/file1");
        assertFile(unpackedDir, "folder1/file2");
        assertFile(unpackedDir, "folder2/file1");
    }

    private void assertFile(File unpackedDir, String fileName) throws FileNotFoundException, IOException {
        File file = new File(unpackedDir, fileName);
        assertTrue(file.exists());
        
        InputStream in = new FileInputStream(file);
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(in));
        String line = reader2.readLine();
        assertEquals(fileName, line);
        in.close();
    }
    
}
