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
package org.apache.geronimo.corba;

import org.apache.geronimo.corba.ior.InternalIOR;

/** A connection manager can translate an IOR into a series of InvocationProfiles.
 *  
 *  an InvocationProfile is a profile from the IOR paired with the means needed to 
 *  connect to the object (i.e. some kind of client connection factory).
 * */
public interface ConnectionManager {

    InvocationProfile[] getInvocationProfiles(InternalIOR ior);

}
