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

package org.apache.geronimo.deployment.cli;

import java.io.*;

import org.apache.geronimo.common.DeploymentException;

/**
 *
 *
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DeployUtils {
    public static String reformat(String source, int indent, int endCol) {
        StringBuffer buf = new StringBuffer((int)(source.length()*1.1));
        String prefix = indent == 0 ? "" : buildIndent(indent);
        try {
            BufferedReader in = new BufferedReader(new StringReader(source));
            String line;
            int pos;
            while((line = in.readLine()) != null) {
                if(buf.length() > 0) {
                    buf.append('\n');
                }
                while(line.length() > 0) {
                    line = prefix + line;
                    if(line.length() > endCol) {
                        pos = line.lastIndexOf(' ', endCol);
                        if(pos < indent) {
                            pos = line.indexOf(' ', endCol);
                            if(pos < indent) {
                                pos = line.length();
                            }
                        }
                        buf.append(line.substring(0, pos)).append('\n');
                        if(pos < line.length()-1) {
                            line = line.substring(pos+1);
                        } else {
                            break;
                        }
                    } else {
                        buf.append(line).append("\n");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new AssertionError("This should be impossible");
        }
        return buf.toString();
    }

    private static String buildIndent(int indent) {
        StringBuffer buf = new StringBuffer(indent);
        for(int i=0; i<indent; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }

    public static boolean isJarFile(File file) throws DeploymentException {
        if(file.isDirectory()) {
            return false;
        }
        if(!file.canRead()) {
            throw new DeploymentException("Cannot read file "+file.getAbsolutePath());
        }
        if(file.length() < 4) {
            return false;
        }
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            int test = in.readInt();
            in.close();
            return test == 0x504b0304;
        } catch(IOException e) {
            throw new DeploymentException("Cannot read from file "+file.getAbsolutePath(), e);
        }
    }

    public static void main(String[] args) {
        String msg = "/home/ammulder/cvs/geronimo/modules/security/target/geronimo-security-1.0-SNAPSHOT.jar does not specify a J2EE-DeploymentFactory-Implementation-Class; cannot load driver.";
        System.out.println(reformat("Error: "+msg, 4, 72));
    }
}
