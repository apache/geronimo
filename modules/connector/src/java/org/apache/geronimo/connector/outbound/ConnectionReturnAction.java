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

package org.apache.geronimo.connector.outbound;

/**
 * ConnectionReturnAction.java
 *
 *
 * Created: Thu Oct  2 15:11:39 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class ConnectionReturnAction {

    public final static ConnectionReturnAction RETURN_HANDLE =
            new ConnectionReturnAction();
    public final static ConnectionReturnAction DESTROY =
            new ConnectionReturnAction();

    private ConnectionReturnAction() {

    } // ConnectionReturnAction constructor

} // ConnectionReturnAction
