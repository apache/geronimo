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
import java.util.Date;
import java.io.Serializable;

import org.apache.geronimo.samples.daytrader.util.*;

public class AccountDataBean implements Serializable
{

	/* Accessor methods for persistent fields */

	private Integer 	accountID; 		/* accountID */
	private int 		loginCount; 	/* loginCount */
	private int 		logoutCount; 	/* logoutCount */
	private Date		lastLogin; 		/* lastLogin Date */
	private Date		creationDate; 	/* creationDate */
	private BigDecimal 	balance; 		/* balance */
	private BigDecimal 	openBalance; 	/* open balance */

	/* Accessor methods for relationship fields are only included for the AccountProfile profileID */

	private String 		profileID;
	public AccountDataBean(){ }
	public AccountDataBean(Integer accountID,
								int	loginCount,
								int logoutCount,
								Date lastLogin,
								Date creationDate,
								BigDecimal balance,
								BigDecimal openBalance,
								String profileID)
	{
		setAccountID(accountID);
		setLoginCount(loginCount);
		setLogoutCount(logoutCount);
		setLastLogin(lastLogin);
		setCreationDate(creationDate);
		setBalance(balance);
		setOpenBalance(openBalance);
		setProfileID(profileID);	}

	public static AccountDataBean getRandomInstance() {
		return new AccountDataBean(new Integer(TradeConfig.rndInt(100000)), //accountID
		TradeConfig.rndInt(10000), //loginCount
		TradeConfig.rndInt(10000), //logoutCount
		new java.util.Date(), //lastLogin
		new java.util.Date(TradeConfig.rndInt(Integer.MAX_VALUE)), //creationDate
		TradeConfig.rndBigDecimal(1000000.0f), //balance
		TradeConfig.rndBigDecimal(1000000.0f), //openBalance
		TradeConfig.rndUserID() //profileID	  
		);
	}

	public String toString()
	{
		return "\n\tAccount Data for account: " + getAccountID()
			+ "\n\t\t   loginCount:" + getLoginCount()
			+ "\n\t\t  logoutCount:" + getLogoutCount()
			+ "\n\t\t    lastLogin:" + getLastLogin()
			+ "\n\t\t creationDate:" + getCreationDate()
			+ "\n\t\t      balance:" + getBalance()
			+ "\n\t\t  openBalance:" + getOpenBalance()
			+ "\n\t\t    profileID:" + getProfileID()			
			;
	}
	public String toHTML()
	{
		return "<BR>Account Data for account: <B>" + getAccountID() + "</B>"
			+ "<LI>   loginCount:" + getLoginCount() + "</LI>"
			+ "<LI>  logoutCount:" + getLogoutCount() + "</LI>"
			+ "<LI>    lastLogin:" + getLastLogin() + "</LI>"
			+ "<LI> creationDate:" + getCreationDate() + "</LI>"
			+ "<LI>      balance:" + getBalance() + "</LI>"
			+ "<LI>  openBalance:" + getOpenBalance() + "</LI>"
			+ "<LI>    profileID:" + getProfileID() + "</LI>"
			;
	}
	public void print()
	{
		Log.log( this.toString() );
	}	
	
	/**
	 * Gets the accountID
	 * @return Returns a Integer
	 */
	public Integer getAccountID() {
		return accountID;
	}
	/**
	 * Sets the accountID
	 * @param accountID The accountID to set
	 */
	public void setAccountID(Integer accountID)
	{
		this.accountID = accountID;
	}
	/**
	 * Gets the loginCount
	 * @return Returns a int
	 */
	public int getLoginCount() {
		return loginCount;
	}
	/**
	 * Sets the loginCount
	 * @param loginCount The loginCount to set
	 */
	public void setLoginCount(int loginCount)
	{
		this.loginCount = loginCount;
	}

	/**
	 * Gets the logoutCount
	 * @return Returns a int
	 */
	public int getLogoutCount() {
		return logoutCount;
	}
	/**
	 * Sets the logoutCount
	 * @param logoutCount The logoutCount to set
	 */
	public void setLogoutCount(int logoutCount)
	{
		this.logoutCount = logoutCount;
	}

	/**
	 * Gets the lastLogin
	 * @return Returns a Date
	 */
	public Date getLastLogin() {
		return lastLogin;
	}
	/**
	 * Sets the lastLogin
	 * @param lastLogin The lastLogin to set
	 */
	public void setLastLogin(Date lastLogin)
	{
		this.lastLogin = lastLogin;
	}

	/**
	 * Gets the creationDate
	 * @return Returns a Date
	 */
	public Date getCreationDate() {
		return creationDate;
	}
	/**
	 * Sets the creationDate
	 * @param creationDate The creationDate to set
	 */
	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
	}

	/**
	 * Gets the balance
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getBalance() {
		return balance;
	}
	/**
	 * Sets the balance
	 * @param balance The balance to set
	 */
	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}

	/**
	 * Gets the openBalance
	 * @return Returns a BigDecimal
	 */
	public BigDecimal getOpenBalance() {
		return openBalance;
	}
	/**
	 * Sets the openBalance
	 * @param openBalance The openBalance to set
	 */
	public void setOpenBalance(BigDecimal openBalance)
	{
		this.openBalance = openBalance;
	}

	/**
	 * Gets the profileID
	 * @return Returns a String
	 */
	public String getProfileID() {
		return profileID;
	}
	/**
	 * Sets the profileID
	 * @param profileID The profileID to set
	 */
	public void setProfileID(String profileID) {
		this.profileID = profileID;
	}
	/**
	 * Gets the profileID
	 * @return Returns a String
	 */
	/* Disabled for D185273
	public String getUserID() {
		return getProfileID();
	}
	*/

}