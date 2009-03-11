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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ZipDirectoryPackager implements DirectoryPackager {

    public File pack(File configurationDir) throws IOException {
        File zippedDir = File.createTempFile(configurationDir.getName(), ".zip");

        OutputStream out = new FileOutputStream(zippedDir);
        out = new BufferedOutputStream(out);
        ZipOutputStream zos = new ZipOutputStream(out);
        zip(zos, configurationDir, configurationDir);
        zos.close();

        return zippedDir;
    }

    public File unpack(File packedConfigurationDir) throws IOException {
        String tmpDirAsString = System.getProperty("java.io.tmpdir");
        File targetDir = new File(new File(tmpDirAsString), packedConfigurationDir.getName() + "_unpack");
        unpack(targetDir, packedConfigurationDir);
        return targetDir;
    }
     
    public void unpack(File targetDir, File packedConfigurationDir) throws IOException {
        ZipFile zipFile = new ZipFile(packedConfigurationDir);
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            File targetFile = new File(targetDir, zipEntry.getName());

            if (zipEntry.isDirectory()) {
                targetFile.mkdirs();
            } else {
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                OutputStream out = new FileOutputStream(targetFile);
                out = new BufferedOutputStream(out);
                InputStream in = zipFile.getInputStream(zipEntry);

                byte[] buffer = new byte[1024];
                int read;
                while (-1 != (read = in.read(buffer))) {
                    out.write(buffer, 0, read);
                }
                
                in.close();
                out.close();
            }
        }
        zipFile.close();
    }
    
    protected void zip(ZipOutputStream zos, File configurationDir, File nestedFile) throws IOException {
        if (nestedFile.isDirectory()) {
            File[] nestedFiles = nestedFile.listFiles();
            for (int i = 0; i < nestedFiles.length; i++) {
                zip(zos, configurationDir, nestedFiles[i]);
            }
        } else {
            String nestedFilePath = nestedFile.getAbsolutePath();
            String zipEntryName = nestedFilePath.substring(configurationDir.getAbsolutePath().length() + 1, nestedFilePath.length());
            ZipEntry zipEntry = new ZipEntry(normalizePathOfEntry(zipEntryName));
            zos.putNextEntry(zipEntry);
            
            InputStream in = new FileInputStream(nestedFile);
            in = new BufferedInputStream(in);
            
            byte[] buffer = new byte[1024];
            int read;
            while (-1 != (read = in.read(buffer))) {
                zos.write(buffer, 0, read);
            }

            in.close();
            zos.closeEntry();
        }
    }
    
    private String normalizePathOfEntry(String entryName){
        return entryName.replace('\\', '/');
    }

}
