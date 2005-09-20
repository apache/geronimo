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


public interface LocalOrderHome extends EJBLocalHome {

    public LocalOrder create (int orderID, LocalAccount account, LocalQuote quote, LocalHolding holding, String orderType, double quantity)
    	throws CreateException;
    	
    public LocalOrder create (Integer orderID, LocalAccount account, LocalQuote quote, LocalHolding holding, String orderType, double quantity)
    	throws CreateException;
    
    public LocalOrder findByPrimaryKeyForUpdate (Integer orderID)
    	throws FinderException;
    	
    public LocalOrder findByPrimaryKey (Integer orderID)
    	throws FinderException;

    public Collection findAll() 
        throws FinderException;

    public Collection findByUserID (String userID)
    	throws FinderException;

	public Collection findClosedOrders(String userID)
    	throws FinderException;	

	public Collection findClosedOrdersForUpdate(String userID)
    	throws FinderException;	    
}
