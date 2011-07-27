/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.geronimo.kernel.util.IOUtils;

public class Utils {
    
    public static void regressiveDelete(File file){
        if (file == null || !file.exists()) return;
        
        File parent = file.getParentFile();
                
        if (file.isFile() || (file.isDirectory() && file.listFiles().length ==0)) {
            file.delete();
            regressiveDelete(parent);
        }
        
    }
    
    public static void appendLine(File file, String line) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, true));
            writer.newLine();
            writer.append(line);
            writer.flush();
        } finally {
            IOUtils.close(writer);
        }
    }
    
    public static void deleteLineByKeyword(File file, String keyword) throws IOException{
        BufferedReader reader = null;
        BufferedWriter writer = null;

        File tmpFile = new File(file.getAbsolutePath()+".tmpfile");
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new BufferedWriter(new FileWriter(tmpFile));
            String line = null;
            boolean emptyLineFlag = false;
            while (( line = reader.readLine()) != null){
                if (line.startsWith("#")) { // comments line
                    writer.append(line);
                    writer.newLine();
                    continue;
                }
                
                if (line.contains(keyword)) {
                    continue;
                }
                                
                if (line.isEmpty()){
                    // this can help reduce multi empty lines to one
                    emptyLineFlag = true;
                    continue;
                }
                
                if (emptyLineFlag) {
                    writer.append("");
                    writer.newLine();
                    emptyLineFlag = false;
                }
                writer.append(line);
                writer.newLine();
            }
            
        } finally {
            IOUtils.close(reader);
            IOUtils.close(writer);
        }
        
        if (file.delete()){
            tmpFile.renameTo(file);
        }else{
            tmpFile.delete();
            throw new RuntimeException("Can not delete a line in file: " + file.getAbsolutePath());
        }

    }
    
    public static String findLineByKeyword(File file, String keyword) throws IOException{
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while (( line = reader.readLine()) != null){
                if (line.startsWith("#")) continue;
                
                if (line.contains(keyword)) return line;
            }
            
        } finally {
            IOUtils.close(reader);
        }
        
        return null;
        
    }
    
   
}
