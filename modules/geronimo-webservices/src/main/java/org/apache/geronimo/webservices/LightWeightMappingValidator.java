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
package org.apache.geronimo.webservices;

import java.util.ArrayList;
import java.util.Map;
import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;

import org.apache.geronimo.validator.ValidationContext;
import org.apache.geronimo.validator.ValidationFailure;

public class LightWeightMappingValidator extends WSDLVisitor {

    private ArrayList operationNames;
    private ValidationContext context;

    private static final QName XSD_STRING = new QName("http://www.w3.org/2001/XMLSchema", "string");

    public LightWeightMappingValidator(Definition definition) {
        super(definition);
    }

    public ValidationContext validate() {
        if (context == null) {
            context = new ValidationContext(definition.getQName().toString());
            walkTree();
        }
        return context;
    }

    public boolean isValid() {
        ValidationContext context = validate();
        return !context.hasFailures() && !context.hasErrors();
    }

    protected void begin() {
        operationNames = new ArrayList();
    }

    protected void visit(Definition definition) {
        if (definition.getServices().values().size() != 1) {
            context.addFailure(new ValidationFailure("A lightweight RPC/Encoded service must contain only one Service"));
        }
    }

    protected void visit(Output output) {
        Map outputParts = output.getMessage().getParts();
        if (outputParts.size() != 0 && outputParts.size() != 1) {
            context.addFailure(new ValidationFailure("The output message must contain zero or one parts: " + output.getName()));
        }

    }

    protected void visit(Operation operation) {
        if (!operationNames.add(operation.getName())) {
            context.addFailure(new ValidationFailure("No two operations can have the same name: " + operation.getName()));
        }
    }

    protected void visit(Fault fault) {
        Part message = fault.getMessage().getPart("message");
        if (message == null) {
            context.addFailure(new ValidationFailure("The fault message must contain one part named 'message' : " + fault.getName()));
        } else if (!XSD_STRING.equals(message.getTypeName())) {
            context.addFailure(new ValidationFailure("The fault message must contain one part of type 'xsd:string' : " + fault.getName()));
        }
    }


    protected void visit(BindingInput bindingInput) {
        SOAPBody body = getSOAPBody(bindingInput.getExtensibilityElements());
        String encoding = body.getUse();
        if (encoding == null || !encoding.equals("encoded")) {
            context.addFailure(new ValidationFailure("The use attribute of the binding input operation must be 'encoded': " + bindingInput.getName()));
        }
    }

    protected void visit(BindingOutput bindingOutput) {
        SOAPBody body = getSOAPBody(bindingOutput.getExtensibilityElements());
        String encoding = body.getUse();
        if (encoding == null || !encoding.equals("encoded")) {
            context.addFailure(new ValidationFailure("The use attribute of the binding output operation must be 'encoded': " + bindingOutput.getName()));
        }
    }

    protected void visit(BindingFault bindingFault) {
        SOAPBody body = getSOAPBody(bindingFault.getExtensibilityElements());
        String encoding = body.getUse();
        if (encoding == null || !encoding.equals("encoded")) {
            context.addFailure(new ValidationFailure("The use attribute of the binding fault operation must be 'encoded': " + bindingFault.getName()));
        }
    }

    protected void visit(Binding binding) {
        SOAPBinding soapBinding = getSOAPBinding(binding);
        if (soapBinding == null || soapBinding.getStyle() == null || !soapBinding.getStyle().equals("rpc")) {
            context.addFailure(new ValidationFailure("The messaging style of the binding must be rpc: " + binding.getQName()));
        }
    }

    protected void visit(Service service) {
        if (service.getPorts().values().size() != 1) {
            context.addFailure(new ValidationFailure("A lightweight RPC/Encoded service must contain only one Port"));
        }
    }
}
