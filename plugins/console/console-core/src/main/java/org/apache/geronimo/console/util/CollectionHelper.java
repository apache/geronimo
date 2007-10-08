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

package org.apache.geronimo.console.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dimperial
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class CollectionHelper {

    public static Hashtable parameterMapToHashtable(Map m) {
        Hashtable ret = new Hashtable();
        ret.putAll(m);
        for (Iterator i = ret.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            String[] value = (String[]) ret.get(key);
            try {
                ret.put(key, value[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                // This should not happen but if it does just continue
                // processing.
            }
        }
        return ret;
    }

}
