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
package org.openejb.test.simple.bmp;

import javax.ejb.EJBLocalObject;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/08/01 20:14:20 $
 */
public interface SimpleBMPEntityLocal extends EJBLocalObject {
    String getName();
    void setName(String name);
    String getValue();
    void setValue(String value);
}
