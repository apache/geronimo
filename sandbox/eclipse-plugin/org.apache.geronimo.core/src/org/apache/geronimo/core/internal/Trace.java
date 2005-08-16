/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.geronimo.core.internal;
/**
 * Helper class to route trace output.
 */
public class Trace {
	/**
	 * Config trace event.
	 */
	public static byte CONFIG = 0;

	/**
	 * Warning trace event.
	 */
	public static byte WARNING = 1;

	/**
	 * Severe trace event.
	 */
	public static byte SEVERE = 2;

	/**
	 * Finest trace event.
	 */
	public static byte FINEST = 3;

	/**
	 * Parsing trace event.
	 */
	public static byte PARSING = 4;

	/**
	 * Trace constructor comment.
	 */
	private Trace() {
		super();
	}

	/**
	 * Trace the given text.
	 *
	 * @param level the trace level
	 * @param s a message
	 */
	public static void trace(byte level, String s) {
		trace(level, s, null);
	}

	/**
	 * Trace the given message and exception.
	 *
	 * @param level the trace level
	 * @param s a message
	 * @param t a throwable
	 */
	public static void trace(byte level, String s, Throwable t) {
		if (!GeronimoPlugin.getInstance().isDebugging())
			return;

		System.out.println(GeronimoPlugin.PLUGIN_ID + " " + s);
		if (t != null)
			t.printStackTrace();
	}
}