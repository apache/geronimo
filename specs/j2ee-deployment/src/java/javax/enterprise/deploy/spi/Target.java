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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.spi;

/**
 * A Target interface represents a single logical core server of one instance of a
 * J2EE platform product.  It is a designator for a server and the implied location
 * to copy a configured application for the server to access.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:34 $
 */
public interface Target {
    /**
     * Retrieve the name of the target server.
     */
    public String getName();

    /**
     * Retrieve other descriptive information about the target.
     */
    public String getDescription();
}