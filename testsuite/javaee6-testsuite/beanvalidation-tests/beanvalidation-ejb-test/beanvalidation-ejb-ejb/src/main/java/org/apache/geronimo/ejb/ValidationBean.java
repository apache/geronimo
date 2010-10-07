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
package org.apache.geronimo.ejb;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
@Stateless
public class ValidationBean implements ValidationRemote {
    
    @Resource ValidatorFactory injectedValidatorFactory;
    @Resource Validator injectedValidator;

    /**
     * Default constructor. 
     */
    public ValidationBean() {
    }

    @Override
    public String validatorInfo() {
        String result = "";
        
        result += "injectedValidatorFactory="+injectedValidatorFactory+"<br>\n";
        result += "injectedValidator="+injectedValidator+"<br>\n";

        InitialContext ic = null;
        try {
            ic = new InitialContext();
        } catch (NamingException e) {
            result += e.toString()+"<br>\n";
        }
        
        if(ic == null) {
            return result;
        }
        
        try {
            result += "JNDIValidatorFactory="+ic.lookup("java:comp/ValidatorFactory")+"<br>\n";
        } catch (NamingException e) {
            result += e.toString()+"<br>\n";
        }

        try {
            result += "JNDIValidator="+ic.lookup("java:comp/Validator")+"<br>\n";        
        } catch (NamingException e) {
            result += e.toString()+"<br>\n";
        }

        return result;
    }
}
