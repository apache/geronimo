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
import org.apache.geronimo.samples.daytrader.*;


public abstract class KeyGenBean
		implements EntityBean {

    private EntityContext context;

    /* Accessor methods for persistent fields */


    public abstract String		getKeyName();				/* Unique Primary Key name */
  	public abstract void		setKeyName(String KeyName);
    public abstract int			getKeyVal();				/* Value for PK */
    public abstract void		setKeyVal(int keyVal);

    /* Accessor methods for relationship fields */
   
    /* Select methods */

    /* Business methods */
    
    public Collection allocBlockOfKeys()
    {
    		int min = getKeyVal();
			int max = min+TradeConfig.KEYBLOCKSIZE;
			setKeyVal(max);
			return new KeyBlock(min, max-1);
    }

    /* Required javax.ejb.EntityBean interface methods */
    public String ejbCreate (String keyName)
    throws CreateException {

        setKeyName(keyName);
		setKeyVal(0);
        return null;
    }


    public void ejbPostCreate (String keyName)          
	throws CreateException {
	}
	    
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