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

public interface LocalAccountProfile extends EJBLocalObject {

    /* Container persisted attributes 
     * 	Note: Commented out methods are internal only and not exposed to EJB clients
     *        For example: The Primary Key value cannot be modified after creation
     *        Also, modification of other Entity attributes may be restricted to internal use
     */

	public String	getUserID();				/* userID */
    //public void		setUserID(String userID);  
    public String	getPassword();				/* password */
    public void		setPassword(String password);
    public String	getFullName();				/* fullName */
    public void		setFullName(String fullName);  
    public String	getAddress();				/* address */
    public void		setAddress(String address);  
    public String	getEmail();					/* email */
    public void		setEmail(String email);  
    public String	getCreditCard();			/* creditCard */
    public void		setCreditCard(String creditCard);  

    /* Accessor methods for relationship fields */
    public LocalAccount getAccount();		/* This profiles account */


    /* Business methods */
	public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData);
    public LocalAccount getAccountForUpdate();		/* This profiles account */
	public AccountProfileDataBean getDataBean();	
	public String toString();	

}
