/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

public abstract class IntegerCache {
    private static final int MIN_VALUE = -999;
    private static final int MAX_VALUE = 9999;

    private static final Integer[] CACHE = getCache();

    public static Integer get(int i) {
        if (i >= MIN_VALUE && i <= MAX_VALUE) {
            return CACHE[i - MIN_VALUE];
        } else {
            return new Integer(i);
        }
    }

    private static Integer[] getCache() {
        Integer[] cache = new Integer[1 + MAX_VALUE - MIN_VALUE];
        for (int i = MIN_VALUE; i <= MAX_VALUE; i++) {
            cache[i - MIN_VALUE] = new Integer(i);
        }
        return cache;
    }
}
