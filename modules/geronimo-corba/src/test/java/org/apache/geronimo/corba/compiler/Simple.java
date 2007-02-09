/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.compiler;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.geronimo.corba.compiler.other.BlahEx;
import org.apache.geronimo.corba.compiler.other.CheeseIDLEntity;
import org.apache.geronimo.corba.compiler.other.Donkey;
import org.apache.geronimo.corba.compiler.other.DonkeyEx;
import org.apache.geronimo.corba.compiler.other.Generic$Interface;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public interface Simple extends Remote, Special {
    public void invoke(boolean x0,
            char x1,
            byte x2,
            int x3,
            long x4,
            float x5,
            double x6,
            BigDecimal x7,
            Class x8,
            org.omg.CORBA.Object x9,
            org.omg.CORBA.Any x10,
            org.omg.CORBA.TypeCode x11,
            CheeseIDLEntity x12,
            Generic$Interface x13,
            BlahEx x14,
            BooException x15) throws RemoteException, RemoteException, BlahEx, BooException, DonkeyEx, Donkey;

    public int invokeInt() throws RemoteException;
    public String invokeString() throws RemoteException;
    public Generic$Interface invokeGeneric$Interface() throws RemoteException;
    public Foo invokeFoo() throws RemoteException;

}
