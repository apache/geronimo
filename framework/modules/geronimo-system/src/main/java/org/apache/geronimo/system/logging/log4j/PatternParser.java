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

package org.apache.geronimo.system.logging.log4j;

import org.apache.log4j.helpers.PatternConverter;

/**
 * A simple extension of the log4j pattern parser which adds support for the
 * 'a' letter for a NamedNDC.
 *
 * @version $Rev$ $Date$
 */
public class PatternParser
        extends org.apache.log4j.helpers.PatternParser {
    public PatternParser(String pattern) {
        super(pattern);
    }

    protected void finalizeConverter(char c) {
        PatternConverter pc = null;
        switch (c) {
            case 'a':
                String key = extractOption();
                pc = new NamedNDCConverter(formattingInfo, key);
                currentLiteral.setLength(0);
                break;
            default:
                super.finalizeConverter(c);
                return;
        }
        addConverter(pc);
    }
}
