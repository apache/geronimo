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
package org.apache.geronimo.validator;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class ValidationContext {
    
    protected Vector failures = new Vector();
    protected Vector warnings = new Vector();
    protected Vector errors   = new Vector();

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
        warnings.addElement( warning );
    }
    
    public void addFailure(ValidationFailure failure) {
        failures.addElement( failure );
    }

    public void addError(ValidationError error) {
        errors.addElement( error );
    }

    public ValidationFailure[] getFailures() {
        ValidationFailure[] tmp = new ValidationFailure[failures.size()];
        failures.copyInto( tmp );
        return tmp;
    }
    
    public ValidationWarning[] getWarnings() {
        ValidationWarning[] tmp = new ValidationWarning[warnings.size()];
        warnings.copyInto( tmp );
        return tmp;
    }
    
    public ValidationError[] getErrors() {
        ValidationError[] tmp = new ValidationError[errors.size()];
        errors.copyInto( tmp );
        return tmp;
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
