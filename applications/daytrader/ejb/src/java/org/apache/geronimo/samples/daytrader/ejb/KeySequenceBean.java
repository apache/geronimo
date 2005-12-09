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
import java.util.HashMap;

public class KeySequenceBean implements SessionBean {

	private SessionContext  context = null;
	private LocalKeyGenHome keyGenHome = null;
	private HashMap 		keyMap = null;
	
    /* Business methods */
    
    public Integer getNextID(String keyName)
    {

		// First verify we have allocated a block of keys 
		// for this key name
		// Then verify the allocated block has not been depleted
		// allocate a new block if necessary
		if ( keyMap.containsKey(keyName) == false)
			allocNewBlock(keyName);
		Collection block = 	(Collection) keyMap.get(keyName);
		Iterator ids = block.iterator();
		if ( ids.hasNext() == false )
			ids = allocNewBlock(keyName).iterator();
		//get and return a new unique key
		Integer nextID = (Integer) ids.next();
		if (Log.doTrace()) Log.trace("KeySequenceBean:getNextID - return new PK ID for Entity type: " + keyName + " ID=" + nextID);
		return nextID;
	}

	private Collection allocNewBlock(String keyName)  
	{
		try 
		{
			
			LocalKeyGen keyGen = null;
			try
			{
				keyGen = keyGenHome.findByPrimaryKeyForUpdate(keyName);
			}
			catch (javax.ejb.ObjectNotFoundException onfe )
			{
				// No keys found for this name - create a new one
				keyGen = keyGenHome.create(keyName);
			}
			Collection block = keyGen.allocBlockOfKeys();
			keyMap.put(keyName, block);
			return block;			
		}
		catch (Exception e)
		{
			Log.error(e, "KeySequence:allocNewBlock - failure to allocate new block of keys for Entity type: "+ keyName);
			throw new EJBException(e);
		}
	}

    
    /* Required javax.ejb.SessionBean interface methods */

	public KeySequenceBean() {
	}

	public void ejbCreate() throws CreateException {
		if  ( keyGenHome == null) 
		{
			String error = "KeySequenceBean:ejbCreate()  JNDI lookup of KeyGen Home failed\n" +
					"\n\t keyGenHome="+keyGenHome;
			Log.error(error);
			throw new EJBException(error);
		}
	}

	public void ejbRemove() {
	}
	public void ejbActivate() {
	}
	public void ejbPassivate() {
	}

	public void setSessionContext(SessionContext sc) {
		try {
			context = sc;

			InitialContext ic = new InitialContext();
			keyGenHome 		= (LocalKeyGenHome)  ic.lookup("java:comp/env/ejb/KeyGen");
			keyMap = new HashMap();
		
		} catch (NamingException ne) {
			Log.error("KeySequenceEJB: Lookup of Local KeyGen Home Failed\n", ne);
			throw new EJBException("KeySequenceEJB: Lookup of Local KeyGen Home Failed\n", ne);
		}
	}
}
