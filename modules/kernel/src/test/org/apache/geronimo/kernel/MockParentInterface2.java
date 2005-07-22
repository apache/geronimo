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
package org.apache.geronimo.kernel;

/**
 * An interface with a couple more or less arbitrary methods, used to
 * test GBeans declaring interfaces and generating proxies based on
 * those interfaces.
 *
 * @version $Rev: 220236 $ $Date: 2005-07-22 00:34:39 -0400 (Fri, 22 Jul 2005) $
 */
public interface MockParentInterface2 {
    public String getValue();

    public void setValue(String value);

    public void doNothing();

    public String echo(String msg);

}
