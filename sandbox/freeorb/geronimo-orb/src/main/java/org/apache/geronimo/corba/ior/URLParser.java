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

package org.apache.geronimo.corba.ior;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.ClientDelegate;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.io.EncapsulationInputStream;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.util.HexUtil;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.IOP.IOR;
import org.omg.IOP.TaggedProfile;

public class URLParser {
	
	static Log log = LogFactory.getLog(URLParser.class);

	protected InternalIOR ior;

	protected ORB orb;

	InternalIOR getIOR() {
		return ior;
	}

	int schemeEnd;

	private String getScheme(String url) {
		schemeEnd = url.indexOf(':');
		if (schemeEnd == -1)
			throw new org.omg.CORBA.BAD_PARAM(url);

		return url.substring(0, schemeEnd).toLowerCase();
	}

	URLParser(ORB orb, String url) {
		String scheme = getScheme(url);
		this.orb = orb;

		if (log.isDebugEnabled()) {
			log.debug("resolving " + url);
		}

		if (scheme.equals("ior"))
			parseIOR(url);

		else if (scheme.equals("iiop"))
			parseIIOP(url);

		else if (scheme.equals("corbaloc"))
			parseCORBALOC(url);

		else if (scheme.equals("corbaname"))
			parseCORBANAME(url);

		else
			throw new org.omg.CORBA.BAD_PARAM(url);
	}

	private void parseIOR(String url) {
		String hexString = url.substring(schemeEnd + 1);
		byte[] data = HexUtil.hexToByteArray(hexString);
		
		InputStreamBase in = new EncapsulationInputStream(orb, data);
		IOR ior = org.omg.IOP.IORHelper.read(in);
		this.ior = new InternalIOR(orb, ior);
	}

	private void parseIIOP(String url) {
		if (!url.startsWith("iiop://"))
			throw new org.omg.CORBA.BAD_PARAM();

		int idx = url.indexOf('/', 7);
		String hostAndPort = null;
		String name = "";
		if (idx == -1) {
			hostAndPort = url.substring(7);
		} else {
			hostAndPort = url.substring(7, idx);
			name = url.substring(idx + 1);
		}

		if (hostAndPort.length() == 0 || hostAndPort.charAt(0) == ':') {
			hostAndPort = orb.getLocalHost() + hostAndPort;
		}

		if (hostAndPort.indexOf(':') == -1) {
			hostAndPort = hostAndPort + ":2809";
		}

		if (name.equals("")) {
			parseCORBALOC("corbaloc:iiop:" + hostAndPort + "/NameService");
			return;
		}

		org.omg.CORBA.Object nsRef = orb.string_to_object("corbaloc:iiop:"
				+ hostAndPort + "/NameService");

		org.omg.CosNaming.NamingContext ns = org.omg.CosNaming.NamingContextHelper
				.narrow(nsRef);

		String[] names = name.split("/");
		org.omg.CosNaming.NameComponent[] nc = new org.omg.CosNaming.NameComponent[names.length];

		for (int i = 0; i < names.length; i++) {
			nc[i] = new org.omg.CosNaming.NameComponent(names[i], "");
		}

		try {
			ObjectImpl obj = (ObjectImpl) ns.resolve(nc);
			ClientDelegate delegate = (ClientDelegate) obj._get_delegate();

			ior = delegate.getIOR();
		} catch (Exception ex) {
			throw new org.omg.CORBA.BAD_PARAM(ex.getMessage());
		}
	}

