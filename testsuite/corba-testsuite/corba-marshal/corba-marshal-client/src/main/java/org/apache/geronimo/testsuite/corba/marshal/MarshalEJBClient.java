/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.testsuite.corba.marshal;

import java.util.Random;

public class MarshalEJBClient {

    private static final int MAX_ARRAY_SIZE=25;     // Maximum array size needs to be large enough
                                                    // to minimally stress CORBA, but also small
                                                    // enough so as not to encur out of memory
                                                    // exceptions, which is very easy to do since
                                                    // two and three dimensional arrays as used

    public static void main(String[] args) throws Exception{

        Random random = new Random();               // Create a pseudo-random number generator
        int seed = random.nextInt(1000);            // with 1000 possible repeatable variations

        System.out.println("############################################################################");
        System.out.println("Seed for pseudo-random number generator: " + seed);
        System.out.println("Maximum array size: " + MAX_ARRAY_SIZE);

        //
        // marshal/unmarshal various primities and arrays of primitives
        //
        JavaPrimitives primitives = new JavaPrimitives(seed, MAX_ARRAY_SIZE);
        primitives.marshal();


        //
        // marshal/unmarshal various strings and arrays of strings
        //
        JavaStrings strings = new JavaStrings(seed, MAX_ARRAY_SIZE);
        strings.marshal();

        System.out.println("############################################################################");

    }
}
