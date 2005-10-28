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

import org.apache.geronimo.corba.ORB;


public class NVListImpl
        extends org.omg.CORBA.NVList
{

    java.util.ArrayList entries;
    ORB orb;

    public NVListImpl(ORB orb, int length) {
        this.orb = orb;
        entries = new java.util.ArrayList(length);
    }

    public int count() {
        return entries.size();
    }

    public org.omg.CORBA.NamedValue add(int flags) {
        return add_value("", orb.create_any(), flags);
    }

    public org.omg.CORBA.NamedValue add_item(java.lang.String name,
                                             int flags)
    {
        return add_value(name, orb.create_any(), flags);
    }

    public org.omg.CORBA.NamedValue add_value(java.lang.String name,
                                              org.omg.CORBA.Any value,
                                              int flags)
    {
        if (value == null) {
            throw new org.omg.CORBA.BAD_PARAM();
        }

        NamedValueImpl nv = new NamedValueImpl(name, value, flags);
        entries.add(nv);
        return nv;
    }

    public org.omg.CORBA.NamedValue item(int idx)
            throws org.omg.CORBA.Bounds
    {
        if (idx < 0 || idx >= count()) {
            throw new org.omg.CORBA.Bounds();
        }

        return (NamedValueImpl) entries.get(idx);
    }

    public void remove(int idx)
            throws org.omg.CORBA.Bounds
    {
        if (idx < 0 || idx >= count()) {
            throw new org.omg.CORBA.Bounds();
        }

        entries.remove(idx);
    }

}
