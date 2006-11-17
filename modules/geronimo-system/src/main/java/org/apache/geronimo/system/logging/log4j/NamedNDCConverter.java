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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public final class NamedNDCConverter
        extends PatternConverter {
    private final NamedNDC namedNDC;

    public NamedNDCConverter(FormattingInfo formattingInfo, String key) {
        super(formattingInfo);
        namedNDC = NamedNDC.getNamedNDC(key);
        assert namedNDC != null;
    }

    protected String convert(LoggingEvent loggingEvent) {
        try {
            Object value = namedNDC.get();
            if (value == null) {
                return null;
            }
            return value.toString();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }
}
