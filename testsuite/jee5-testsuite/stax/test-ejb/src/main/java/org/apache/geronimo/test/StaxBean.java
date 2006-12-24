/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.test;

import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventFactory;

public class StaxBean implements SessionBean {

    private SessionContext sessionContext;

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setSessionContext(SessionContext sessionContext) {
        this.sessionContext = sessionContext;
    }
    
    public List testStax() throws RemoteException {
        List output = new ArrayList();
        try {
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            output.add(inFactory.getClass().getName());
            StaxTest.testParse(inFactory);
            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            output.add(outFactory.getClass().getName());
            StaxTest.testStreamGenerate(outFactory);
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            output.add(eventFactory.getClass().getName());
            StaxTest.testEventGenerate(outFactory, eventFactory);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Error", e);
        }
        
        return output;
    }

}
