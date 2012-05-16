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
package org.apache.geronimo.crypto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import org.apache.geronimo.crypto.ConfiguredEncryption;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A static class that uses registered Encryption instances to encypt and decrypt objects, typically strings.
 * The encrypted strings are preceded by the name of the Encryption object, such as {Simple}, followed by the base64
 * encoded encrypted bytes.
 *
 * Any number of Encryption instances can be registered but only the first to be explicitly registered will be used.
 * However, when decrypting the Encryption instance is looked up from the name prefix so may be any registered Encryption instance.
 * Furthermore, encrypt and decrypt are idempotent.  Calling encrypt on a string encrypted with a registered Encryption that is not
 * the one in use will decrypt the string and re-encrypt it with the one in use.  This can be useful when changing Encryption.
 *
 * The default Encryption instance (that does not need to be registered) is SimpleEncryption which uses a fixed key hardcoded into
 * the Encryption class itself.  Thus it is useful only to hide information from those who don't read code.  On the other hand
 * you can't lose the key and make your server permanently unusable.
 *
 * n.b. calling these methods idempotent is a slight exageration as this would apply only if all arguments and return values are Strings.
 *
 * @version $Rev$ $Date$
 */
public class EncryptionManager {

	private static final Map<String, Encryption> ENCRYPTORS = Collections.synchronizedMap(new HashMap<String, Encryption>());
    private final static String SIMPLE_ENCRYPTION_PREFIX = "{Simple}";
	private final static String CONFIGURED_ENCRYPTION_PREFIX = "{Configured}";
	private final static Log log = LogFactory.getLog(EncryptionManager.class);
	private static String activeEncryptionPrefix = SIMPLE_ENCRYPTION_PREFIX;
	private static ConfiguredEncryption ce;

    static {
        ENCRYPTORS.put(SIMPLE_ENCRYPTION_PREFIX, SimpleEncryption.INSTANCE);
        //login properties files used to have this
        ENCRYPTORS.put("{Standard}", SimpleEncryption.INSTANCE);
		String keyFile = System.getProperty("org.apache.geronimo.security.encryption.keyfile");

		if (keyFile != null && keyFile.length() != 0) {
			try {
				ce = new ConfiguredEncryption(keyFile);
			} catch (Exception e) {
				log.error("Can not handle "+keyFile, e);
			}
			setEncryptionPrefix(CONFIGURED_ENCRYPTION_PREFIX, ce);
		}

	}

    /**
     * Encryption instances should call this to register themselves.
     *
     * @param prefix id in form {name} for the Encryption instance
     * @param encryption Encryption instance to do the work.
     */
    public synchronized static void setEncryptionPrefix(String prefix, Encryption encryption) {
        if (activeEncryptionPrefix.equals(SIMPLE_ENCRYPTION_PREFIX)) {  //only can be set once?
            activeEncryptionPrefix = prefix;
        }
        ENCRYPTORS.put(prefix, encryption);
    }

    /**
     * Idempotent method that outputs string starting with the active registered encryption prefix followed by
     * the output of the registered Encryption instance.  If called with a string encrypted by another
     * registered Encryption it will re-encrypt with the active Encryption instance.
     * @param source Serializable object to encrypt, usually a password string or an already encrypted string.
     * @return the name of the registered Encryption followed by its output.
     */
    public static String encrypt(Serializable source) {
        if (source instanceof String) {
            String sourceString = (String) source;
            if (sourceString.startsWith(activeEncryptionPrefix)) {
                return (String) source;
            } else if (sourceString.startsWith("{")) {
                source = decrypt(sourceString);
            }
        }
        Encryption activeEncryption = ENCRYPTORS.get(activeEncryptionPrefix);
        return activeEncryptionPrefix + activeEncryption.encrypt(source);
    }

    /**
     * Idempotent method that given a String starting with a registered Encryption name will remove the
     * name prefix and return the result of applying the Encryption to the suffix.  If no registered Encryption
     * name matches the start of the string the input will be returned.
     * @param source String that is possibly the output of calling encrypt, consisting of a Encryption name followed by its encrypt output.
     * @return the result of applying the Encryption.decrypt method to the input suffix after identifying the Encryption from the prefix, or the
     * input if no Encryption name matches.
     */
    public static Serializable decrypt(String source) {
        String prefix = null;
        Encryption encryption = null;
        synchronized (ENCRYPTORS) {
            for (Map.Entry<String, Encryption> entry : ENCRYPTORS.entrySet()) {
                prefix = entry.getKey();
                if (source.startsWith(prefix)) {
                    encryption = entry.getValue();
                    break;
                }
            }
        }
        if (encryption != null) {
            return encryption.decrypt(source.substring(prefix.length()));
        }
        return source;
    }
}
