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
package org.apache.geronimo.corba.interceptor;

import org.apache.geronimo.corba.Policies;
import org.omg.CORBA.LocalObject;

class IORInfoImpl
    extends LocalObject
    implements org.omg.PortableInterceptor.IORInfo
{
    java.util.Map profileMap;
    java.util.List allComponents;
    Policies policies;
    String name;

    public String toString () { return "IORInfo for "+name; }

    IORInfoImpl (String name,
                 java.util.Map profileMap,
                 java.util.List allComponents,
                 Policies policies)
    {
        this.name = name;
        this.profileMap = profileMap;
        this.allComponents = allComponents;
        this.policies = policies;
    }

    public org.omg.CORBA.Policy get_effective_policy(int type)
    {
        org.omg.CORBA.Policy p = policies.get (type);
        if (p == null) {
            // try ORB policy?
            throw new org.omg.CORBA.INV_POLICY ();
        } else {
            return p;
        }
    }

    public void add_ior_component(org.omg.IOP.TaggedComponent component)
    {
        allComponents.add (component);
    }

    public void add_ior_component_to_profile
        (org.omg.IOP.TaggedComponent component, int profile_id)
    {
        java.util.List profile = (java.util.List)
            profileMap.get (new Integer (profile_id));

        if (profile == null) {
            throw new org.omg.CORBA.BAD_PARAM ("no profile "+profile_id);
        }

        profile.add (component);
    }
}
