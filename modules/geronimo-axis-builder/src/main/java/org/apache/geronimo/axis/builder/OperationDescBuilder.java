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
package org.apache.geronimo.axis.builder;

import java.util.Iterator;
import java.util.List;
import javax.wsdl.BindingOperation;
import javax.wsdl.Operation;
import javax.wsdl.Message;
import javax.wsdl.BindingInput;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.webservices.builder.SchemaInfoBuilder;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.description.OperationDesc;

public abstract class OperationDescBuilder {
    protected final OperationDesc operationDesc;
    protected final BindingOperation bindingOperation;
    protected final Operation operation;
    protected final String operationName;
    protected final Message input;
    protected final Message output;
    protected final SOAPOperation soapOperation;
    protected boolean built;

    public OperationDescBuilder(BindingOperation bindingOperation) throws DeploymentException {
        this.bindingOperation = bindingOperation;
        this.operation = bindingOperation.getOperation();
        this.soapOperation = (SOAPOperation) SchemaInfoBuilder.getExtensibilityElement(SOAPOperation.class, bindingOperation.getExtensibilityElements());

        operationDesc = new OperationDesc();
        output = operation.getOutput() == null ? null : operation.getOutput().getMessage();
        operationName = operation.getName();
        input = operation.getInput().getMessage();
    }

    public abstract OperationInfo buildOperationInfo(SOAPConstants soapVersion) throws DeploymentException;

    public abstract OperationDesc buildOperationDesc() throws DeploymentException;

    protected QName getOperationNameFromSOAPBody() {
        BindingInput bindingInput = bindingOperation.getBindingInput();
        List extensibilityElements = bindingInput.getExtensibilityElements();
        for (Iterator iterator = extensibilityElements.iterator(); iterator.hasNext();) {
            ExtensibilityElement extensibilityElement = (ExtensibilityElement) iterator.next();
            if (extensibilityElement instanceof SOAPBody) {
                String namespaceURI = ((SOAPBody)extensibilityElement).getNamespaceURI();
                return new QName(namespaceURI, operationName);
            }
        }
        return new QName("", operationName);
    }
}
