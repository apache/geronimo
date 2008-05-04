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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPBinding;

public class WSDLVisitor
{
    private static final Logger log = LoggerFactory.getLogger(WSDLVisitor.class);

    protected final Definition definition;

    public WSDLVisitor(Definition definition) {
        this.definition = definition;
    }

    public void walkTree() {
        begin();
        try {
            visit(definition);
            for (Iterator iterator = definition.getImports().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String namespaceURI = (String) entry.getKey();
                List importsForNamespace = (List) entry.getValue();
                for (Iterator iterator1 = importsForNamespace.iterator(); iterator1.hasNext();) {
                    Import anImport = (Import) iterator1.next();
                    visit(anImport);
                }
            }
            visit(definition.getTypes());
            Collection messages = definition.getMessages().values();
            for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
                Message message = (Message) iterator.next();
                visit(message);
                Collection parts = message.getParts().values();
                for (Iterator iterator2 = parts.iterator(); iterator2.hasNext();) {
                    Part part = (Part) iterator2.next();
                    visit(part);
                }
            }
            Collection services = definition.getServices().values();
            for (Iterator iterator = services.iterator(); iterator.hasNext();) {
                Service service = (Service) iterator.next();
                visit(service);
                Collection ports = service.getPorts().values();
                for (Iterator iterator1 = ports.iterator(); iterator1.hasNext();) {
                    Port port = (Port) iterator1.next();
                    visit(port);
                    Binding binding = port.getBinding();
                    visit(binding);
                    List bindingOperations = binding.getBindingOperations();
                    for (int i = 0; i < bindingOperations.size(); i++) {
                        BindingOperation bindingOperation = (BindingOperation) bindingOperations.get(i);
                        visit(bindingOperation);
                        visit(bindingOperation.getBindingInput());
                        visit(bindingOperation.getBindingOutput());
                        Collection bindingFaults = bindingOperation.getBindingFaults().values();
                        for (Iterator iterator2 = bindingFaults.iterator(); iterator2.hasNext();) {
                            BindingFault bindingFault = (BindingFault) iterator2.next();
                            visit(bindingFault);
                        }

                    }
                    PortType portType = binding.getPortType();
                    visit(portType);
                    List operations = portType.getOperations();
                    for (int i = 0; i < operations.size(); i++) {
                        Operation operation = (Operation) operations.get(i);
                        visit(operation);
                        {
                            Input input = operation.getInput();
                            visit(input);
                        }
                        {
                            Output output = operation.getOutput();
                            visit(output);
                        }
                        Collection faults = operation.getFaults().values();
                        for (Iterator iterator2 = faults.iterator(); iterator2.hasNext();) {
                            Fault fault = (Fault) iterator2.next();
                            visit(fault);
                        }

                    }
                }
            }
        } catch(Exception e){
            log.error(e.getMessage(), e);
        } finally {
            end();
        }
    }

    protected void begin() {
    }

    protected void end() {
    }

    protected void visit(Fault fault) {
    }

    protected void visit(Definition definition) {
    }

    protected void visit(Import wsdlImport) {
    }

    protected void visit(Types types) {
    }

    protected void visit(BindingFault bindingFault) {
    }

    protected void visit(BindingOutput bindingOutput) {
    }

    protected void visit(BindingInput bindingInput) {
    }

    protected void visit(Output output) {
    }

    protected void visit(Part part) {
    }

    protected void visit(Message message) {
    }

    protected void visit(Input input) {
    }

    protected void visit(Operation operation) {
    }

    protected void visit(PortType portType) {
    }

    protected void visit(BindingOperation bindingOperation) {
    }

    protected void visit(Binding binding) {
    }

    protected void visit(Port port) {
    }

    protected void visit(Service service) {
    }

    protected SOAPBody getSOAPBody(List extensibilityElements) {
        SOAPBody body = null;
        for (int j = 0; j < extensibilityElements.size(); j++) {
            Object element = extensibilityElements.get(j);
            if (element instanceof SOAPBody) {
                body = (SOAPBody) element;
                break;
            }
        }
        return body;
    }

    protected SOAPBinding getSOAPBinding(Binding binding) {
        SOAPBinding soapBinding = null;
        List extensibilityElements = binding.getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            Object element = extensibilityElements.get(i);
            if (element instanceof SOAPBinding) {
                soapBinding = (SOAPBinding) element;
            }
        }
        return soapBinding;
    }
}
