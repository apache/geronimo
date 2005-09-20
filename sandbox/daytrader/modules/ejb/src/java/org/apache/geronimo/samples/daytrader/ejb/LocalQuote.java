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
import org.apache.geronimo.samples.daytrader.*;

public interface LocalQuote extends EJBLocalObject {

    /* Container persisted attributes 
     * 	Note: Commented out methods are internal only and not exposed to EJB clients
     *        For example: The Primary Key value cannot be modified after creation
     *        Also, modification of other Entity attributes may be restricted to internal use
     */
     
    public String		getSymbol();    /* symbol */
    public void			setSymbol(String symbol);
    public String		getCompanyName();    /* companyName */
    public void			setCompanyName(String companyName);
    public double		getVolume();    /* volume */
    //public void		setVolume(double volume);
    public BigDecimal	getPrice();    /* price */
    //public void		setPrice(BigDecimal price);
    public BigDecimal	getOpen();    /* open price */
    //public void		setOpen(BigDecimal price);
    public BigDecimal	getLow();    /* low price */
    /* 	setLow not exposed to client applications
    public void		setLow(BigDecimal price);
     */
    /* high price */
    public BigDecimal	getHigh();
    /* 	setHigh not exposed to client applications
    public void		setHigh(BigDecimal price);
     */
     
    /* Change in price */
    /* Note: this can be computed by "current-open"
       but is added here for convenience
     */
    public double	getChange();
    /* 	setChange not exposed to client applications
    public void		setChange(double change);
     */     

    /* Relationshiop fields */
    public abstract Collection 	getOrders();
    public abstract void		setOrders(Collection orders);

    /* Business methods */
    
    public void updatePrice(BigDecimal price);
    public void updatePrice(double price);
    
    public void addToVolume(double quantity);    

	/* FUTURE This is the approach using ejbSelect methods
	 * using Trade Session EJB approach instead

    public Collection getTopGainers(int count)
	throws FinderException;
    public Collection getTopLosers(int count) 
	throws FinderException;
    public BigDecimal getTSIA() 
	throws FinderException;

    public BigDecimal getOpenTSIA() 
	throws FinderException;
	
	 *
	 */
	
	public double getTotalVolume()	
	throws FinderException;	

    public QuoteDataBean getDataBean();
	public String toString();    

}
