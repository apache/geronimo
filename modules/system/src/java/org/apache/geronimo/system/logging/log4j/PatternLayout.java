/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.system.logging.log4j;


/**
 * An extension to the standard log4j PatternLayout for printing a NamedNDC.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:30 $
 */
public class PatternLayout extends org.apache.log4j.PatternLayout {
    protected org.apache.log4j.helpers.PatternParser createPatternParser(String pattern) {
        return new PatternParser(pattern);
    }
}
