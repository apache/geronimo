/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.sample.servlet;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import javax.sql.DataSource;

/*   @DataSourceDefinition(name="java:app/BJAcc",
   className="org.apache.derby.jdbc.ClientXADataSource",
   url="jdbc:derby://localhost:1527/BJAcc",
   user="system",
   password="manager",
   databaseName="BJAcc",
   transactional=true,
   maxPoolSize=10,
   properties = {"createDatabase = create"})
*/
@DataSourceDefinitions( {
        @DataSourceDefinition(name = "java:app/BJAccNonTx", 
                              className = "org.apache.derby.jdbc.ClientDataSource", 
                              portNumber = 1527, 
                              serverName = "localhost", 
                              url = "jdbc:derby://localhost:1527/BJAcc", 
                              databaseName = "BJAcc", 
                              user = "system", 
                              password = "manager", 
                              transactional = false, 
                              properties = { "createDatabase=create" }),

        @DataSourceDefinition(name = "java:app/BJAccTx", 
                              className = "org.apache.derby.jdbc.ClientXADataSource", 
                              portNumber = 1527, 
                              serverName = "localhost", 
                              url = "jdbc:derby://localhost:1527/BJAcc", 
                              databaseName = "BJAcc", 
                              user = "system", 
                              password = "manager", 
                              transactional = true, 
                              properties = { "createDatabase=create" }) 
} )
public class BJAcc extends BaseServlet {

    @Resource(lookup = "java:app/BJAccNonTx")
    DataSource dataSourceNonTx;

    @Resource(lookup = "java:app/BJAccTx")
    DataSource dataSourcenTx;

    @Override
    DataSource getNonTxDataSourceA() {
        return dataSourceNonTx;
    }

    @Override
    DataSource getTxDataSourceB() {
        return dataSourcenTx;
    }

}
