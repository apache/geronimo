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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.wsdl.Part;
import javax.wsdl.BindingOperation;

import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.objectweb.asm.Type;

/**
 * @version $Rev$ $Date$
 */
public class LightweightOperationDescBuilder extends OperationDescBuilder {

    private final Method method;

    public LightweightOperationDescBuilder(BindingOperation bindingOperation, Method method) throws DeploymentException{
        super(bindingOperation);
        if (bindingOperation == null) {
            throw new DeploymentException("No BindingOperation supplied for method " + method.getName());
        }

        this.method = method;

        operationDesc.setName(operationName);
        operationDesc.setStyle(Style.RPC);
        operationDesc.setUse(Use.ENCODED);
    }

    public OperationInfo buildOperationInfo(SOAPConstants soapVersion) throws DeploymentException {
        buildOperationDesc();
        String soapActionURI = soapOperation.getSoapActionURI();
        boolean usesSOAPAction = (soapActionURI != null);
        QName operationQName = getOperationNameFromSOAPBody();

        String methodName = method.getName();
        String methodDesc = Type.getMethodDescriptor(method);


        OperationInfo operationInfo = new OperationInfo(operationDesc, usesSOAPAction, soapActionURI, soapVersion, operationQName, methodName, methodDesc);
        return operationInfo;
    }

    public OperationDesc buildOperationDesc() throws DeploymentException {
        if (built) {
            return operationDesc;
        }

        built = true;

        operationDesc.setMethod(method);

        //section 7.3.2, we don't have to look at parameter ordering.
        //unless it turns out we have to validate it.
//        List order = operation.getParameterOrdering();

        // Verify we have the right number of args for this method
        Class[] methodParamTypes = method.getParameterTypes();
        List inputParts = input.getOrderedParts(null);
        if (methodParamTypes.length != inputParts.size()) {
            throw new DeploymentException("mismatch in parameter counts: method has " + methodParamTypes.length + " whereas the input message has " + inputParts.size());
        }

        // Map the input parts to method args
        int i = 0;
        for (Iterator parts = inputParts.iterator(); parts.hasNext();) {
            Part part = (Part) parts.next();
            String partName = part.getName();
            QName name = new QName("", partName);
            byte mode = ParameterDesc.IN;
            QName typeQName = part.getTypeName() == null ? part.getElementName() : part.getTypeName();
            Class javaClass = methodParamTypes[i++];
            //lightweight mapping has no parts in headers, so inHeader and outHeader are false
            ParameterDesc parameter = new ParameterDesc(name, mode, typeQName, javaClass, false, false);
            operationDesc.addParameter(parameter);
        }

        // Can't have multiple return values
        if (output != null && output.getParts().size() > 1) {
            throw new DeploymentException("Lightweight mapping has at most one part in the (optional) output message, not: " + output.getParts().size());
        }

        // Map the return message, if there is one
        if (output != null && output.getParts().size() == 1) {
            Part part = (Part) output.getParts().values().iterator().next();

            // Set the element name
            QName returnName = part.getElementName() == null ? new QName(part.getName()) : part.getElementName();
            operationDesc.setReturnQName(returnName);

            // Set the element type
            QName returnType = part.getTypeName() == null ? part.getElementName() : part.getTypeName();
            operationDesc.setReturnType(returnType);

            operationDesc.setReturnClass(method.getReturnType());
        }

        //TODO add faults
//        TFault[] faults = tOperation.getFaultArray();
//        for (int i = 0; i < faults.length; i++) {
//            TFault fault = faults[i];
//            QName faultQName = new QName("", fault.getName());
//            String className = ;
//            QName faultTypeQName = ;
//            boolean isComplex = ;
//            FaultDesc faultDesc = new FaultDesc(faultQName, className, faultTypeQName, isComplex)
//        }
        return operationDesc;
    }
}
