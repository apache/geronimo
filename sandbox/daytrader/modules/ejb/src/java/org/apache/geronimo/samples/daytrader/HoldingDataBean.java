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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.geronimo.samples.daytrader.util.*;

public class HoldingDataBean
		implements Serializable {

    /* persistent/relationship fields */

    private Integer		holdingID;			/* holdingID */
    private double		quantity;			/* quantity */
    private BigDecimal	purchasePrice;		/* purchasePrice */
    private Date		purchaseDate;		/* purchaseDate */
    private String		quoteID;			/* Holding(*)  ---> Quote(1) */

	public HoldingDataBean(){ }
	public HoldingDataBean(Integer	holdingID,
						    double quantity,
						    BigDecimal purchasePrice,
						    Date purchaseDate,
						    String quoteID)
	{
		setHoldingID(holdingID);
		setQuantity(quantity);
		setPurchasePrice(purchasePrice);
		setPurchaseDate(purchaseDate);		
		setQuoteID(quoteID);
	}
	
	public static HoldingDataBean getRandomInstance() {
		return new HoldingDataBean(
			new Integer(TradeConfig.rndInt(100000)), 	//holdingID
			TradeConfig.rndQuantity(), 					//quantity
			TradeConfig.rndBigDecimal(1000.0f),		 	//purchasePrice				
			new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), //purchaseDate
			TradeConfig.rndSymbol()						// symbol  
		);
	}

	public String toString()
	{
		return "\n\tHolding Data for holding: " + getHoldingID() 
			+ "\n\t\t      quantity:" + getQuantity()
			+ "\n\t\t purchasePrice:" + getPurchasePrice()
			+ "\n\t\t  purchaseDate:" + getPurchaseDate()
			+ "\n\t\t       quoteID:" + getQuoteID()
			;
	}

	public String toHTML()
	{
		return "<BR>Holding Data for holding: " + getHoldingID() + "</B>"
			+ "<LI>      quantity:" + getQuantity() + "</LI>"
			+ "<LI> purchasePrice:" + getPurchasePrice() + "</LI>"
			+ "<LI>  purchaseDate:" + getPurchaseDate() + "</LI>"
			+ "<LI>       quoteID:" + getQuoteID() + "</LI>"
			;
	}
	public void print()
	{
		Log.log( this.toString() );
	}	   
	/**
	 * Gets the holdingID
	 * @return Returns a Integer
	 */
	public Integer getHoldingID() {
		return holdingID;
	}
	/**
	 * Sets the holdingID
	 * @param holdingID The holdingID to set
	 */
	public void setHoldingID(Integer holdingID)
	{
		this.holdingID = holdingID;
	}

	/**
	 * Gets the quantity
	 * @return Returns a BigDecimal
	 */
	public double getQuantity() {
		return quantity;
	}
	/**
	 * Sets the quantity
	 * @param quantity The quantity to set
	 */
	public void setQuantity(double quantity)
	{
		this.quantity = quantity;
	}

	/**
	 * Gets the purchasePrice
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getPurchasePrice() {
		return purchasePrice;
	}
	/**
	 * Sets the purchasePrice
	 * @param purchasePrice The purchasePrice to set
	 */
	public void setPurchasePrice(BigDecimal purchasePrice)
	{
		this.purchasePrice = purchasePrice;
	}

	/**
	 * Gets the purchaseDate
	 * @return Returns a Date
	 */
	public Date getPurchaseDate() {
		return purchaseDate;
	}
	/**
	 * Sets the purchaseDate
	 * @param purchaseDate The purchaseDate to set
	 */
	public void setPurchaseDate(Date purchaseDate)
	{
		this.purchaseDate = purchaseDate;
	}

	/**
	 * Gets the quoteID
	 * @return Returns a Integer
	 */
	public String getQuoteID() {
		return quoteID;
	}
	/**
	 * Sets the quoteID
	 * @param quoteID The quoteID to set
	 */
	public void setQuoteID(String quoteID)
	{
		this.quoteID = quoteID;
	}
	/**
	 * Gets the quoteID
	 * @return Returns a Integer
	 */
	/* Disabled for D185273
	public String getSymbol() {
		return getQuoteID();
	}
	*/
}
