/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.naming.enc;

import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.CompoundName;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public final class EnterpriseNamingContextNameParser implements NameParser {
    private static final Properties PARSER_PROPERTIES = new Properties();
    static {
        PARSER_PROPERTIES.put("jndi.syntax.direction", "left_to_right");
        PARSER_PROPERTIES.put("jndi.syntax.separator", "/");
    }

    public final static EnterpriseNamingContextNameParser INSTANCE = new EnterpriseNamingContextNameParser();

    private EnterpriseNamingContextNameParser() {
    }

    public Name parse(String name) throws NamingException {
        return new CompoundName(name, PARSER_PROPERTIES);
    }
}
