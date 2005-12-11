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
import org.omg.CORBA.LocalObject;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;


/**
 * @version $Revision: $ $Date: $
 */
public class CodecFactoryImpl extends LocalObject implements CodecFactory {

	ORB orb;
	Codec giop_1_0_codec = null;
	Codec giop_1_1_codec = null;
	Codec giop_1_2_codec = null;

	public CodecFactoryImpl(ORB orb) {
		this.orb = orb;
	}

	/**
	 * @see org.omg.IOP.CodecFactoryOperations#create_codec(Encoding)
	 */
	public Codec create_codec(Encoding enc) throws UnknownEncoding {
		
		if (enc.format != ENCODING_CDR_ENCAPS.value) {
			throw new UnknownEncoding("only CDR Encapsulation supported");	
		}
		
		if (enc.major_version != 1) {
			throw new UnknownEncoding("Only GIOP major version 1 supported");	
		}
		
		switch (enc.minor_version) {
			case 0:
				if (giop_1_0_codec == null) {
					giop_1_0_codec = new CodecImpl (orb, enc);	
				}		
				
				return giop_1_0_codec;
				
			case 1:
				if (giop_1_1_codec == null) {
					giop_1_1_codec = new CodecImpl (orb, enc);	
				}		
				
				return giop_1_1_codec;

			case 2:
				if (giop_1_2_codec == null) {
					giop_1_2_codec = new CodecImpl (orb, enc);	
				}		
				
				return giop_1_2_codec;

			default:
				throw new UnknownEncoding("Only GIOP minor versions 0, 1, and 2 supported");	
					
		}
		
	}

}
