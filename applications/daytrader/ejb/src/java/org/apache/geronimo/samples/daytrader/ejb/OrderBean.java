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

import org.apache.geronimo.samples.daytrader.util.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.apache.geronimo.samples.daytrader.*;


public abstract class OrderBean
		implements EntityBean {


    private EntityContext context;

    /* Accessor methods for persistent fields */


    public abstract Integer		getOrderID();				/* orderID */
    public abstract void		setOrderID(Integer orderID);
    public abstract String		getOrderType();				/* orderType (buy, sell, etc.) */
    public abstract void		setOrderType(String orderType);    
    public abstract String		getOrderStatus();			/* orderStatus (open, completed, etc.) */
    public abstract void		setOrderStatus(String orderType);        
    public abstract Timestamp	getOpenDate();				/* openDate (when the order was entered) */
    public abstract void		setOpenDate(Timestamp openDate);      
    public abstract Timestamp	getCompletionDate();		/* completionDate */
    public abstract void		setCompletionDate(Timestamp completionDate);            
    public abstract double		getQuantity();				/* quantity */
    public abstract void		setQuantity(double quantity);            
    public abstract BigDecimal	getPrice();					/* price */
    public abstract void		setPrice(BigDecimal price);                
    public abstract BigDecimal	getOrderFee();				/* orderFee */
    public abstract void		setOrderFee(BigDecimal price);                    
    
    /* Accessor methods for relationship fields */
    public abstract LocalAccount	getAccount();			/* The account which placed the order */
    public abstract void			setAccount(LocalAccount account);    
    public abstract LocalQuote		getQuote();				/* The stock purchased/sold in this order */
    public abstract void			setQuote(LocalQuote quote);         /* null for cash transactions */
    public abstract LocalHolding	getHolding();			/* The created/removed holding during this order */
    public abstract void			setHolding(LocalHolding holding);   /* null for cash transactions */
    
    /* Select methods */



    /* Business methods */
    public LocalHolding getHoldingForUpdate()	/* The holding for this order access with intent to update */    
    {
    	return getHolding();
    }
    public boolean isBuy()
    {
    	String orderType = getOrderType();
    	if ( orderType.compareToIgnoreCase("buy") == 0 ) 
    		return true;
    	return false;
    }
    
    public boolean isSell()
    {
    	String orderType = getOrderType();
    	if ( orderType.compareToIgnoreCase("sell") == 0 ) 
    		return true;
    	return false;
    }
    
    public boolean isOpen()
    {
    	String orderStatus = getOrderStatus();
    	if ( (orderStatus.compareToIgnoreCase("open") == 0) ||
	         (orderStatus.compareToIgnoreCase("processing") == 0) ) 
	    		return true;
    	return false;
    }
    
    public boolean isCompleted()
    {
    	String orderStatus = getOrderStatus();
    	if ( (orderStatus.compareToIgnoreCase("completed") == 0) ||
	         (orderStatus.compareToIgnoreCase("alertcompleted") == 0)    ||
	         (orderStatus.compareToIgnoreCase("cancelled") == 0) ) 	         
	    		return true;
    	return false;  	
    }
    
    public boolean isCancelled()
    {
    	String orderStatus = getOrderStatus();
    	if (orderStatus.compareToIgnoreCase("cancelled") == 0)
	    		return true;
    	return false;  	
    }
    

	public void cancel()
	{
		setOrderStatus("cancelled");
	}


	public OrderDataBean getDataBean()
	{
		return new OrderDataBean(getOrderID(),
									getOrderType(),
									getOrderStatus(),
									getOpenDate(),
									getCompletionDate(),
									getQuantity(),
									getPrice(),
									getOrderFee(),
									(String)getQuote().getPrimaryKey()
									);

	}
	

	public String toString()
	{
		return getDataBean().toString();
	}
	
    /* Required javax.ejb.EntityBean interface methods */
    
    public Integer ejbCreate (int orderID, LocalAccount account, LocalQuote quote, LocalHolding holding, String orderType, double quantity)
    throws CreateException {
    	return ejbCreate(new Integer(orderID), account, quote, holding, orderType, quantity);
    }
    
    public Integer ejbCreate (Integer orderID, LocalAccount account, LocalQuote quote, LocalHolding holding, String orderType, double quantity)
	throws CreateException {


		Timestamp currentDate = new Timestamp(System.currentTimeMillis());
				
        setOrderID(orderID);
        setOrderType(orderType);
        setOrderStatus("open");
        setOpenDate(currentDate);
        setQuantity (quantity);
        setPrice (quote.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND));        


		setOrderFee(TradeConfig.getOrderFee(orderType));
		
        return null;
    }


    public void ejbPostCreate (Integer orderID, LocalAccount account, LocalQuote quote, LocalHolding holding, String orderType, double quantity)
	throws CreateException {
		setAccount(account);
		setQuote(quote);
		setHolding(holding);
	}
	    
    public void ejbPostCreate (int orderID, LocalAccount account, LocalQuote quote, LocalHolding holding, String orderType, double quantity)
	throws CreateException { 
		ejbPostCreate(new Integer(orderID), account, quote, holding, orderType, quantity);
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
