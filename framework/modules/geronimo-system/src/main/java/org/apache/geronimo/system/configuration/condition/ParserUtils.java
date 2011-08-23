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
package org.apache.geronimo.system.configuration.condition;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic parser utility functions.
 *
 * @version $Rev: 476049 $ $Date: 2006-11-16 23:35:17 -0500 (Thu, 16 Nov 2006) $
 */
public class ParserUtils
{
    public static void addDefaultVariables(Map<String, Object> vars) {
        vars.put("java", new JavaVariable());
        vars.put("os", new OsVariable());
        
        // Install properties (to allow getProperty(x,y) to be used for defaults
        Properties props = new Properties();
        props.putAll(System.getProperties());
        vars.put("props", props);
    }
       
    public static class DebugHashMapContext extends MapContext {
        private static final Logger log = LoggerFactory.getLogger(DebugHashMapContext.class);
        
        public Object get(String str) {
            Object r = super.get(str);
            
            log.debug("Get property: {} {}", str, r);
            
            return r;
        }
    }
}