	private void parseCORBALOC(String url) {
		if (!url.startsWith("corbaloc:")) {
			throw new org.omg.CORBA.BAD_PARAM("invalid url");
		}

		byte[] keyData;
		String obj_addr_list;
		int idx = url.indexOf('/');
		if (idx != -1) {
			keyData = decode(url.substring(idx + 1));
			obj_addr_list = url.substring("corbaloc:".length(), idx);
		} else {
			obj_addr_list = url.substring("corbaloc:".length());
			keyData = new byte[0];
		}

		String[] obj_addrs = obj_addr_list.split(",");

		IIOPProfile[] profiles = new IIOPProfile[obj_addrs.length];

		for (int profile_idx = 0; profile_idx < obj_addrs.length; profile_idx++) {
			String obj_addr = obj_addrs[profile_idx];
			String prot;
			String iiop_addr;
			if (obj_addr.startsWith(":")) {
				prot = "iiop";
				iiop_addr = obj_addr.substring(1);
			} else if (obj_addr.startsWith("iiop:")) {
				prot = "iiop";
				iiop_addr = obj_addr.substring(5);
			} else {
				throw new org.omg.CORBA.BAD_PARAM("unknown protocol "
						+ obj_addr);
			}

			int major = 1;
			int minor = 0;
			String host;
			short port = 2809;

			idx = iiop_addr.indexOf('@');
			if (idx != -1) {
				String version = iiop_addr.substring(0, idx);
				iiop_addr = iiop_addr.substring(idx + 1);

				idx = version.indexOf('.');
				if (idx == -1) {
					throw new org.omg.CORBA.BAD_PARAM(
							"invalid url, version has no '.'");
				}

				try {
					major = Integer.parseInt(version.substring(0, idx));
					minor = Integer.parseInt(version.substring(idx + 1));
				} catch (NumberFormatException ex) {
					throw new org.omg.CORBA.BAD_PARAM(
							"invalid url, cannot parse version " + version);
				}
			}

			org.omg.IIOP.Version version = new org.omg.IIOP.Version(
					(byte) major, (byte) minor);

			idx = iiop_addr.indexOf(':');
			if (idx != -1) {
				try {
					port = Short.parseShort(iiop_addr.substring(idx + 1));
				} catch (NumberFormatException ex) {
					throw new org.omg.CORBA.BAD_PARAM(
							"invalid url, cannot parse port "
									+ iiop_addr.substring(idx + 1));
				}

				host = iiop_addr.substring(0, idx);
			} else {
				host = iiop_addr;
			}
			
			try {
				profiles[profile_idx] = new IIOPProfile(orb, version, orb
						.getAddress(host), port, keyData);
			} catch (UnknownHostException e) {
				throw new org.omg.CORBA.BAD_PARAM("cannot decode "+host);
			}

		}

		ior = new InternalIOR(orb, "IDL:omg.org/CORBA/Object:1.0", profiles);

		
	}

	public static byte[] decode(String s)
	/* throws UnsupportedEncodingException */{

		StringBuffer sb = null;
		int length = s.length();

		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);

			switch (c) {
			case '+':
				if (sb == null) {
					sb = new StringBuffer();
					for (int j = 0; j < i; j++) {
						sb.append(s.charAt(j));
					}
				}
				sb.append(' ');
				break;

			case '%':

				if (sb == null) {
					sb = new StringBuffer();
					for (int j = 0; j < i; j++) {
						sb.append(s.charAt(j));
					}
				}

				try {
					sb.append(Byte.parseByte(s.substring(i + 1, i + 3), 16));
				} catch (NumberFormatException ex) {
					throw new org.omg.CORBA.BAD_PARAM(
							"invalid url, cannot parse "
									+ s.substring(i + 1, i + 3));
				}

				i += 2;
				break;
			default:
				if (sb != null) {
					sb.append(c);
				}
				break;
			}
		}

		if (sb != null) {
			 s = sb.toString();
		}

		try {
			return s.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new INTERNAL("unsupported encoding");
		}
	}


	private void parseCORBANAME(String url) {
		if (!url.startsWith("corbaname:")) {
			throw new org.omg.CORBA.BAD_PARAM("invalid url");
		}

		int idx = url.indexOf('#');
		String string_name = "";
		String corbaloc_obj;
		if (idx == -1) {
			corbaloc_obj = url.substring("corbaname:".length());
		} else {
			corbaloc_obj = url.substring("corbaname:".length(), idx);
			string_name = url.substring(idx + 1);
		}

		if (corbaloc_obj.indexOf('/') == -1) {
			corbaloc_obj += "/NameService";
		}

		org.omg.CORBA.Object nsRef = orb.string_to_object("corbaloc:"
				+ corbaloc_obj);

		org.omg.CosNaming.NamingContext ns = org.omg.CosNaming.NamingContextHelper
				.narrow(nsRef);

		try {
			string_name = URLDecoder.decode(string_name, "iso-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new INTERNAL("unknown encoding iso-8859-1");
		}

		String[] names = string_name.split("/");
		org.omg.CosNaming.NameComponent[] nc = new org.omg.CosNaming.NameComponent[names.length];

		for (int i = 0; i < names.length; i++) {
			nc[i] = new org.omg.CosNaming.NameComponent(names[i], "");
		}

		try {
			ObjectImpl obj = (ObjectImpl) ns.resolve(nc);
			ClientDelegate delegate = (ClientDelegate) obj._get_delegate();

			ior = delegate.getIOR();
		} catch (org.omg.CosNaming.NamingContextPackage.NotFound ex) {
			throw new org.omg.CORBA.OBJECT_NOT_EXIST(url);

		} catch (org.omg.CORBA.SystemException ex) {
			// ex.printStackTrace ();
			throw ex;

		} catch (Exception ex) {
			// ex.printStackTrace ();
			throw new org.omg.CORBA.BAD_PARAM(ex.getMessage());
		}

	}

}
