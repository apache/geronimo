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

package org.apache.geronimo.remoting;

import java.io.Serializable;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:04 $
 */
public class Person implements Serializable, IPerson {
    
    private String firstName;
    private String lastName;
    private IPerson spouse;
    private TransientValue value;
    
   /**
    * @return
    */
   public TransientValue getValue() {
      return value;
   }

   /**
    * @param value
    */
   public void setValue(TransientValue value) {
      this.value = value;
   }

   /**
    * @return
    */
   public IPerson getSpouse() {
      return spouse;
   }

   /**
    * @param spouse
    */
   public void setSpouse(IPerson spouse) {
      this.spouse = spouse;
   }

   /**
    * @return
    */
   public String getFirstName() {
      return firstName;
   }

   /**
    * @param firstName
    */
   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   /**
    * @return
    */
   public String getLastName() {
      return lastName;
   }

   /**
    * @param lastName
    */
   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

    public static void main(String[] args) {
      Person p = new Person();
      p.setSpouse(p);
   }
}
