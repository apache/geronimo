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
import java.math.BigDecimal;


public interface LocalAccountHome extends EJBLocalHome {

			//Account create takes parameters for the Account and Profile Entities
			//Account acts as a proxy to user Profile information    
    public LocalAccount create (int accountID, String userID, String password, BigDecimal openBalance,
    							String fullname, String address, String email, String creditcard)
    	throws CreateException;
    	
    public LocalAccount create (Integer accountID, String userID, String password, BigDecimal openBalance,
       							String fullname, String address, String email, String creditcard)
    	throws CreateException;    	

    public LocalAccount findByPrimaryKey (Integer accountID)
    	throws FinderException;
    
    public LocalAccount findByPrimaryKeyForUpdate (Integer accountID)
    	throws FinderException;

    public Collection findAll() 
        throws FinderException;

    public LocalAccount findByUserID(String userID)
	throws FinderException;

    public LocalAccount findByUserIDForUpdate(String userID)
	throws FinderException;
}
