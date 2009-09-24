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

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.geronimo.corba.compiler.other.Generic$Interface;
import org.apache.geronimo.corba.compiler.other._Something;
import org.apache.geronimo.corba.compiler.other.inout;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public interface Special extends Remote {
    // J_underscore
    public void _underscore() throws RemoteException;

    public void _underscoreOverload() throws RemoteException;
    public void _underscoreOverload(_Something x) throws RemoteException;
    public void _underscoreOverload(_Something[] x) throws RemoteException;

    // special characters
    public void dollar$() throws RemoteException;
    public void $dollar() throws RemoteException;

    // unicode chars in method name doesn't work in rmic either
    // although the spec says it's legal

    // innerclass
    public void innerClass(Generic$Interface.Generic$InnerClass x, int y) throws RemoteException;
    public void innerClass(Generic$Interface.Generic$InnerClass x[], int y) throws RemoteException;

    // class collision
    public void special() throws RemoteException;

    // difer by case only
    public void differByCase() throws RemoteException;
    public void differByCASE() throws RemoteException;
    public void differByCaseOverload() throws RemoteException;
    public void differByCASEOverload() throws RemoteException;
    public void differByCASEOverload(int x) throws RemoteException;

    // keywords
    public void keyword() throws RemoteException;
    public void keyword(inout x) throws RemoteException;
    public void ABSTRACT() throws RemoteException;
    public void ABSTRACT(int x) throws RemoteException;

    public void any() throws RemoteException;
    public void attribute() throws RemoteException;
    public void BOOLEAN() throws RemoteException;
    public void CASE() throws RemoteException;
    public void CHAR() throws RemoteException;
    public void CONST() throws RemoteException;
    public void context() throws RemoteException;
    public void custom() throws RemoteException;
    public void DEFAULT() throws RemoteException;
    public void DOUBLE() throws RemoteException;
//    public void enum() throws RemoteException;
    public void exception() throws RemoteException;
    public void factory() throws RemoteException;
    public void FALSE() throws RemoteException;
    public void fixed() throws RemoteException;
    public void FLOAT() throws RemoteException;
    public void in() throws RemoteException;
    public void inout() throws RemoteException;
    public void INTERFACE() throws RemoteException;
    public void LONG() throws RemoteException;
    public void module() throws RemoteException;
    public void NATIVE() throws RemoteException;
    public void OBJECT() throws RemoteException;
    public void octet() throws RemoteException;
    public void oneway() throws RemoteException;
    public void out() throws RemoteException;
    public void PRIVATE() throws RemoteException;
    public void PUBLIC() throws RemoteException;
    public void raises() throws RemoteException;
    public void readonly() throws RemoteException;
    public void sequence() throws RemoteException;
    public void SHORT() throws RemoteException;
    public void string() throws RemoteException;
    public void struct() throws RemoteException;
    public void supports() throws RemoteException;
    public void SWITCH() throws RemoteException;
    public void TRUE() throws RemoteException;
    public void truncatable() throws RemoteException;
    public void typedef() throws RemoteException;
    public void union() throws RemoteException;
    public void unsigned() throws RemoteException;
    public void ValueBase() throws RemoteException;
    public void valuetype() throws RemoteException;
    public void VOID() throws RemoteException;
    public void wchar() throws RemoteException;
    public void wstring() throws RemoteException;
}
