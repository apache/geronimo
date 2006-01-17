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
import org.apache.geronimo.samples.daytrader.*;

public abstract class AccountProfileBean
		implements EntityBean {

    private EntityContext context;

    /* Accessor methods for persistent fields */
	public abstract String		getUserID();				/* userID */
    public abstract void		setUserID(String userID);  
    public abstract String		getPassword();				/* password */
    public abstract void		setPassword(String password);
    public abstract String		getFullName();				/* fullName */
    public abstract void		setFullName(String fullName);  
    public abstract String		getAddress();				/* address */
    public abstract void		setAddress(String address);  
    public abstract String		getEmail();					/* email */
    public abstract void		setEmail(String email);  
    public abstract String		getCreditCard();			/* creditCard */
    public abstract void		setCreditCard(String creditCard);  
 
    /* Accessor methods for relationship fields */
    
    //Account --> AccountProfile is a unidirectional relationship
    // 		no relationship fields here
    /* Accessor methods for relationship fields */
    public abstract LocalAccount	getAccount();				/* This profile's account */
    public abstract void				setAccount(LocalAccount account);    


    /* Select methods */


    /* Business methods */
    public LocalAccount getAccountForUpdate() /* Get ths profile's account with a lock */
    {
    	 return getAccount();		
    }
    
	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData)
	{
		setPassword(profileData.getPassword());    
		setFullName(profileData.getFullName());      
		setAddress(profileData.getAddress());  
		setEmail(profileData.getEmail());          
		setCreditCard(profileData.getCreditCard());      
		return getDataBean();
	}   


	public AccountProfileDataBean getDataBean()
	{
		return new AccountProfileDataBean(getUserID(),
									getPassword(),
									getFullName(),
									getAddress(),
									getEmail(),
									getCreditCard());
	}
	
	
	public String toString()
	{
		return getDataBean().toString();
	}


    /* Required javax.ejb.EntityBean interface methods */
    public String ejbCreate (String userID, String password, String fullname, 
    							String address, String email, String creditcard)
    throws CreateException {
		
        setUserID(userID);
        setPassword(password);
        setFullName(fullname);
        setAddress(address);
        setEmail(email);
        setCreditCard(creditcard);
        
        return null;
    }

    public void ejbPostCreate (String userID, String password, String fullname, 
    							String address, String email, String creditcard)

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
