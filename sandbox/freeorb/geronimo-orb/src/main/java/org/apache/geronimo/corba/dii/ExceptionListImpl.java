/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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


package org.apache.geronimo.corba.dii;

import java.util.ArrayList;


public class ExceptionListImpl
        extends org.omg.CORBA.ExceptionList
{

    ArrayList list = new ArrayList();

    public int count() {
        return list.size();
    }

    public void add(org.omg.CORBA.TypeCode tc) {
        list.add(tc);
    }

    public org.omg.CORBA.TypeCode item(int idx)
            throws org.omg.CORBA.Bounds
    {
        if (idx < 0 || idx >= list.size())
            throw new org.omg.CORBA.Bounds();

        return (org.omg.CORBA.TypeCode) list.get(idx);
    }

    public void remove(int idx)
            throws org.omg.CORBA.Bounds
    {
        if (idx < 0 || idx >= list.size())
            throw new org.omg.CORBA.Bounds();

        list.remove(idx);
    }


}
