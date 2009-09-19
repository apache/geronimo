/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.j2ee.deployment.annotation;

import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;

@PersistenceUnits ({
                       @PersistenceUnit(name = "PersistenceUnit1",
                                        unitName = "unitName1"),
                       @PersistenceUnit(name = "PersistenceUnit2",
                                        unitName = "unitName2"),
                       @PersistenceUnit( unitName = "unitName3"),
                       @PersistenceUnit(name = "PersistenceUnit4")
                   })
public class PersistenceUnitAnnotationExample {

    @PersistenceUnit(name = "PersistenceUnit5",
                     unitName = "unitName5")
    long annotatedField1;

    @PersistenceUnit
    String annotatedField2;

    //------------------------------------------------------------------------------------------
    // Method name (for setter-based injection) must follow JavaBeans conventions:
    // -- Must start with "set"
    // -- Have one parameter
    // -- Return void
    //------------------------------------------------------------------------------------------
    @PersistenceUnit(name = "PersistenceUnit7",
                     unitName = "unitName7")
    public void setAnnotatedMethod1(int ii) {
    }

    @PersistenceUnit(name = "PersistenceUnit8",
                     unitName = "unitName8")
    public void setAnnotatedMethod2(boolean bool) {
    }

}
