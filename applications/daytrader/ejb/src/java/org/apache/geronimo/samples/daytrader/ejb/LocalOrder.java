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


public interface LocalOrder extends EJBLocalObject {


    /* Container persisted attributes 
     * 	Note: Commented out methods are internal only and not exposed to EJB clients
     *        For example: The Primary Key value cannot be modified after creation
     *        Also, modification of other Entity attributes may be restricted to internal use
     */


    public Integer		getOrderID();				/* orderID */
    //public void		setOrderID(Integer accountID);
    public String		getOrderType();				/* orderType (buy, sell, etc.) */
    //public void		setOrderType(String orderType);    
    public String		getOrderStatus();			/* orderStatus (open, completed, etc.) */
    public void			setOrderStatus(String orderType);        
    public Timestamp	getOpenDate();				/* openDate (when the order was entered) */
    //public void		setOpenDate(Date openDate);      
    public Timestamp	getCompletionDate();		/* completionDate */
    public void			setCompletionDate(Timestamp completionDate);            
    public double		getQuantity();				/* quantity */
    //public void		setQuantity(double quantity);            
    public BigDecimal	getPrice();					/* price */
    //public void		setPrice(BigDecimal price);                
    public BigDecimal	getOrderFee();				/* price */
    //public void		setOrderFee(BigDecimal price);                    


    /* Accessor methods for relationship fields */
    public LocalAccount		getAccount();			/* The account which placed the order */
    //public void			setAccount(LocalAccount account);    
    public LocalQuote		getQuote();				/* The stock purchased/sold in this order */
    //public void			setQuote(LocalQuote quote);         /* null for cash transactions */
    public LocalHolding		getHolding();			/* The created/removed holding for this order */
    public void				setHolding(LocalHolding holding);   /* null for cash transactions */

    /* Business methods */
    public LocalHolding getHoldingForUpdate();	/* The holding for this order access with intent to update */        
    public void    cancel();
        
    public boolean isBuy();
    public boolean isSell();
    public boolean isOpen();
    public boolean isCompleted();
    public boolean isCancelled();
        
	public OrderDataBean getDataBean();
	public String toString();	
}
