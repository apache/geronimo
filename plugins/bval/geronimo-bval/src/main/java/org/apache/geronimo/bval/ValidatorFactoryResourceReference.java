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


package org.apache.geronimo.bval;
 
import javax.naming.NamingException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;

import org.apache.geronimo.naming.reference.ResourceReference;

/**
 * @version $Rev$ $Date$
 */
public class ValidatorFactoryResourceReference extends ResourceReference<ValidationException> {

    public ValidatorFactoryResourceReference(String query, String type) {
        super(query, type);
    }
    
    @Override
    public Object getContent() throws NamingException {
        // get the associated reference and use that to request the validator 
        try {
            return (ValidatorFactory)super.getContent();
        } catch (ValidationException e) {
            // turn any creation errors into a NamingException 
            throw (NamingException)new NamingException("Could not create ValidatorFactory instance").initCause(e);
        }
    }
}


