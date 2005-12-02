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
package org.apache.geronimo.corba.giop;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.ior.InternalExceptionDetailMessage;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.util.StringUtil;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;

public class GIOPHelper {

	static Log log = LogFactory.getLog(GIOPHelper.class);

	static SystemException unmarshalSystemException(
			InternalServiceContextList scl, InputStream in) {

		InternalExceptionDetailMessage msg = InternalExceptionDetailMessage
				.get(scl);

		String id = in.read_string();
		int minor = in.read_ulong();
		org.omg.CORBA.CompletionStatus status = org.omg.CORBA.CompletionStatusHelper
				.read(in);

		String className = idToClass(id);
		try {
			Class c = classForName(className);

			Constructor cons = null;
			try {
				cons = c.getConstructor(new Class[] { String.class, int.class,
						CompletionStatus.class });
			} catch (NoSuchMethodException e) {
			}
			SystemException ex = null;

			if (cons == null) {
				ex = (SystemException) c.newInstance();
			} else {
				ex = (SystemException) cons.newInstance(new Object[] {
						msg == null ? "" : msg.getMessage(),
						new Integer(minor), status });
			}

			ex.minor = minor;
			ex.completed = status;

			return ex;
		} catch (ClassNotFoundException ex) {
			// ignore //
		} catch (InstantiationException ex) {
			// ignore //
		} catch (IllegalAccessException ex) {
			// ignore //
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new org.omg.CORBA.UNKNOWN(id, minor, status);
	}

	public static Class classForName(String name) throws ClassNotFoundException {
		return Class.forName(name, true, getContextClassLoader());
	}

	static ClassLoader getContextClassLoader() {
		if (System.getSecurityManager() == null) {
			return Thread.currentThread().getContextClassLoader();
		} else {
			return (ClassLoader) AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return Thread.currentThread()
									.getContextClassLoader();
						}
					});
		}
	}

	public static String idToClass(String repid) {
		// debug
		if (log.isDebugEnabled())
			log.debug("idToClass " + repid);

		if (repid.startsWith("IDL:")) {

			String id = repid;

			try {
				int end = id.lastIndexOf(':');
				String s = end < 0 ? id.substring(4) : id.substring(4, end);

				StringBuffer bb = new StringBuffer();

				//
				// reverse order of dot-separated name components up
				// till the first slash.
				//
				int firstSlash = s.indexOf('/');
				if (firstSlash > 0) {
					String prefix = s.substring(0, firstSlash);
					String[] elems = StringUtil.split(prefix, '.');

					for (int i = elems.length - 1; i >= 0; i--) {
						bb.append(fixName(elems[i]));
						bb.append('.');
					}

					s = s.substring(firstSlash + 1);
				}

				//
				// Append slash-separated name components ...
				//
				String[] elems = StringUtil.split(s, '/');
				for (int i = 0; i < elems.length; i++) {
					bb.append(fixName(elems[i]));
					if (i != elems.length - 1)
						bb.append('.');
				}

				String result = bb.toString();

				if (log.isDebugEnabled()) {
					log.debug("idToClassName " + repid + " => " + result);
				}

				return result;
			} catch (IndexOutOfBoundsException ex) {
				log.error("idToClass", ex);
				return null;
			}

		} else if (repid.startsWith("RMI:")) {
			int end = repid.indexOf(':', 4);
			return end < 0 ? repid.substring(4) : repid.substring(4, end);
		}

		return null;
	}

	static String fixName(String name) {
		if (keyWords.contains(name)) {
			StringBuffer buf = new StringBuffer();
			buf.append('_');
			buf.append(name);
			return buf.toString();
		}

		String result = name;
		String current = name;

		boolean match = true;
		while (match) {

			int len = current.length();
			match = false;

			for (int i = 0; i < reservedPostfixes.length; i++) {
				if (current.endsWith(reservedPostfixes[i])) {
					StringBuffer buf = new StringBuffer();
					buf.append('_');
					buf.append(result);
					result = buf.toString();

					int resultLen = reservedPostfixes[i].length();
					if (len > resultLen)
						current = current.substring(0, len - resultLen);
					else
						current = new String("");

					match = true;
					break;
				}
			}

		}

		return name;
	}

	static final java.util.Set keyWords = new java.util.HashSet();

	static final String[] reservedPostfixes = new String[] { "Helper",
			"Holder", "Operations", "POA", "POATie", "Package", "ValueFactory" };

	static {
		String[] words = { "abstract", "boolean", "break", "byte", "case",
				"catch", "char", "class", "clone", "const", "continue",
				"default", "do", "double", "else", "equals", "extends",
				"false", "final", "finalize", "finally", "float", "for",
				"getClass", "goto", "hashCode", "if", "implements", "import",
				"instanceof", "int", "interface", "long", "native", "new",
				"notify", "notifyAll", "null", "package", "private",
				"protected", "public", "return", "short", "static", "super",
				"switch", "synchronized", "this", "throw", "throws",
				"toString", "transient", "true", "try", "void", "volatile",
				"wait", "while" };

		for (int i = 0; i < words.length; i++) {
			keyWords.add(words[i]);
		}
	}

}
