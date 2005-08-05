/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.databasemanager;

import java.sql.Connection;

import javax.portlet.PortletContext;
import javax.sql.DataSource;

import org.apache.geronimo.console.databasemanager.derby.DerbyConnectorRenderer;
import org.apache.geronimo.console.databasemanager.mssql.MSSQLConnectorRenderer;
import org.apache.geronimo.console.databasemanager.tranql.TranQLConnectorRenderer;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.kernel.jmx.JMXUtil;

public class DatabaseManagerPortlet extends
        AbstractConnectionFactoryManagerPortlet {

    public DatabaseManagerPortlet() {
        super(JMXUtil.getObjectName(ObjectNameConstants.JCA_MANAGED_CF_QUERY),
                "/WEB-INF/view/databasemanager/normal.jsp",
                "/WEB-INF/view/databasemanager/help.jsp", DataSource.class);
    }

    protected void setUpExplicitRenderers(PortletContext context) {
        RENDERERS.put("org.tranql.connector.jdbc.JDBCDriverMCF",
                new TranQLConnectorRenderer(kernel, context));
        RENDERERS
                .put(
                        "org.apache.geronimo.derby.connector.DerbyXAManagedConnectionFactory",
                        new DerbyConnectorRenderer(kernel, context));
        RENDERERS
                .put(
                        "com.gluecode.se.mssql.connector.MSSQLXAManagedConnectionFactory",
                        new MSSQLConnectorRenderer(kernel, context));
    }

    protected void testConnection(Object cf) throws Exception {
        DataSource ds = (DataSource) cf;
        Connection c = ds.getConnection();
        c.close();
    }
}
