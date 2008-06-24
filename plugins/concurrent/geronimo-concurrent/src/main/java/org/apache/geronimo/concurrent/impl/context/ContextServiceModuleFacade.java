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
package org.apache.geronimo.concurrent.impl.context;

import java.util.Map;

import javax.util.concurrent.ContextService;

import org.apache.geronimo.concurrent.impl.ModuleContext;
import org.apache.geronimo.gbean.AbstractName;

public class ContextServiceModuleFacade implements ContextService {
    
    private ContextService contextService;
    private AbstractName moduleID;

    public ContextServiceModuleFacade(ContextService contextService,
                                      AbstractName moduleID) {
        this.contextService = contextService;
        this.moduleID = moduleID;
    }
    
    protected Object before() {
        return ModuleContext.setCurrentModule(this.moduleID);
    }
    
    protected void after(Object obj) {
        ModuleContext.setCurrentModule((AbstractName)obj);
    }
           
    public Object createContextObject(Object arg0, Class<?>[] arg1) {
        Object rs = before();
        try {
            return this.contextService.createContextObject(arg0, arg1);
        } finally {
            after(rs);
        } 
    }

    public Object createContextObject(Object arg0, Class<?>[] arg1, Map<String, String> arg2) {
        Object rs = before();
        try {
            return this.contextService.createContextObject(arg0, arg1, arg2);
        } finally {
            after(rs);
        } 
    }

    public Map<String, String> getProperties(Object arg0) {
        return this.contextService.getProperties(arg0);
    }

    public void setProperties(Object arg0, Map<String, String> arg1) {
        this.contextService.setProperties(arg0, arg1);
    }
           
}
