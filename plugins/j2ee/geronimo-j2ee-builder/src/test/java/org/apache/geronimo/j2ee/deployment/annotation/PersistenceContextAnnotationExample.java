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

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceProperty;

@PersistenceContexts ({
                          @PersistenceContext(name = "PersistenceContext1",
                                              properties={@PersistenceProperty(name="property1", value="value1"),
                                                  @PersistenceProperty(name="property2", value="value2")},
                                              type = PersistenceContextType.TRANSACTION),
                          @PersistenceContext(name = "PersistenceContext2",
                                              unitName = "unitName2",
                                              properties={@PersistenceProperty(name="property3", value="value3"),
                                                  @PersistenceProperty(name="property4", value="value4")},
                                              type = PersistenceContextType.EXTENDED)
                      })
public class PersistenceContextAnnotationExample {

    @PersistenceContext(name = "PersistenceContext3",
                        properties={@PersistenceProperty(name="property5", value="value5"),
                            @PersistenceProperty(name="property6", value="value6")},
                        type = PersistenceContextType.TRANSACTION)
    String annotatedField1;

    @PersistenceContext(name = "PersistenceContext3",
                        unitName = "unitName4",
                        type = PersistenceContextType.EXTENDED)
    String annotatedField2;

    //------------------------------------------------------------------------------------------
    // Method name (for setter-based injection) must follow JavaBeans conventions:
    // -- Must start with "set"
    // -- Have one parameter
    // -- Return void
    //------------------------------------------------------------------------------------------
    @PersistenceContext(name = "PersistenceContext4",
                        unitName = "unitName5",
                        properties={@PersistenceProperty(name="property9", value="value9"),
                            @PersistenceProperty(name="property10", value="value10")},
                        type = PersistenceContextType.TRANSACTION)
    public void setAnnotatedMethod1(String string) {
    }

    @PersistenceContext
    public void setAnnotatedMethod2(String string) {
    }

}

