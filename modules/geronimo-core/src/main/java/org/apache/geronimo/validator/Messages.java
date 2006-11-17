/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.validator;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class Messages {
	static private Hashtable bundles = new Hashtable();
	static private Hashtable rbFormats = new Hashtable();
	static private Locale globalLocale;

	private ResourceBundle messages;
	private Hashtable formats;
	private Locale locale;
	private String resourceName;

	public Messages(String resourceName) {
		synchronized (Messages.class) {
			locale = globalLocale;
			this.resourceName = resourceName + ".Messages";

			ResourceBundle rb = (ResourceBundle) bundles.get(this.resourceName);
			if (rb == null) {
				init();  // TODO Remove lazy call to init
			} else {
				messages = rb;
				formats = (Hashtable) rbFormats.get(this.resourceName);
			}
		}

	}

	protected void init() {
		try {
			if (locale == null)
				messages = ResourceBundle.getBundle(resourceName);
			else
				messages = ResourceBundle.getBundle(resourceName, locale);
		} catch (Exception except) {
			messages = new EmptyResourceBundle();
		}

		formats = new Hashtable();

		bundles.put(resourceName, messages);
		rbFormats.put(resourceName, formats);
	}

	public String format(String message, Object arg1) {
		return format(message, new Object[] { arg1 });
	}

	public String format(String message, Object arg1, Object arg2) {
		return format(message, new Object[] { arg1, arg2 });
	}

	public String format(String message, Object arg1, Object arg2, Object arg3) {
		return format(message, new Object[] { arg1, arg2, arg3 });
	}

	public String format(String message, Object arg1, Object arg2, Object arg3, Object arg4) {
		return format(message, new Object[] { arg1, arg2, arg3, arg4 });
	}

	public String format(String message, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return format(message, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}

	public String format(String message) {
		return message(message);
	}

	public String format(String message, Object[] args) {
		if (locale != globalLocale) {
			synchronized (Messages.class) {
				init();  // TODO Remove lazy call to init
			}
		}

		MessageFormat mf;
		String msg;

		try {
			mf = (MessageFormat) formats.get(message);
			if (mf == null) {
				try {
					msg = messages.getString(message);
				} catch (MissingResourceException except) {
					return message;
				}
				mf = new MessageFormat(msg);
				formats.put(message, mf);
			}
			return mf.format(args);
		} catch (Exception except) {
			return "An internal error occured while processing message " + message;
		}
	}

	public String message(String message) {
		if (locale != globalLocale) {
			synchronized (Messages.class) {
				init();
			}
		}

		try {
			return messages.getString(message);
		} catch (MissingResourceException except) {
			return message;
		}
	}

	static public void setLocale(Locale locale) {
		synchronized (Messages.class) {
			globalLocale = locale;
			bundles = new Hashtable();
			rbFormats = new Hashtable();
		}
	}

	static {
		setLocale(Locale.getDefault());
	}

	private static final class EmptyResourceBundle extends ResourceBundle implements Enumeration {

		public Enumeration getKeys() {
			return this;
		}

		protected Object handleGetObject(String name) {
			return "[Missing message " + name + "]";
		}

		public boolean hasMoreElements() {
			return false;
		}

		public Object nextElement() {
			return null;
		}

	}

}
