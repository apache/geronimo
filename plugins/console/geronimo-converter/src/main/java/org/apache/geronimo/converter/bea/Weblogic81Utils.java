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
package org.apache.geronimo.converter.bea;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Properties;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * Reads information out of the WebLogic domain directory.
 * Needs access to the WebLogic JARs in the weblogic81/server/lib directory.
 *
 * @version $Rev$ $Date$
 */
public class Weblogic81Utils {
    private final static Pattern ENCRYPTED_STRING = Pattern.compile("\\\"\\{\\S+\\}\\S+?\\\"");
    private Object decoder;
    private Method decode;
    private Object decrypter;
    private Method decrypt;
    private File domainDir;

    public Weblogic81Utils(String libDirPath, String domainDirPath) {
        File libDir = new File(libDirPath);
        if(!libDir.exists() || !libDir.canRead() || !libDir.isDirectory()) throw new IllegalArgumentException("Bad weblogic lib dir");
        File weblogicJar = new File(libDir, "weblogic.jar");
        File securityJar = new File(libDir, "jsafeFIPS.jar");
        if(!weblogicJar.canRead()) throw new IllegalArgumentException("Cannot find JARs in provided lib dir");
        domainDir = new File(domainDirPath);
        if(!domainDir.exists() || !domainDir.canRead() || !domainDir.isDirectory()) throw new IllegalArgumentException("Bad domain directory");
        File state = new File(domainDir, "SerializedSystemIni.dat");
        if(!state.canRead()) throw new IllegalArgumentException("Cannot find serialized state in domain directory");
        try {
            ClassLoader loader = new URLClassLoader(securityJar.exists() ? new URL[]{weblogicJar.toURI().toURL(), securityJar.toURI().toURL()} : new URL[]{weblogicJar.toURI().toURL()}, Weblogic81Utils.class.getClassLoader());
            initialize(loader, state);
        } catch (Exception e) {
            throw (RuntimeException)new IllegalArgumentException("Unable to initialize encryption routines from provided arguments").initCause(e);
        }
    }

    public Properties getBootProperties() {
        File boot = new File(domainDir, "boot.properties");
        FileInputStream bootIn = null;
        try {
            bootIn = new FileInputStream(boot);
        } catch (FileNotFoundException e) {
            return null;
        }
        try {
            Properties props = new Properties();
            props.load(bootIn);
            bootIn.close();
            for (Iterator it = props.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = props.getProperty(key);
                if(value != null && value.startsWith("{")) props.setProperty(key, decryptString(value));
            }
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    public String getConfigXML() throws FileNotFoundException {
        File config = new File(domainDir, "config.xml");
        BufferedReader in = new BufferedReader(new FileReader(config));
        StringWriter string = new StringWriter();
        PrintWriter out = new PrintWriter(string);
        String line;
        Matcher m = ENCRYPTED_STRING.matcher("");
        try {
            while((line = in.readLine()) != null) {
                m.reset(line);
                int last = -1;
                while(m.find()) {
                    out.print(line.substring(last+1, m.start()));
                    String s = line.substring(m.start(), m.end());
                    out.print("\"");
                    out.print(decryptString(s.substring(1, s.length()-1)));
                    out.print("\"");
                    last = m.end()-1;
                }
                if(last == -1) {
                    out.println(line);
                } else {
                    if(line.length() > last+1) {
                        out.print(line.substring(last+1));
                    }
                    out.println();
                }
                out.flush();
            }
            in.close();
            out.close();
        } catch (Exception e) {
            return null;
        }
        return string.getBuffer().toString();
    }

    private void initialize(ClassLoader loader, File state) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        byte[] salt = null, key = null;
        FileInputStream in = new FileInputStream(state);
        salt = readBytes(in);
        int i = in.read();
        if(i != -1) {
            if(i != 1) throw new IllegalStateException();
            key = readBytes(in);
        }
        in.close();
        decrypter = getEncryptionService(loader, salt, key);
        decoder = loader.loadClass("weblogic.utils.encoders.BASE64Decoder").newInstance();
        decode = decoder.getClass().getMethod("decodeBuffer", new Class[]{String.class});
        decrypt = decrypter.getClass().getMethod("decryptString", new Class[]{byte[].class});
    }

    private static byte[] readBytes(InputStream in) throws IOException {
        int len = in.read();
        if(len < 0)
            throw new IOException("stream is empty");
        byte result[] = new byte[len];
        int index = 0;
        while(true) {
            if(index >= len) {
                break;
            }
            int count = in.read(result, index, len - index);
            if(count == -1)
                break;
            index += count;
        }
        return result;
    }

    private String decryptString(String string) throws IllegalAccessException, InvocationTargetException {
        if(string.indexOf('}') > -1) {
            string = string.substring(string.indexOf("}")+1);
        }
        return (String) decrypt.invoke(decrypter, new Object[]{decode.invoke(decoder, new Object[]{string})});
    }

    static Object getEncryptionService(ClassLoader loader, byte salt[], byte key[]) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        String magic = "0xccb97558940b82637c8bec3c770f86fa3a391a56";
        Object factory = loader.loadClass("weblogic.security.internal.encryption.JSafeEncryptionServiceImpl").getMethod("getFactory", new Class[0]).invoke(null, null);
        Method getter = factory.getClass().getMethod("getEncryptionService", new Class[]{byte[].class, String.class, byte[].class});
        return getter.invoke(factory, new Object[]{salt, magic, key});
    }
}
