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

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Big
@Named("big")
public class BigApple extends Apple {

    private static final long serialVersionUID = 1L;

    private int no = 1;

    public void setNo(int no) {
        this.no = no;
    }

    public int getNo() {
        return no;
    }

    public void eat() {
        no--;
        if (no == 0) {
            setMessage("Congratulations! You have got the only apple!");
        } else {
            setMessage("Sorry, the only apple has been taken by another guy~");
        }
    }
}
