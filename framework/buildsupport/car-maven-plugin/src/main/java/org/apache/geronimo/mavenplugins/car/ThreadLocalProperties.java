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


package org.apache.geronimo.mavenplugins.car;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.Override;
import java.util.Collection;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev:$ $Date:$
 */
public class ThreadLocalProperties extends Properties {

    private static Properties defaultProperties;
    private static int count = 0;

    public static synchronized void install() {
        if (count ==0) {
            defaultProperties = System.getProperties();
            System.setProperties(new ThreadLocalProperties());
        }
        count++;
    }

    public static synchronized void uninstall() {
        count--;
        if (count ==0) {
            System.setProperties(defaultProperties);
        }
    }

    private final ThreadLocal<Properties> properties = new InheritableThreadLocal<Properties>() {
        @Override
        protected Properties initialValue() {
            return new Properties(defaultProperties);
        }
    };

    public Properties getProperties() {
        return properties.get();
    }

    public int size() {
        return getProperties().size();
    }

    public boolean isEmpty() {
        return getProperties().isEmpty();
    }

    public Enumeration<Object> keys() {
        return getProperties().keys();
    }

    public Enumeration<Object> elements() {
        return getProperties().elements();
    }

    public boolean contains(Object o) {
        return getProperties().contains(o);
    }

    public boolean containsValue(Object o) {
        return getProperties().containsValue(o);
    }

    public boolean containsKey(Object o) {
        return getProperties().containsKey(o);
    }

    public Object get(Object o) {
        return getProperties().get(o);
    }

    public Object put(Object o, Object o1) {
        return getProperties().put(o, o1);
    }

    public Object remove(Object o) {
        return getProperties().remove(o);
    }

    public void putAll(Map<? extends Object, ? extends Object> map) {
        getProperties().putAll(map);
    }

    public void clear() {
        getProperties().clear();
    }

    public Object clone() {
        return getProperties().clone();
    }

    public String toString() {
        return getProperties().toString();
    }

    public Set<Object> keySet() {
        return getProperties().keySet();
    }

    public Set<Map.Entry<Object, Object>> entrySet() {
        return getProperties().entrySet();
    }

    public Collection<Object> values() {
        return getProperties().values();
    }

    public boolean equals(Object o) {
        return getProperties().equals(o);
    }

    public int hashCode() {
        return getProperties().hashCode();
    }

    public Object setProperty(String s, String s1) {
        return getProperties().setProperty(s, s1);
    }

    public void load(Reader reader) throws IOException {
        getProperties().load(reader);
    }

    public void load(InputStream inputStream) throws IOException {
        getProperties().load(inputStream);
    }

    public void save(OutputStream outputStream, String s) {
        getProperties().save(outputStream, s);
    }

    public void store(Writer writer, String s) throws IOException {
        getProperties().store(writer, s);
    }

    public void store(OutputStream outputStream, String s) throws IOException {
        getProperties().store(outputStream, s);
    }

    public void loadFromXML(InputStream inputStream) throws IOException, InvalidPropertiesFormatException {
        getProperties().loadFromXML(inputStream);
    }

    public void storeToXML(OutputStream outputStream, String s) throws IOException {
        getProperties().storeToXML(outputStream, s);
    }

    public void storeToXML(OutputStream outputStream, String s, String s1) throws IOException {
        getProperties().storeToXML(outputStream, s, s1);
    }

    public String getProperty(String s) {
        return getProperties().getProperty(s);
    }

    public String getProperty(String s, String s1) {
        return getProperties().getProperty(s, s1);
    }

    public Enumeration<?> propertyNames() {
        return getProperties().propertyNames();
    }

    public Set<String> stringPropertyNames() {
        return getProperties().stringPropertyNames();
    }

    public void list(PrintStream printStream) {
        getProperties().list(printStream);
    }

    public void list(PrintWriter printWriter) {
        getProperties().list(printWriter);
    }
}
