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
import org.apache.geronimo.samples.daytrader.*;
import java.rmi.*;

public interface Quote extends EJBObject, Remote
{
    /* Container persisted attributes 
     *        Note: Commented out methods are internal only and not exposed to EJB clients
     *        For example: The Primary Key value cannot be modified after creation
     *        Also, modification of other Entity attributes may be restricted to internal use
     */
     
    public String				getSymbol() throws RemoteException;    /* symbol */
    public void				setSymbol(String symbol) throws RemoteException;
    public String				getCompanyName() throws RemoteException;    /* companyName */
    public void				setCompanyName(String companyName) throws RemoteException;
    public double				getVolume() throws RemoteException;    /* volume */
    //public void				setVolume(double volume) throws RemoteException;
    public BigDecimal			getPrice() throws RemoteException;    /* price */
    //public void				setPrice(BigDecimal price) throws RemoteException;
    public BigDecimal			getOpen() throws RemoteException;    /* open price */
    //public void				setOpen(BigDecimal price) throws RemoteException;
    public BigDecimal			getLow() throws RemoteException;    /* low price */
    //public void					setLow(BigDecimal price) throws RemoteException;

    /* high price */
    public BigDecimal			getHigh() throws RemoteException;
    //public void              setHigh(BigDecimal price) throws RemoteException;

     
    /* Change in price */
    /* Note: this can be computed by "current-open"
       but is added here for convenience
     */
    public double       getChange() throws RemoteException;
    //public void              setChange(double change) throws RemoteException;


    /* Relationshiop fields */
    // Cannot be exposed on Remote I/F
    //public abstract Collection		getOrders() throws RemoteException;
    //public abstract void				setOrders(Collection orders) throws RemoteException;


    /* Business methods */
    
    public void updatePrice(BigDecimal price) throws RemoteException;
    public void updatePrice(double price) throws RemoteException;
    
    public void addToVolume(double quantity) throws RemoteException;    

       /* FUTURE This is the approach using ejbSelect methods
        * using Trade Session EJB approach instead


    public Collection getTopGainers(int count)
       throws FinderException throws RemoteException;

    public Collection getTopLosers(int count) 
       throws FinderException throws RemoteException;

    public BigDecimal getTSIA() 
       throws FinderException throws RemoteException;

    public BigDecimal getOpenTSIA() 
       throws FinderException throws RemoteException;
       
        *
        */
       
	public double getTotalVolume()       
		throws FinderException, RemoteException;       

	public QuoteDataBean getDataBean() throws RemoteException;
//	public String toString() throws RemoteException;    
}
