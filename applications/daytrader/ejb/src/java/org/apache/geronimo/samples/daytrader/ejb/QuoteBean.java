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

import java.util.Collection;
import java.util.Iterator;
import java.math.BigDecimal;
import org.apache.geronimo.samples.daytrader.*;

public abstract class QuoteBean 
		implements EntityBean {

    private EntityContext context;
    
    /* Accessor methods for persistent fields */

    public abstract String		getSymbol();				/* symbol */
    public abstract void		setSymbol(String symbol);
    public abstract String		getCompanyName();			/* companyName */
    public abstract void		setCompanyName(String companyName);
    public abstract double		getVolume();				/* volume */
    public abstract void		setVolume(double volume);    
    public abstract BigDecimal	getPrice();					/* price */
    public abstract void		setPrice(BigDecimal price);
    public abstract BigDecimal	getOpen();					/* open price */ 
    public abstract void		setOpen(BigDecimal price);  	      
    public abstract BigDecimal	getLow();					/* low price */
    public abstract void		setLow(BigDecimal price);
    public abstract BigDecimal	getHigh();					/* high price */
    public abstract void		setHigh(BigDecimal price);
    public abstract double		getChange();				/* price change */
    public abstract void		setChange(double change);
    
    /* Accessor methods for relationship fields */
    public abstract Collection 	getOrders();
    public abstract void		setOrders(Collection orders);

    /* Select methods */
/*                	
    public abstract Collection ejbSelectTopGainPrices()
            throws FinderException;
    public abstract Collection ejbSelectTopLossPrices()
            throws FinderException;    
	public abstract Collection ejbSelectChangeGreaterThan(double minGain)
            throws FinderException;    
	public abstract Collection ejbSelectChangeLessThan(double minLoss)
            throws FinderException;       
    public abstract Collection ejbSelectPricesForTSIA()
        throws FinderException;
    public abstract Collection ejbSelectOpenPricesForTSIA()
        throws FinderException;
*/        
    public abstract Collection ejbSelectTotalVolume()
        throws FinderException;       

    /* Business methods */

    public void updatePrice(BigDecimal current)
    {
    	int compare = current.compareTo(getPrice());
    	// Do nothing if the price has not changed
		if ( compare == 0) return;  	

  		setPrice(current.setScale(FinancialUtils.SCALE, FinancialUtils.ROUND));  //Update current price
  		setChange( current.doubleValue() - getOpen().doubleValue() );
  		
  		//Reset high, low if applicable
		if ( current.compareTo(getHigh()) > 0 ) setHigh(current);
		else if ( current.compareTo(getLow()) < 0 ) setLow(current);
    }
    
    public void updatePrice(double current)
    {
    	updatePrice( new BigDecimal(current));
    }
    
    public void addToVolume(double quantity)
    {
    	setVolume( getVolume() + quantity);
    }

    
/* To get TopGainers and Losers, 
 * using an algorithm in TradeSession EJB instead of ejbSelect approach
 *
 *
    public Collection getTopGainers(int count) 
	throws FinderException
    {
//      LocalQuote quote = (LocalQuote) context.getEJBLocalObject();    	
      Collection topGainPrices = ejbSelectTopGainPrices();
      ArrayList topGains = new ArrayList(topGainPrices);
 
	  count = count -1; //index starts with zero
	   double minGain = 0.0;
	  if ( topGains.size() >= count )
		  minGain = ((Double)topGains.get(count)).doubleValue();

   	  return ejbSelectChangeGreaterThan(minGain);
    }

    public Collection getTopLosers(int count) 
	throws FinderException
    {
//      LocalQuote quote = (LocalQuote) context.getEJBLocalObject();    	
      Collection topLossPrices = ejbSelectTopLossPrices();
      ArrayList topLoss = new ArrayList(topLossPrices);

	  count = count -1; //index starts with zero
	  double minLoss = 0.0;
	  if ( topLoss.size() >= count )
		  minLoss = ((Double)topLoss.get(count)).doubleValue();

   	  return ejbSelectChangeLessThan(minLoss);
    }

    public BigDecimal getTSIA() 
	throws FinderException
    {
     // LocalQuote quote = (LocalQuote) context.getEJBLocalObject();
	  BigDecimal TSIA = FinancialUtils.ZERO;
	  Collection currentPrices = ejbSelectPricesForTSIA();
	  int size = currentPrices.size();
 
 	  if (size > 0)
 	  {
		  Iterator it = currentPrices.iterator();
		  while (it.hasNext())
		  {
		  	BigDecimal price = (BigDecimal)it.next();
		  	TSIA = TSIA.add(price);
		  }
	  
	      TSIA = TSIA.divide(new BigDecimal(size), FinancialUtils.ROUND);
 	  }
 	  return TSIA;
    }
    
    public BigDecimal getOpenTSIA() 
	throws FinderException
    {
     // LocalQuote quote = (LocalQuote) context.getEJBLocalObject();
	  BigDecimal openTSIA = FinancialUtils.ZERO;
	  Collection openPrices = ejbSelectOpenPricesForTSIA();
	  int size = openPrices.size();
 
 	  if (size > 0)
 	  {
		  Iterator it = openPrices.iterator();
		  while (it.hasNext())
		  {
		  	BigDecimal price = (BigDecimal)it.next();
		  	openTSIA = openTSIA.add(price);
		  }
	  
	      openTSIA = openTSIA.divide(new BigDecimal(size), FinancialUtils.ROUND);
 	  }
 	  return openTSIA;
    }
 *
 */
    
	public double getTotalVolume()
	throws FinderException	
	{
		double totalVolume = 0.0;
		Collection volumes = ejbSelectTotalVolume();
		Iterator it = volumes.iterator();

		while (it.hasNext())
		{
			Double volume;
			Object o = it.next();
			try { 
				Float f = (Float) o; 
				volume = new Double(f.doubleValue());
			} 
			catch (Exception e)
			{ 
				volume = (Double) o;
			}
			totalVolume = totalVolume + volume.doubleValue();

		}
		return totalVolume;
	}    
    
    public QuoteDataBean getDataBean()
    {
    	return new QuoteDataBean(getSymbol(),getCompanyName(),getVolume(),getPrice(),getOpen(),
    								getLow(),getHigh(),getChange());
    }

	public String toString()
	{
		return getDataBean().toString();
	}
	
    /* Required javax.ejb.EntityBean interface methods */

    public String ejbCreate (String symbol, String companyName, BigDecimal price) 
	throws CreateException {
		if (Log.doTrace()) Log.traceEnter("QuoteBean:ejbCreate");
        setSymbol(symbol);
        setCompanyName(companyName);
        price = price.setScale(FinancialUtils.SCALE, FinancialUtils.ROUND);
        setVolume(0.0);
        setPrice(price);
        setOpen (price);
        setLow  (price);
        setHigh (price);
        setChange(0.0);
		if (Log.doTrace()) Log.traceExit("QuoteBean:ejbCreate");
        return null;
    }
         
    public void ejbPostCreate (String symbol, String companyName, BigDecimal price) 
	throws CreateException { }

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
