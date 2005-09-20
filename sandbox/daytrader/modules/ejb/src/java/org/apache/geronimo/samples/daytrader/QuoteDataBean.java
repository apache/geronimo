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

package org.apache.geronimo.samples.daytrader;

import java.math.BigDecimal;
import java.io.Serializable;

import org.apache.geronimo.samples.daytrader.util.*;

public class QuoteDataBean implements Serializable
{

	/* Accessor methods for persistent fields */

	private String symbol; 		/* symbol */
	private String companyName; /* companyName */
	private double volume; 		/* volume */	
	private BigDecimal price; 	/* price */
	private BigDecimal open; 	/* open price */
	private BigDecimal low; 	/* low price */
	private BigDecimal high;	/* high price */
	private double change; 		/* price change */

	/* Accessor methods for relationship fields are not kept in the DataBean */
	public QuoteDataBean(){ }

	public QuoteDataBean(String symbol, String companyName, double volume, 
						BigDecimal price, BigDecimal open, BigDecimal low, 
						BigDecimal high, double change)
	{
		setSymbol(symbol);
		setCompanyName(companyName);
		setVolume(volume);
		setPrice(price);
		setOpen(open);
		setLow(low);
		setHigh(high);
		setChange(change);
	}

	public static QuoteDataBean getRandomInstance() {
		return new QuoteDataBean(
			TradeConfig.rndSymbol(), 				//symbol
			TradeConfig.rndSymbol() + " Incorporated", 		//Company Name
			TradeConfig.rndFloat(100000),			//volume			
			TradeConfig.rndBigDecimal(1000.0f), 	//price
			TradeConfig.rndBigDecimal(1000.0f), 	//open
			TradeConfig.rndBigDecimal(1000.0f), 	//low				
			TradeConfig.rndBigDecimal(1000.0f), 	//high
			TradeConfig.rndFloat(100000)			//volume					  
		);
	}
	
	//Create a "zero" value quoteDataBean for the given symbol
	public QuoteDataBean(String symbol)
	{
		setSymbol(symbol);
	}
	
	
	public String toString()
	{
		return "\n\tQuote Data for: " + getSymbol()
			+ "\n\t\t companyName: " + getCompanyName()
			+ "\n\t\t      volume: " + getVolume()
			+ "\n\t\t       price: " + getPrice()
			+ "\n\t\t        open: " + getOpen()
			+ "\n\t\t         low: " + getLow()
			+ "\n\t\t        high: " + getHigh()
			+ "\n\t\t      change: " + getChange()
			;
	}

	public String toHTML()
	{
		return "<BR>Quote Data for: " + getSymbol()
			+ "<LI> companyName: " + getCompanyName() + "</LI>"
			+ "<LI>      volume: " + getVolume()+ "</LI>"
			+ "<LI>       price: " + getPrice()+ "</LI>"
			+ "<LI>        open: " + getOpen()+ "</LI>"
			+ "<LI>         low: " + getLow()+ "</LI>"
			+ "<LI>        high: " + getHigh()+ "</LI>"
			+ "<LI>      change: " + getChange()+ "</LI>"
			;
	}
	public void print()
	{
		Log.log( this.toString() );
	}

	/**
	 * Gets the symbol
	 * @return Returns a String
	 */
	public String getSymbol()
	{
		return symbol;
	}
	/**
	 * Sets the symbol
	 * @param symbol The symbol to set
	 */
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	/**
	 * Gets the companyName
	 * @return Returns a String
	 */
	public String getCompanyName()
	{
		return companyName;
	}
	/**
	 * Sets the companyName
	 * @param companyName The companyName to set
	 */
	public void setCompanyName(String companyName)
	{
		this.companyName = companyName;
	}

	/**
	 * Gets the price
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getPrice()
	{
		return price;
	}
	/**
	 * Sets the price
	 * @param price The price to set
	 */
	public void setPrice(BigDecimal price)
	{
		this.price = price;
	}

	/**
	 * Gets the open
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getOpen()
	{
		return open;
	}
	/**
	 * Sets the open
	 * @param open The open to set
	 */
	public void setOpen(BigDecimal open)
	{
		this.open = open;
	}

	/**
	 * Gets the low
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getLow()
	{
		return low;
	}
	/**
	 * Sets the low
	 * @param low The low to set
	 */
	public void setLow(BigDecimal low)
	{
		this.low = low;
	}

	/**
	 * Gets the high
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getHigh()
	{
		return high;
	}
	/**
	 * Sets the high
	 * @param high The high to set
	 */
	public void setHigh(BigDecimal high)
	{
		this.high = high;
	}

	/**
	 * Gets the change
	 * @return Returns a double
	 */
	public double getChange()
	{
		return change;
	}
	/**
	 * Sets the change
	 * @param change The change to set
	 */
	public void setChange(double change)
	{
		this.change = change;
	}

	/**
	 * Gets the volume
	 * @return Returns a BigDecimal
	 */
	public double getVolume() {
		return volume;
	}
	/**
	 * Sets the volume
	 * @param volume The volume to set
	 */
	public void setVolume(double volume) {
		this.volume = volume;
	}

}