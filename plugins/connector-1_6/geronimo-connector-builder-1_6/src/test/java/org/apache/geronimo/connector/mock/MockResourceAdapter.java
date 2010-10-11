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

package org.apache.geronimo.connector.mock;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import javax.validation.constraints.Pattern;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockResourceAdapter implements ResourceAdapter {


    private BootstrapContext bootstrapContext;

    private String raStringProperty;

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        assert this.bootstrapContext == null : "Attempting to restart adapter without stoppping";
        assert bootstrapContext != null: "Null bootstrap context";
        this.bootstrapContext = bootstrapContext;
    }

    public void stop() {
        bootstrapContext = null;
    }

    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
    }

    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }

    @Pattern(regexp="[\\w]+" )
    public String getRAStringProperty() {
        return raStringProperty;
    }

    public void setRAStringProperty(String raStringProperty) {
        this.raStringProperty = raStringProperty;
    }

}
