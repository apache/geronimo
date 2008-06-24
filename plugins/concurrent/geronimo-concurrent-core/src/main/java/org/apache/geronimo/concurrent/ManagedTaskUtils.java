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
package org.apache.geronimo.concurrent;

import java.util.Locale;

import javax.util.concurrent.Identifiable;

public class ManagedTaskUtils {

    public static String getTaskDescription(Object task, String localeStr) {
        if (task == null) {
            return null;
        } else if (task instanceof Identifiable) {
            Locale locale = parseLocale(localeStr);                        
            return ((Identifiable)task).getIdentityDescription(locale);                    
        } else {
            return task.toString();
        }
    }
    
    public static String getTaskName(Object task) {
        if (task == null) {
            return null;
        } else if (task instanceof Identifiable) {
            return ((Identifiable)task).getIdentityName();                 
        } else {
            return task.toString();
        }
    }
    
    public static Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid locale string:" + localeStr);
        }
        
        Locale locale = null;
        
        String[] tokens = localeStr.split("_");
        if (tokens.length == 1) { 
            locale = new Locale(tokens[0]);
        } else if (tokens.length == 2) {
            locale = new Locale(tokens[0], tokens[1]);
        } else if (tokens.length == 3) {
            locale = new Locale(tokens[0], tokens[1], tokens[2]);
        } else {
            throw new IllegalArgumentException("Invalid locale string:" + localeStr);
        }
        
        return locale;
    }
    
}
