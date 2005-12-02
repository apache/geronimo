/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.test;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

public class HelloObjectServant extends HelloObjectPOA {

	public void hello0() {
		System.err.println("INSIDE HELLO0");;
	}

	public void hello1(short s) {
		System.err.println("INSIDE HELLO1 "+Integer.toHexString(s));;
	}

	public void hello2(int l) {
		System.err.println("INSIDE HELLO2 "+Integer.toHexString(l));;
	}

	public void hello3(String s) {
		// TODO Auto-generated method stub

	}

	public void hello4(String s) {
		// TODO Auto-generated method stub

	}

	public short hello10() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int hello11() {
		System.err.println("INSIDE HELLO 11");
		// TODO Auto-generated method stub
		return 0xcafebaba;
	}

	public String hello12() {
		// TODO Auto-generated method stub
		return "abc";
	}

	public String hello13() {
		// TODO Auto-generated method stub
		return "abc";
	}


}
