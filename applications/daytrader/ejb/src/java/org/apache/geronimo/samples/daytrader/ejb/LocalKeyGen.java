/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
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

package org.apache.geronimo.samples.daytrader.ejb;

import javax.ejb.*;
import java.util.Collection;

public interface LocalKeyGen extends EJBLocalObject {

    /* Container persisted attributes 
     * 	Note: Commented out methods are internal only and not exposed to EJB clients
     *        For example: The Primary Key value cannot be modified after creation
     *        Also, modification of other Entity attributes may be restricted to internal use
     */

    public String		getKeyName();				/* Unique Primary Key name */
    //public void		setKeyName(String KeyName);
    public int			getKeyVal();				/* Value for PK */
    //public void		setKeyValue(int KeyVal);

    /* Accessor methods for relationship fields */

    /* Business methods */    
    public Collection allocBlockOfKeys();
}
