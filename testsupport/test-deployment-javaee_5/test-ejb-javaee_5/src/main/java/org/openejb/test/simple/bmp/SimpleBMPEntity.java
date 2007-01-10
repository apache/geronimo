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

package org.openejb.test.simple.bmp;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public interface SimpleBMPEntity extends EJBObject {
    String getName() throws RemoteException;
    void setName(String name) throws RemoteException;
    String getValue() throws RemoteException;
    void setValue(String value) throws RemoteException;
}
