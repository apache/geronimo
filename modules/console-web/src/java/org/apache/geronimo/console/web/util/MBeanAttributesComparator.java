/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.console.web.util;

import java.util.Comparator;

/*
 * Sort Attribute Strings while ignoring case.  Later may be changed to
 * better handle the JSR-77 attributes.
 *
 */

public class MBeanAttributesComparator implements Comparator {
    private static final int LEFT_GREATER = 1;
    private static final int RIGHT_GREATER = -1;
    private static final int EQUAL = 0;

    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        return s1.compareToIgnoreCase(s2);
    }
}
