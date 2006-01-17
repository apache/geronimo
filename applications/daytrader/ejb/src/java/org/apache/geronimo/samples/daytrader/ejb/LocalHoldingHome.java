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


public interface LocalHoldingHome extends EJBLocalHome {
    
    public LocalHolding create (Integer holdingID, LocalAccount account, LocalQuote quote, double quantity, BigDecimal purchasePrice)
    	throws CreateException;
    	
    public LocalHolding create (int holdingID, LocalAccount account, LocalQuote quote, double quantity, BigDecimal purchasePrice) 
    	throws CreateException;    	
    
    public LocalHolding findByPrimaryKeyForUpdate (Integer holdingID)
    	throws FinderException;

    public LocalHolding findByPrimaryKey (Integer holdingID)
    	throws FinderException;

    public Collection findAll() 
        throws FinderException;

    public Collection findByAccountID(Integer AccountID)
	throws FinderException;

    public Collection findByUserID(String userID)
	throws FinderException;

}
