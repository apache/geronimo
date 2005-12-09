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
import javax.naming.*;

import org.apache.geronimo.samples.daytrader.util.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.apache.geronimo.samples.daytrader.*;

public abstract class AccountBean
		implements EntityBean {

    private EntityContext context;
    private LocalAccountProfileHome accountProfileHome;

    /* Accessor methods for persistent fields */

    public abstract Integer		getAccountID();				/* accountID */
    public abstract void			setAccountID(Integer accountID);
    public abstract int			getLoginCount();			/* loginCount */
    public abstract void			setLoginCount(int loginCount);
    public abstract int			getLogoutCount();			/* logoutCount */
    public abstract void			setLogoutCount(int logoutCount);    
    public abstract Timestamp		getLastLogin();				/* lastLogin Date */
    public abstract void			setLastLogin(Timestamp lastLogin);        
    public abstract Timestamp		getCreationDate();			/* creationDate */
    public abstract void			setCreationDate(Timestamp creationDate);
    public abstract BigDecimal	getBalance();				/* balance */
    public abstract void			setBalance(BigDecimal balance);            
    public abstract BigDecimal	getOpenBalance();			/* open balance */
    public abstract void			setOpenBalance(BigDecimal openBalance);                
    
    /* Accessor methods for relationship fields */
    public abstract LocalAccountProfile	getProfile();				/* This account's profile */
    public abstract void				setProfile(LocalAccountProfile profile);    
    public abstract Collection		getHoldings();				/* This account's holdings */
    public abstract void				setHoldings(Collection holdings);    
    public abstract Collection		getOrders();				/* This account's orders */
    public abstract void				setOrders(Collection orders);    
   
    /* Select methods */

    /* Business methods */
    
    public void login(String password)
    {
    	LocalAccountProfile profile = getProfile();
    	if ( (profile==null) || (profile.getPassword().equals(password) == false) )
    	{
    		String error = "AccountBean:Login failure for account: " + getAccountID() + 
    					( (profile==null)? "null AccountProfile" :
    						"\n\tIncorrect password-->" + profile.getUserID() + ":" + profile.getPassword() );
    		throw new EJBException(error);
    	}
    	
    	setLastLogin( new Timestamp(System.currentTimeMillis()) );
    	setLoginCount( getLoginCount() + 1 );
	}
	
    public void logout()
    {
    	setLogoutCount( getLogoutCount() + 1 );
	}

	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData)
	throws FinderException
	{
		return getProfileForUpdate().updateAccountProfile(profileData);
	}
   	
   	public AccountDataBean getDataBean()
   	{
   		return new AccountDataBean(getAccountID(),
									getLoginCount(),
									getLogoutCount(),
									getLastLogin(),
									getCreationDate(),
									getBalance(),
									getOpenBalance(),
									(String)getProfile().getPrimaryKey());
   	}    

	public AccountProfileDataBean getProfileDataBean()
	{
		return getProfile().getDataBean();
	}
	public Collection getHoldingDataBeans()
	{
		Collection holdings = getHoldings();
		ArrayList holdingDataBeans = new ArrayList(holdings.size());
		Iterator it = holdings.iterator();
		while (it.hasNext())
		{
			LocalHolding holding = (LocalHolding) it.next();
			HoldingDataBean holdingData = holding.getDataBean();
			holdingDataBeans.add(holdingData);
		}
		return holdingDataBeans;	
	}

	
    /* Select methods */
    public abstract Collection ejbSelectClosedOrders(Integer accountID)
            throws FinderException;	
                      
	
	public Collection getClosedOrders()
			throws FinderException
	{
		return ejbSelectClosedOrders(getAccountID());
	}
	public LocalAccountProfile getProfileForUpdate()
			throws FinderException
	{
		return getProfile();
	}	
	
	public Collection getOrderDataBeans()
	{
		Collection orders = getOrders();
		ArrayList orderDataBeans = new ArrayList(orders.size());
		Iterator it = orders.iterator();
		while (it.hasNext())
		{
			LocalOrder order = (LocalOrder) it.next();
			OrderDataBean orderData = order.getDataBean();
			orderDataBeans.add(orderData);
		}
		return orderDataBeans;
	}
	
	public String toString()
	{
		return getDataBean().toString();
	}
	
	

    /* Required javax.ejb.EntityBean interface methods */
    public Integer ejbCreate (int accountID, String userID, String password, BigDecimal openBalance,
            							String fullname, String address, String email, String creditcard)
    throws CreateException {
    	return ejbCreate(new Integer(accountID), userID, password, openBalance,
    	        							fullname, address, email, creditcard);
    }
    
    public Integer ejbCreate (Integer accountID, String userID, String password, BigDecimal openBalance, 
            							String fullname, String address, String email, String creditCard)
	throws CreateException {
		
        setAccountID(accountID);
		setLoginCount(0);
		setLogoutCount(0);
		Timestamp current = new Timestamp(System.currentTimeMillis());
        setLastLogin(current);
        setCreationDate(current);
		openBalance = openBalance.setScale(FinancialUtils.SCALE, FinancialUtils.ROUND);
        setBalance (openBalance);
        setOpenBalance (openBalance);
	
        return null;
    }

    public void ejbPostCreate (Integer accountID, String userID, String password, BigDecimal openBalance,
        							String fullname, String address, String email, String creditCard)          
	throws CreateException {
		 //Account creates a new AccountProfile entity here.        
		LocalAccountProfile profile = accountProfileHome.create(userID, password, fullname, address, email, creditCard);
		setProfile(profile);
	}
	    
    public void ejbPostCreate (int accountID, String userID, String password, BigDecimal openBalance,
        							String fullname, String address, String email, String creditcard)
	throws CreateException { 
		ejbPostCreate(new Integer(accountID), userID, password, openBalance,fullname, address, email, creditcard);	
	}

    public void setEntityContext(EntityContext ctx) {
        context = ctx;
        try {
			InitialContext ic = new InitialContext();
			accountProfileHome = (LocalAccountProfileHome) ic.lookup("java:comp/env/ejb/AccountProfile");
		}
 	    catch (NamingException ne)
  	    {
			Log.error(ne, "Account EJB: Lookup of Local Entity Homes Failed\n" + ne);
		}
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
