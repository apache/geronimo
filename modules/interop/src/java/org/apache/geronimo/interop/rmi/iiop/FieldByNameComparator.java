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
package org.apache.geronimo.interop.rmi.iiop;

import java.lang.reflect.*;
import java.util.*;

/**
 ** Sort fields by name only.
 **/
public class FieldByNameComparator implements Comparator
{
    public static final FieldByNameComparator SINGLETON = new FieldByNameComparator();

    public int compare(Object x, Object y)
    {
        Field a = (Field)x;
        Field b = (Field)y;
        return a.getName().compareTo(b.getName());
    }

    public boolean equals(Object x)
    {
        // shouldn't be used
        return false;
    }
}

