/*
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
package org.apache.geronimo.testsuite.di.app;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

@Named
public class AppleBean {
    @Inject @Big
    private Provider<Apple> bigAppleProvider;

    @Inject @Little
    private Provider<Apple> littleAppleProvider;

    public String eatBigApple() {
        bigAppleProvider.get().eat();
        return "result1";
    }

    public String eatLittleApple() {
        // Apple little = littleAppleProvider.get();
        // System.out.print(little);
        // little.eat();
        // System.out.print(little.getMessage());
        littleAppleProvider.get().eat();
        return "result2";
    }
}
