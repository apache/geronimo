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
package org.apache.geronimo.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidationContext {
    
    protected ArrayList failures = new ArrayList();
    protected ArrayList warnings = new ArrayList();
    protected ArrayList errors   = new ArrayList();

    protected Map attributes = new HashMap();

    protected String jarPath;
    
    public ValidationContext(String name){
        this.jarPath = name;
    }

    public Set entrySet() {
        return attributes.entrySet();
    }

    public Object remove(Object key) {
        return attributes.remove(key);
    }

    public Object put(Object key, Object value) {
        return attributes.put(key, value);
    }

    public Object get(Object key) {
        return attributes.get(key);
    }

    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }

    public void addWarning( ValidationWarning warning ) {
        warnings.add( warning );
    }
    
    public void addFailure(ValidationFailure failure) {
        failures.add( failure );
    }

    public void addError(ValidationError error) {
        errors.add( error );
    }

    public ValidationFailure[] getFailures() {
        return (ValidationFailure[])failures.toArray(new ValidationFailure[0]);
    }
    
    public ValidationWarning[] getWarnings() {
        return (ValidationWarning[])failures.toArray(new ValidationWarning[0]);
    }
    
    public ValidationError[] getErrors() {
        return (ValidationError[])failures.toArray(new ValidationError[0]);
    }

    public boolean hasWarnings(){
        return warnings.size() > 0;
    }

    public boolean hasFailures(){
        return failures.size() > 0;
    }
    
    public boolean hasErrors(){
        return errors.size() > 0;
    }
}
