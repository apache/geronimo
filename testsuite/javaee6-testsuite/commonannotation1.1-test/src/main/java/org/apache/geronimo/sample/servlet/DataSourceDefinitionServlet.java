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
import javax.sql.DataSource;

@DataSourceDefinition(name="java:app/MyDataSource",
        className="org.apache.derby.jdbc.ClientDataSource",
        portNumber=1527,
        description="this is my data source",
        user="system",
        databaseName="MyDatabase",
        properties = { "osgi.jndi.service.name = mydatasource", "createDatabase = create" },
        serverName="localhost")
public class DataSourceDefinitionServlet extends BaseServlet {

    @Resource(lookup="java:app/MyDataSource")
    DataSource dataSource1;

    @Resource(lookup="osgi:service/mydatasource")
    DataSource dataSource2;

    DataSource getDataSourceA() {
        return dataSource1;
    }

    @Override
    DataSource getDataSourceB() {
        return dataSource2;
    }
      
}
