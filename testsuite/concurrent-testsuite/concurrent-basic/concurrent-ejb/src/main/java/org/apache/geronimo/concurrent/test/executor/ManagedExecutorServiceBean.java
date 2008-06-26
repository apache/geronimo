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
package org.apache.geronimo.concurrent.test.executor;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import javax.util.concurrent.ManagedExecutorService;

import org.apache.geronimo.concurrent.test.ManagedExecutorServiceTest;

@Stateless(name="ManagedExecutorServiceBean")
public class ManagedExecutorServiceBean 
    implements ManagedExecutorServiceRemote, 
               ManagedExecutorServiceLocal {

    @Resource
    ManagedExecutorService executorService;
          
    @Resource(name="ConcurrentPool")
    DataSource db;
       
    public void testBasicContextMigration() throws Exception {
        ManagedExecutorServiceTest test = new ManagedExecutorServiceTest(executorService);
        test.testContextMigration(false);
    }
    
    public void testSecurityContextMigration() throws Exception {
        ManagedExecutorServiceTest test = new ManagedExecutorServiceTest(executorService);
        test.testContextMigration(true);
    }
    
}
