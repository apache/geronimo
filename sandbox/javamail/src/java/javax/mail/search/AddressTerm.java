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

package javax.mail.search;
import javax.mail.Address;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:28 $
 */
public abstract class AddressTerm extends SearchTerm {
    protected Address address;
    protected AddressTerm(Address address) {
        this.address = address;
    }
    public boolean equals(Object other) {
        return super.equals(other)
            && ((AddressTerm) other).address.equals(address);
    }
    public Address getAddress() {
        return address;
    }
    public int hashCode() {
        return super.hashCode() + address.hashCode();
    }
    protected boolean match(Address address) {
        return this.address.equals(address);
    }
}
