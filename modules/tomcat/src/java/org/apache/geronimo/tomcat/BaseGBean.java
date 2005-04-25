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
package org.apache.geronimo.tomcat;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.modeler.util.IntrospectionUtils;

public abstract class BaseGBean {

    protected void setParameters(Object object, Map map){
        if (map != null){
            Set keySet = map.keySet();
            Iterator iterator = keySet.iterator();
            while(iterator.hasNext()){
                String name = (String)iterator.next();
                String value = (String)map.get(name);
                
                IntrospectionUtils.setProperty(object, name, value);                    
            }
        }
        
    }
}
