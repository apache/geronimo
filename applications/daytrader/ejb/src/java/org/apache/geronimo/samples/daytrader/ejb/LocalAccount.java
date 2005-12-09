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
import java.sql.Timestamp;
import org.apache.geronimo.samples.daytrader.*;

public interface LocalAccount extends EJBLocalObject {

    /* Container persisted attributes 
     * 	Note: Commented out methods are internal only and not exposed to EJB clients
     *        For example: The Primary Key value cannot be modified after creation
     *        Also, modification of other Entity attributes may be restricted to internal use
     */

    public Integer		getAccountID();				/* accountID */
    //public void		setAccountID(int accountID);
    public int			getLoginCount();			/* loginCount */
    //public void		setLoginCount(int loginCount);
    public int			getLogoutCount();			/* logoutCount */
    //public void		setLogoutCount(int logoutCount);    
    public Timestamp	getLastLogin();				/* lastLogin Date */
    //public void		setLastLogin(Date lastLogin);        
    public Timestamp	getCreationDate();			/* creationDate */
    //public void		setCreationDate(Date creationDate);
    public BigDecimal	getBalance();				/* balance */
    public void			setBalance(BigDecimal balance);            
    public BigDecimal	getOpenBalance();			/* open balance */
    //public void		setOpenBalance(BigDecimal openBalance);                


    /* Accessor methods for relationship fields */
    public LocalAccountProfile	getProfile();	/* This account's profile */
    public Collection 			getHoldings();  /* This account's holdings */
    public Collection 			getOrders();    /* This account's orders */

    /* Business methods */     
    
    public void login(String password);
    public void logout();

    public Collection getClosedOrders() throws FinderException;
    public LocalAccountProfile getProfileForUpdate() throws FinderException;

	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws FinderException;        
	public AccountDataBean getDataBean();

	public AccountProfileDataBean getProfileDataBean();
	public Collection getHoldingDataBeans();
	public Collection getOrderDataBeans();
	
	public String toString();
}
