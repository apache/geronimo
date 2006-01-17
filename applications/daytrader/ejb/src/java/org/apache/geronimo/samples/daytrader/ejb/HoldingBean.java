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
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.apache.geronimo.samples.daytrader.*;

public abstract class HoldingBean
		implements EntityBean {

    private EntityContext context;

    /* Accessor methods for persistent fields */

    public abstract Integer		getHoldingID();				/* holdingID */
    public abstract void		setHoldingID(Integer holdingID);
    public abstract double		getQuantity();				/* quantity */
    public abstract void		setQuantity(double quantity);    
    public abstract BigDecimal	getPurchasePrice();			/* purchasePrice */
    public abstract void		setPurchasePrice(BigDecimal purchasePrice);            
    public abstract Timestamp	getPurchaseDate();			/* purchaseDate */
    public abstract void		setPurchaseDate(Timestamp purchaseDate);               

    /* Accessor methods for relationship fields */
    public abstract LocalAccount	getAccount();			/* Account(1) <---> Holding(*) */
    public abstract void			setAccount(LocalAccount account);    
    public abstract LocalQuote		getQuote();				/* Holding(*)  ---> Quote(1) */
    public abstract void			setQuote(LocalQuote quote);    

    /* Select methods */
 
   public abstract LocalQuote ejbSelectQuoteFromSymbol(String symbol)
       throws FinderException;


	private LocalQuote getQuoteFromSymbol(String symbol) throws FinderException 
	{
         LocalHolding holding = (LocalHolding) context.getEJBLocalObject();
         return ejbSelectQuoteFromSymbol(symbol);
	}

    /* Business methods */
    
    public HoldingDataBean getDataBean()
    {
    	return new HoldingDataBean(getHoldingID(),
									getQuantity(),
									getPurchasePrice(),
									getPurchaseDate(),
									(String)getQuote().getPrimaryKey());
    }

	public String toString()
	{
		return getDataBean().toString();
	}
    

    /* Required javax.ejb.EntityBean interface methods */
    public Integer ejbCreate (int holdingID, LocalAccount account, LocalQuote quote, double quantity, BigDecimal purchasePrice) 
    throws CreateException {
    	return this.ejbCreate(new Integer(holdingID), account, quote, quantity, purchasePrice);
    }

    public Integer ejbCreate (Integer holdingID, LocalAccount account, LocalQuote quote, double quantity, BigDecimal purchasePrice) 
	throws CreateException {

        setHoldingID(holdingID);
		setQuantity(quantity);
		setPurchasePrice(purchasePrice);
		setPurchaseDate(new Timestamp(System.currentTimeMillis()));

        return null;
    }

    public void ejbPostCreate (Integer holdingID, LocalAccount account, LocalQuote quote, double quantity, BigDecimal purchasePrice)
	throws CreateException { 
		
		//Establish relationship with Quote for this holding
		setAccount(account);
		setQuote(quote);
	}
	    
    public void ejbPostCreate (int holdingID, LocalAccount account, LocalQuote quote, double quantity, BigDecimal purchasePrice)
	throws CreateException { 
	    	this.ejbPostCreate(new Integer(holdingID), account, quote, quantity, purchasePrice);
	}

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
    }
    
    public void unsetEntityContext() {
        context = null;
    }
    
    public void ejbRemove() {
    }
    
    public void ejbLoad() {
    }
    
    public void ejbStore() {
    }
    
    public void ejbPassivate() { 
    }
    
    public void ejbActivate() { 
    }
}
