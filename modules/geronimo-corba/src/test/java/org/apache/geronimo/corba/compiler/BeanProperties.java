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
import org.apache.geronimo.corba.compiler.other.Generic$Interface;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public interface BeanProperties extends Remote {
    public void setBooleanabcdef(boolean x) throws RemoteException;
    public void setCharabcdef(char x) throws RemoteException;
    public void setByteabcdef(byte x) throws RemoteException;
    public void setIntabcdef(int x) throws RemoteException;
    public void setLongabcdef(long x) throws RemoteException;
    public void setFloatabcdef(float x) throws RemoteException;
    public void setDoubleabcdef(double x) throws RemoteException;
    public void setBigDecimalabcdef(BigDecimal x) throws RemoteException;
    public void setClassObjectabcdef(Class x) throws RemoteException;
    public void setCORBA_Objectabcdef(org.omg.CORBA.Object x) throws RemoteException;
    public void setCORBA_Anyabcdef(org.omg.CORBA.Any x) throws RemoteException;
    public void setCORBA_TypeCodeabcdef(org.omg.CORBA.TypeCode x) throws RemoteException;
    public void setCheeseIDLEntityabcdef(CheeseIDLEntity x) throws RemoteException;
    public void setGenericInterfaceabcdef(Generic$Interface x) throws RemoteException;
    public void setBlahExceptionabcdef(BlahEx x) throws RemoteException;
    public void setBooExceptionabcdef(BooException x) throws RemoteException;

    public boolean isBooleanabcdef() throws RemoteException;
    public char getCharabcdef() throws RemoteException;
    public byte getByteabcdef() throws RemoteException;
    public int getIntabcdef() throws RemoteException;
    public long getLongabcdef() throws RemoteException;
    public float getFloatabcdef() throws RemoteException;
    public double getDoubleabcdef() throws RemoteException;
    public BigDecimal getBigDecimalabcdef() throws RemoteException;
    public Class getClassObjectabcdef() throws RemoteException;
    public org.omg.CORBA.Object getCORBA_Objectabcdef() throws RemoteException;
    public org.omg.CORBA.Any getCORBA_Anyabcdef() throws RemoteException;
    public org.omg.CORBA.TypeCode getCORBA_TypeCodeabcdef() throws RemoteException;
    public CheeseIDLEntity getCheeseIDLEntityabcdef() throws RemoteException;
    public Generic$Interface getGenericInterfaceabcdef() throws RemoteException;
    public BlahEx getBlahExceptionabcdef() throws RemoteException;
    public BooException getBooExceptionabcdef() throws RemoteException;


    // special
    public int getWithArgumentabcdef(int x) throws RemoteException;

    public int getWithSetReturningabcdef() throws RemoteException;
    public int setWithSetReturningabcdef(int x) throws RemoteException;

    public int getWithSetOfDifferentTypeabcdef() throws RemoteException;
    public void setWithSetOfDifferentTypeabcdef(long x) throws RemoteException;

    public int getThrowsUserExceptionabcdef() throws RemoteException, Exception;
    public void setThrowsUserExceptionabcdef(int x) throws RemoteException, Exception;

    public int getOverridenSetabcdef() throws RemoteException;
    public void setOverridenSetabcdef(int x) throws RemoteException;
    public void setOverridenSetabcdef(long x) throws RemoteException;

    public void setOnlyabcdef(int x) throws RemoteException;

    public int getOverridenGetabcdef() throws RemoteException;
    public int getOverridenGetabcdef(int x) throws RemoteException;

    public int getUPPERCASEabcdef() throws RemoteException;

    public int get() throws RemoteException;

    public int get_collisionabcdef() throws RemoteException;

    public int getA() throws RemoteException;
}
