/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.concurrent.impl.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.transaction.GeronimoUserTransaction;
import org.apache.xbean.naming.context.ContextFlyweight;
import org.apache.xbean.naming.context.ContextUtil;
import org.apache.xbean.naming.reference.SimpleReference;

/**
 * A wrapper around an existing java:comp/ context that exposes
 * UserTransaction entry. 
 */
public class UserTransactionContext extends ContextFlyweight {

    private static final String USER_TRANSACTION = "UserTransaction";
    
    private Context componentContext;

    public UserTransactionContext(Context componentContext) {
        this.componentContext = componentContext;
    }
    
    public static boolean hasUserTransaction(Context componentContext) {  
        UserTransaction userTransaction = getUserTransaction(componentContext);
        return (userTransaction != null);
    }
    
    public static UserTransaction getUserTransaction(Context componentContext) {
        try {
            return (UserTransaction)componentContext.lookup(USER_TRANSACTION);
        } catch (NamingException e) {
            return null;
        } 
    }
    
    protected Context getContext() throws NamingException {
        return this.componentContext;
    }
   
    public Object lookup(Name name) throws NamingException {
        if (name != null && name.size() == 1 && name.get(0).equals(USER_TRANSACTION)) {
            return getUserTransaction();
        } else {
            return super.lookup(name);
        }
    }

    public Object lookup(String name) throws NamingException {
        if (name != null && name.equals(USER_TRANSACTION)) {
            return getUserTransaction();
        } else {
            return super.lookup(name);
        }
    }
    
    public NamingEnumeration list(String name) throws NamingException {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return list(getNameParser(name).parse(name));
    }
    
    public NamingEnumeration list(Name name) throws NamingException {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
       
        if (name.isEmpty()) {
            return list();
        }
        
        Object target = null;
        try {
            target = super.lookup(name);
        } catch (NamingException e) {
            throw new NotContextException(name.toString());
        }

        if (target == this) {
            return list();
        } else if (target instanceof Context) {
            return ((Context) target).list("");
        } else {
            throw new NotContextException("The name " + name + " cannot be listed");
        }
    }
    
    public NamingEnumeration listBindings(String name) throws NamingException {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return listBindings(getNameParser(name).parse(name));
    }
    
    public NamingEnumeration listBindings(Name name) throws NamingException {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        
        if (name.isEmpty()) {
            return listBindings();
        }
        
        Object target = null;
        try {
            target = super.lookup(name);
        } catch (NamingException e) {
            throw new NotContextException(name.toString());
        }

        if (target == this) {
            return listBindings();
        } else if (target instanceof Context) {
            return ((Context) target).listBindings("");
        } else {
            throw new NotContextException("The name " + name + " cannot be listed");
        }
    }
        
    private NamingEnumeration listBindings() throws NamingException {
        NamingEnumeration e = super.listBindings("");
        List<Binding> list = new ArrayList<Binding>();
        while(e.hasMore()) {
            Binding binding = (Binding)e.next();
            list.add(binding);
        }
        list.add(new ContextUtil.ReadOnlyBinding(USER_TRANSACTION, new UserTransactionReference(), this));
        return new ListNamingEnumeration(list.iterator());
    }
    
    private NamingEnumeration list() throws NamingException {
        NamingEnumeration e = super.list("");
        List<NameClassPair> list = new ArrayList<NameClassPair>();
        while(e.hasMore()) {
            NameClassPair nameClassPair = (NameClassPair)e.next();
            list.add(nameClassPair);
        }
        list.add(new NameClassPair(USER_TRANSACTION, UserTransaction.class.getName()));
        return new ListNamingEnumeration(list.iterator());
    }
    
    private Object getUserTransaction() throws NamingException {
        return new GeronimoUserTransaction(TransactionContextHandler.getTransactionManager());
    }
    
    private class UserTransactionReference extends SimpleReference {
        
        public Object getContent() throws NamingException {
            return getUserTransaction();
        }
        
        public String getClassName() {
            return UserTransaction.class.getName();
        }
        
    }
    
    private static class ListNamingEnumeration implements NamingEnumeration {

        private Iterator iter;
        
        public ListNamingEnumeration(Iterator iter){
            this.iter = iter;
        }
        
        public void close() throws NamingException {
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public Object next() throws NamingException {
            return nextElement();
        }
        
        public boolean hasMoreElements() {
            return this.iter.hasNext();
        }
            
        public Object nextElement() {
            return this.iter.next();
        }
        
    }
    
}
