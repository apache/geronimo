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
package org.apache.geronimo.cxf;

import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;

public class GeronimoJaxWsImplementorInfo extends JaxWsImplementorInfo {

    private String bindingURI;

    public GeronimoJaxWsImplementorInfo(Class clazz, String bindingURI) {
        super(clazz);
        this.bindingURI = bindingURI;
    }
    
    public String getBindingType() {
        if (this.bindingURI != null) {
            return this.bindingURI;
        } else {
            return super.getBindingType();
        }
    }
   
}
