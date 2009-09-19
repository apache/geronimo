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

import javax.ejb.EJB;
import javax.ejb.EJBs;

@EJBs ({
           @EJB(name = "EJB1",
                description = "description1",
                beanInterface = javax.ejb.EJBHome.class,
                beanName = "beanName1",
                mappedName = "mappedName1"),
           @EJB(name = "EJB2",
                description = "description2",
                beanInterface = javax.ejb.EJBLocalHome.class,
                beanName = "beanName2",
                mappedName = "mappedName2"),
           @EJB(name = "EJB3",
                description = "description3",
                beanInterface = java.lang.Object.class,
                beanName = "beanName3",
                mappedName = "mappedName3"),
           @EJB(name = "EJB4",
                description = "description4",
                beanInterface = javax.ejb.EJBLocalHome.class,
                beanName = "beanName4",
                mappedName = "mappedName4"),
           @EJB(name = "EJB5",
                description = "description5",
                beanInterface = javax.ejb.EJBHome.class,
                beanName = "beanName5",
                mappedName = "mappedName5"),
           @EJB(name = "EJB6",
                description = "description6",
                beanInterface = javax.ejb.EJBLocalHome.class,
                beanName = "beanName6",
                mappedName = "mappedName6"),
           @EJB(name = "EJB7",
                description = "description7",
                beanInterface = java.lang.Object.class,
                beanName = "beanName7",
                mappedName = "mappedName7")
       })
public class EJBAnnotationExample {

    @EJB
    String annotatedField1;

    @EJB(name = "EJB9",
         description = "description9",
         beanName = "beanName9",
         mappedName = "mappedName9")
    String annotatedField2;

    //------------------------------------------------------------------------------------------
    // Method name (for setter-based injection) must follow JavaBeans conventions:
    // -- Must start with "set"
    // -- Have one parameter
    // -- Return void
    //------------------------------------------------------------------------------------------
    @EJB(name = "EJB10",
         description = "description10",
        beanInterface = javax.ejb.EJBLocalHome.class,
         mappedName = "mappedName10")
    public void setAnnotatedMethod1(int ii) {
    }

    @EJB(name = "EJB11",
         description = "description11",
         beanName = "beanName11",
         mappedName = "mappedName11")
    public void setAnnotatedMethod2(String string) {
    }
    
    @EJB(name="myejb1", 
         beanInterface = javax.ejb.EJBLocalHome.class)
    Object annotatedField3;
    
    @EJB(name="myejb1", 
         beanInterface = javax.ejb.EJBLocalHome.class)
    Object annotatedField4;
    
    @EJB(name="myejb2", 
         beanInterface = javax.ejb.EJBHome.class)
    Object annotatedField5;
       
    @EJB(name="myejb2", 
         beanInterface = javax.ejb.EJBHome.class)
    Object annotatedField6;
}

