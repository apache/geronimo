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

public interface LocalQuoteHome extends EJBLocalHome {
    
    public LocalQuote create (String symbol, String companyName, BigDecimal price)
    	throws CreateException;
    
    public LocalQuote findByPrimaryKeyForUpdate (String symbol)
    	throws FinderException;

    public LocalQuote findByPrimaryKey (String symbol)
    	throws FinderException;


    public LocalQuote findOne ()
    	throws FinderException;

    public Collection findAll() 
        throws FinderException;

	// Find TradeStockIndexAvg. Stocks -- unordered
    public Collection findTSIAQuotes() 
        throws FinderException;
	// Find TradeStockIndexAvg. Stocks -- ordered by change
    public Collection findTSIAQuotesOrderByChange() 
        throws FinderException;
        
    /* findQuotes must take a comma seperated String of symbols
     *  EJB QL will not take a collection type
     */
    public Collection findQuotes(String symbols)
	throws FinderException;

}
