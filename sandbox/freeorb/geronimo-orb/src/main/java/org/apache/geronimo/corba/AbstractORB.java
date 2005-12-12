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
package org.apache.geronimo.corba;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA_2_3.ORB;
import org.omg.IIOP.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.codeset.CharConverter;
import org.apache.geronimo.corba.codeset.DefaultCharConverter;
import org.apache.geronimo.corba.codeset.DefaultWCharConverter;
import org.apache.geronimo.corba.dii.EnvironmentImpl;
import org.apache.geronimo.corba.dii.ExceptionListImpl;
import org.apache.geronimo.corba.dii.NamedValueImpl;
import org.apache.geronimo.corba.io.GIOPVersion;


/** 
 * This class holds implementations of methods that should be there
 * for both the real orb (class ORB in this package) and the singleton orb
 * (class SingletonORB) in this package.
 * */
public abstract class AbstractORB extends ORB {

	static private Log log = LogFactory.getLog(AbstractORB.class);
	
	protected AbstractORB() {
	}

	public org.omg.CORBA.NamedValue create_named_value(String name,
			org.omg.CORBA.Any value, int flags) {
		return new NamedValueImpl(name, value, flags);
	}

	public org.omg.CORBA.ExceptionList create_exception_list() {
		return new ExceptionListImpl();
	}

	public org.omg.CORBA.Environment create_environment() {
		return new EnvironmentImpl();
	}

	final public org.omg.CORBA.TypeCode create_struct_tc(String id,
			String name, org.omg.CORBA.StructMember[] members) {
		return TypeCodeUtil.create_struct_tc(id, name, members);
	}

	final public org.omg.CORBA.TypeCode create_union_tc(String id, String name,
			org.omg.CORBA.TypeCode discriminator_type,
			org.omg.CORBA.UnionMember[] members) {
		return TypeCodeUtil.create_union_tc(id, name, discriminator_type,
				members);
	}

	final public org.omg.CORBA.TypeCode create_enum_tc(String id, String name,
			String[] members) {
		return TypeCodeUtil.create_enum_tc(id, name, members);
	}

	final public org.omg.CORBA.TypeCode create_alias_tc(String id, String name,
			org.omg.CORBA.TypeCode original_type) {
		return TypeCodeUtil.create_alias_tc(id, name, original_type);
	}

	final public org.omg.CORBA.TypeCode create_exception_tc(String id,
			String name, org.omg.CORBA.StructMember[] members) {
		return TypeCodeUtil.create_exception_tc(id, name, members);
	}

	final public org.omg.CORBA.TypeCode create_interface_tc(String id,
			String name) {
		return TypeCodeUtil.create_interface_tc(id, name);
	}

	final public org.omg.CORBA.TypeCode create_string_tc(int bound) {
		return TypeCodeUtil.create_string_tc(bound);
	}

	final public org.omg.CORBA.TypeCode create_wstring_tc(int bound) {
		return TypeCodeUtil.create_wstring_tc(bound);
	}

	final public org.omg.CORBA.TypeCode create_fixed_tc(short digits,
			short scale) {
		return TypeCodeUtil.create_fixed_tc(digits, scale);
	}

	final public org.omg.CORBA.TypeCode create_sequence_tc(int bound,
			org.omg.CORBA.TypeCode element_type) {
		return TypeCodeUtil.create_sequence_tc(bound, element_type);
	}

	/**
	 * @deprecated
	 */
	final public org.omg.CORBA.TypeCode create_recursive_sequence_tc(int bound,
			int offset) {
		return TypeCodeUtil.create_recursive_sequence_tc(bound, offset);
	}

	final public org.omg.CORBA.TypeCode create_array_tc(int length,
			org.omg.CORBA.TypeCode element_type) {
		return TypeCodeUtil.create_array_tc(length, element_type);
	}

	final public org.omg.CORBA.TypeCode create_value_tc(String id, String name,
			short type_modifier, org.omg.CORBA.TypeCode concrete_base,
			org.omg.CORBA.ValueMember[] members) {
		return TypeCodeUtil.create_value_tc(id, name, type_modifier,
				concrete_base, members);
	}

	final public org.omg.CORBA.TypeCode create_value_box_tc(String id,
			String name, org.omg.CORBA.TypeCode boxed_type) {
		return TypeCodeUtil.create_value_box_tc(id, name, boxed_type);
	}

	final public org.omg.CORBA.TypeCode create_native_tc(String id, String name) {
		return TypeCodeUtil.create_native_tc(id, name);
	}

	final public org.omg.CORBA.TypeCode create_recursive_tc(String id) {
		return TypeCodeUtil.create_recursive_tc(id);
	}

	final public org.omg.CORBA.TypeCode create_abstract_interface_tc(String id,
			String name) {
		return TypeCodeUtil.create_abstract_interface_tc(id, name);
	}

	final public org.omg.CORBA.TypeCode get_primitive_tc(
			org.omg.CORBA.TCKind kind) {
		return TypeCodeUtil.get_primitive_tc(kind);
	}

	public org.omg.CORBA.Any create_any() {
		return new AnyImpl(this);
	}

	public GIOPVersion getGIOPVersion() {
		return GIOPVersion.V1_2;
	}

	public Version getIIOPVersion() {
		GIOPVersion v = getGIOPVersion();
		return new Version((byte) v.major, (byte) v.minor);
	}

	public CharConverter get_char_converter(GIOPVersion version) {
		return DefaultCharConverter.getInstance(version);
	}

	public CharConverter get_wchar_converter(GIOPVersion version) {
		return DefaultWCharConverter.getInstance(version);
	}

	private static final DNSCache DNS_CACHE = new DNSCache();

	public final InetAddress getAddress(String host)
			throws UnknownHostException {
		return DNS_CACHE.getAddress(host);
	}

	public void fatal(String string) {
		log.fatal(string);
	}

	public void fatal(String string, Throwable ex) {
		log.fatal(string, ex);
	}

	public void fatal(Throwable ex) {
		log.fatal("fatal error in GeronimoORB", ex);
	}

	private static ValueHandler valueHandler;

	public javax.rmi.CORBA.ValueHandler getValueHandler() {
		if (valueHandler == null) {
			valueHandler = Util.createValueHandler();
		}
		return valueHandler;
	}

}
