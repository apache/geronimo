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

package org.apache.geronimo.remoting;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:21 $
 */
public interface IPerson {
    public TransientValue getValue();
    public void setValue(TransientValue value);
   /**
    * @return
    */
   public abstract IPerson getSpouse();
   /**
    * @param spouse
    */
   public abstract void setSpouse(IPerson spouse);
   /**
    * @return
    */
   public abstract String getFirstName();
   /**
    * @param firstName
    */
   public abstract void setFirstName(String firstName);
   /**
    * @return
    */
   public abstract String getLastName();
   /**
    * @param lastName
    */
   public abstract void setLastName(String lastName);
}
