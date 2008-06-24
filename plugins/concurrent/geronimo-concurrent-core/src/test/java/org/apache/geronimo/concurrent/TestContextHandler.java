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

import java.util.Map;

public class TestContextHandler implements ManagedContextHandler {

    public static final String SKIP = 
        TestContextHandler.class.getName() + ".skip";
    
    private final static String OLD_OBJECT = 
        TestContextHandler.class.getName() + ".oldObject";

    private final static String NEW_OBJECT = 
        TestContextHandler.class.getName() + ".newObject";

    private static ThreadLocal context = new ThreadLocal();

    public TestContextHandler() {
    }

    public void saveContext(Map<String, Object> context) {
        context.put(NEW_OBJECT, getCurrentObject());
    }

    public void setContext(Map<String, Object> threadContext) {
        threadContext.put(OLD_OBJECT, getCurrentObject());
                
        if (Boolean.valueOf((String)threadContext.get(SKIP)).booleanValue()) {
            setCurrentObject("skipped");
        } else {        
            setCurrentObject(threadContext.get(NEW_OBJECT));
        }
    }

    public void unsetContext(Map<String, Object> threadContext) {
        setCurrentObject(threadContext.get(OLD_OBJECT));
    }

    public static Object getCurrentObject() {
        return context.get();
    }

    public static void setCurrentObject(Object obj) {
        context.set(obj);
    }

}
