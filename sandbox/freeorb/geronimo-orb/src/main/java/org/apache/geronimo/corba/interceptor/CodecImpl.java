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

package org.apache.geronimo.corba.interceptor;

import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.codeset.CharConverter;
import org.apache.geronimo.corba.io.EncapsulationInputStream;
import org.apache.geronimo.corba.io.EncapsulationOutputStream;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.TypeCode;
import org.omg.IOP.Codec;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;

/**
 * @version $Revision$ $Date$
 */
public class CodecImpl extends LocalObject implements Codec {

	Encoding enc;

	ORB orb;

	CodecImpl(ORB orb, Encoding enc) {
		this.orb = orb;
		this.enc = enc;
	}

	/**
	 * @see org.omg.IOP.CodecOperations#encode(Any)
	 */
	public byte[] encode(Any data) throws InvalidTypeForEncoding {
		
		EncapsulationOutputStream out = new EncapsulationOutputStream(orb, getGIOPVersion());

		out.write_any(data);

		return out.getBytes();

	}

	/**
	 * @see org.omg.IOP.CodecOperations#decode(byte[])
	 */
	public Any decode(byte[] data) throws FormatMismatch {
		EncapsulationInputStream in = new EncapsulationInputStream(orb,
				getGIOPVersion(), getCharConverter(), getWCharConverter(),
				data, 0, data.length);

		return in.read_any();
	}

	/**
	 * @see org.omg.IOP.CodecOperations#encode_value(Any)
	 */
	public byte[] encode_value(Any data) throws InvalidTypeForEncoding {
		
		EncapsulationOutputStream out = new EncapsulationOutputStream(orb, getGIOPVersion());

		data.write_value(out);

		return out.getBytes();
	}

	/**
	 * @see org.omg.IOP.CodecOperations#decode_value(byte[], TypeCode)
	 */
	public Any decode_value(byte[] data, TypeCode tc) throws FormatMismatch,
			TypeMismatch {
		EncapsulationInputStream in = new EncapsulationInputStream(orb,
				getGIOPVersion(), getCharConverter(), getWCharConverter(),
				data, 0, data.length);
		Any any = orb.create_any();

		any.read_value(in, tc);

		return any;
	}

	private CharConverter getWCharConverter() {
		return orb.get_wchar_converter(getGIOPVersion());
	}

	private CharConverter getCharConverter() {
		return orb.get_char_converter(getGIOPVersion());
	}

	private GIOPVersion getGIOPVersion() {
		return GIOPVersion.get(enc.major_version, enc.minor_version);
	}

}
