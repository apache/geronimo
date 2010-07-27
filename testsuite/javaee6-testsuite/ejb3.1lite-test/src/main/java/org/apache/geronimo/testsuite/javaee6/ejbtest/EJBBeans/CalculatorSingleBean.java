/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.apache.geronimo.testsuite.javaee6.ejbtest.EJBBeans;

import javax.ejb.Singleton;
import java.text.DecimalFormat;
@Singleton
public class CalculatorSingleBean {
    String output = "Start the calculator process:<br/>";
    double result = 0;
    public double add(double d) {
        double tmp = result + d;
        this.output += result + " + " + d + " equals " + tmp + "<br/>";
        result = tmp;
        return result;
    }

    public String getOutput() {
        return this.output;
    }

    public double sub(double d) {
        double tmp = result - d;
        this.output += result + " - " + d + " equals " + tmp + "<br/>";
        result = tmp;
        return result;
    }
 
}
