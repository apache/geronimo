/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.axis.builder;

import java.lang.String;
import java.lang.reflect.Method;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;
import javax.xml.rpc.holders.BigDecimalHolder;
import javax.xml.rpc.holders.BigIntegerHolder;
import javax.xml.rpc.holders.BooleanHolder;
import javax.xml.rpc.holders.BooleanWrapperHolder;
import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.ByteHolder;
import javax.xml.rpc.holders.ByteWrapperHolder;
import javax.xml.rpc.holders.CalendarHolder;
import javax.xml.rpc.holders.DoubleHolder;
import javax.xml.rpc.holders.DoubleWrapperHolder;
import javax.xml.rpc.holders.FloatHolder;
import javax.xml.rpc.holders.FloatWrapperHolder;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.IntegerWrapperHolder;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.LongWrapperHolder;
import javax.xml.rpc.holders.ObjectHolder;
import javax.xml.rpc.holders.QNameHolder;
import javax.xml.rpc.holders.ShortHolder;
import javax.xml.rpc.holders.ShortWrapperHolder;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.FaultDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.soap.SOAPConstants;
import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.xbeans.j2ee.*;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.objectweb.asm.Type;

public class HeavyweightOperationDescBuilder extends OperationDescBuilder {

    private final JavaWsdlMappingType mapping;
    private final ServiceEndpointMethodMappingType methodMapping;
    private final SOAPBody soapBody;


    private final Style defaultStyle;
    private final Map exceptionMap;
    private final Map complexTypeMap;
    private final Map elementMap;
    private final ClassLoader classLoader;

    /* Keep track of in and out parameter names so we can verify that
     * everything has been mapped and mapped correctly
     */
    private final Set inParamNames = new HashSet();
    private final Set outParamNames = new HashSet();
    private final Class serviceEndpointInterface;

    public HeavyweightOperationDescBuilder(BindingOperation bindingOperation, JavaWsdlMappingType mapping, ServiceEndpointMethodMappingType methodMapping, Style defaultStyle, Map exceptionMap, Map complexTypeMap, Map elementMap, ClassLoader classLoader, Class serviceEndpointInterface) throws DeploymentException {
        super(bindingOperation);
        this.mapping = mapping;
        this.methodMapping = methodMapping;
        this.defaultStyle = defaultStyle;
        this.exceptionMap = exceptionMap;
        this.complexTypeMap = complexTypeMap;
        this.elementMap = elementMap;
        this.classLoader = classLoader;
        this.serviceEndpointInterface = serviceEndpointInterface;
        BindingInput bindingInput = bindingOperation.getBindingInput();
        this.soapBody = (SOAPBody) WSDescriptorParser.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
    }


    public OperationInfo buildOperationInfo(SOAPConstants soapVersion) throws DeploymentException {
        buildOperationDesc();

        String soapActionURI = soapOperation.getSoapActionURI();
        boolean usesSOAPAction = (soapActionURI != null);
        QName operationQName = new QName("", operation.getName());

        String methodName = methodMapping.getJavaMethodName().getStringValue().trim();

        ArrayList parameters = operationDesc.getParameters();
        Type[] parameterASMTypes = new Type[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            ParameterDesc parameterDesc = (ParameterDesc) parameters.get(i);
            parameterASMTypes[i] = Type.getType(parameterDesc.getJavaType());
        }

        Type returnASMType = (operationDesc.getReturnClass() != null) ? Type.getType(operationDesc.getReturnClass()) : Type.VOID_TYPE;

        String methodDesc = Type.getMethodDescriptor(returnASMType, parameterASMTypes);
        OperationInfo operationInfo = new OperationInfo(operationDesc, usesSOAPAction, soapActionURI, soapVersion, operationQName, methodName, methodDesc);
        return operationInfo;
    }

    public OperationDesc buildOperationDesc() throws DeploymentException {
        if (built) {
            return operationDesc;
        }
        built = true;

        operationDesc.setName(operationName);
        
        // Set to 'document' or 'rpc'
        Style style = Style.getStyle(soapOperation.getStyle(), defaultStyle);
        operationDesc.setStyle(style);

        // Set to 'encoded' or 'literal'
        Use use = Use.getUse(soapBody.getUse());
        operationDesc.setUse(use);

        boolean isWrappedElement = methodMapping.isSetWrappedElement();

        MethodParamPartsMappingType[] paramMappings = methodMapping.getMethodParamPartsMappingArray();

        /* Put the ParameterDesc instance in an array so they can be ordered properly
         * before they are added to the the OperationDesc.
         */
        ParameterDesc[] parameterDescriptions = new ParameterDesc[paramMappings.length];


        // MAP PARAMETERS
        for (int i = 0; i < paramMappings.length; i++) {
            MethodParamPartsMappingType paramMapping = paramMappings[i];
            int position = paramMapping.getParamPosition().getBigIntegerValue().intValue();

            ParameterDesc parameterDesc = mapParameter(paramMapping, isWrappedElement);

            parameterDescriptions[position] = parameterDesc;
        }

        //check that all input message parts are mapped
        if (!inParamNames.equals(input.getParts().keySet())) {
            throw new DeploymentException("Not all input message parts were mapped for operation name" + operationName);
        }

        Class[] paramTypes = new Class[parameterDescriptions.length];
        for (int i = 0; i < parameterDescriptions.length; i++) {
            ParameterDesc parameterDescription = parameterDescriptions[i];
            if (parameterDescription == null) {
                throw new DeploymentException("There is no mapping for parameter number " + i + " for operation " + operationName);
            }
            operationDesc.addParameter(parameterDescription);
            paramTypes[i] = parameterDescription.getJavaType();
        }

        String methodName = methodMapping.getJavaMethodName().getStringValue().trim();
        Method method = null;
        try {
            method = serviceEndpointInterface.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            String args = "(";
            for (int i = 0; i < paramTypes.length; i++) {
                args += paramTypes[i].getName();
                if (i < paramTypes.length - 1) {
                    args += ",";
                }
            }
            args += ")";

            throw new DeploymentException("Mapping references non-existent method in service-endpoint: " + methodName + args);
        }

        operationDesc.setMethod(method);

        // MAP RETURN TYPE
        if (methodMapping.isSetWsdlReturnValueMapping()) {
            mapReturnType();

        }

        if (output != null && !outParamNames.equals(output.getParts().keySet())) {
            throw new DeploymentException("Not all output message parts were mapped to parameters or a return value for operation " + operationName);
        }

        Map faultMap = operation.getFaults();
        for (Iterator iterator = faultMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String faultName = (String) entry.getKey();
            Fault fault = (Fault) entry.getValue();
            FaultDesc faultDesc = mapException(faultName, fault);

            operationDesc.addFault(faultDesc);
        }
        return operationDesc;
    }

    //see jaxrpc 1.1 4.2.1
    private static final Map qnameToClassMap = new HashMap();

    static {
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "integer"), BigInteger.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "long"), long.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "short"), short.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "decimal"), BigDecimal.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "double"), double.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "byte"), byte.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"), long.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"), int.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"), short.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "QName"), QName.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "dateTime"), Calendar.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "date"), Calendar.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "time"), Calendar.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"), URI.class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "hexBinary"), byte[].class);
        qnameToClassMap.put(new QName("http://www.w3.org/2001/XMLSchema", "anySimpleType"), String.class);
    }


    private FaultDesc mapException(String faultName, Fault fault) throws DeploymentException {
        Message message = fault.getMessage();
        QName messageQName = message.getQName();
        ExceptionMappingType exceptionMapping = (ExceptionMappingType) exceptionMap.get(messageQName);
        if (exceptionMapping == null) {
            throw new DeploymentException("No exception mapping for fault " + faultName + " and fault message " + messageQName + " for operation " + operationName);
        }
        String className = exceptionMapping.getExceptionType().getStringValue().trim();
        //this is weird, but I can't figure out what it should be.
        QName faultQName = new QName("", faultName);
        Part part;
        if (exceptionMapping.isSetWsdlMessagePartName()) {
            //According to schema documentation, this will only be set when several headerfaults use the same message.
            String headerFaultMessagePartName = exceptionMapping.getWsdlMessagePartName().getStringValue();
            part = message.getPart(headerFaultMessagePartName);
        } else {
            part = (Part) message.getOrderedParts(null).iterator().next();
        }
        QName faultTypeQName;// = part.getElementName() == null ? part.getTypeName() : part.getElementName();
        if (part.getElementName() == null) {
            faultTypeQName = part.getTypeName();
            if (faultTypeQName == null) {
                throw new DeploymentException("Neither type nor element name supplied for part: " + part);
            }
        } else {
            faultTypeQName = (QName) elementMap.get(part.getElementName());
            if (faultTypeQName == null) {
                throw new DeploymentException("Can not find type for: element: " + part.getElementName() + ", known elements: " + elementMap);
            }
        }
        SchemaType complexType = (SchemaType) complexTypeMap.get(faultTypeQName);
        boolean isComplex = complexType != null;
        FaultDesc faultDesc = new FaultDesc(faultQName, className, faultTypeQName, isComplex);

        //constructor parameters
        if (exceptionMapping.isSetConstructorParameterOrder()) {
            if (!isComplex) {
                throw new DeploymentException("ConstructorParameterOrder can only be set for complex types, not " + faultTypeQName);
            }
            Map elementMap = new HashMap();
            SchemaProperty[] properties = complexType.getProperties();
            for (int i = 0; i < properties.length; i++) {
                SchemaProperty property = properties[i];
                QName elementName = property.getName();
                SchemaType elementType = property.getType();
                QName elementTypeQName = elementType.getName();
                elementMap.put(elementName.getLocalPart(), elementTypeQName);
            }
//                LocalElement[] elements = explicitGroup.getElementArray();
//                for (int i = 0; i < elements.length; i++) {
//                    LocalElement element = elements[i];
//                    String elementName = element.getName();
//                    QName elementType = element.getType();
//                    elementMap.put(elementName, elementType);
//                }
            ArrayList parameterTypes = new ArrayList();
            ConstructorParameterOrderType constructorParameterOrder = exceptionMapping.getConstructorParameterOrder();
            for (int i = 0; i < constructorParameterOrder.getElementNameArray().length; i++) {
                String elementName = constructorParameterOrder.getElementNameArray(i).getStringValue().trim();
                QName elementType = (QName) elementMap.get(elementName);
                Class javaElementType;
                if (complexTypeMap.containsKey(elementType)) {
                    String packageName = WSDescriptorParser.getPackageFromNamespace(elementType.getNamespaceURI(), mapping);
                    String javaElementTypeName = packageName + "." + elementType.getLocalPart();
                    try {
                        javaElementType = ClassLoading.loadClass(javaElementTypeName, classLoader);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("Could not load exception constructor parameter", e);
                    }
                } else if (qnameToClassMap.containsKey(elementType)) {
                    javaElementType = (Class) qnameToClassMap.get(elementType);
                } else {
                    throw new DeploymentException("Unknown type: " + elementType);
                }
                //todo faultTypeQName is speculative
                //todo outheader might be true!
                ParameterDesc parameterDesc = new ParameterDesc(faultTypeQName, ParameterDesc.OUT, elementType, javaElementType, false, false);
                parameterTypes.add(parameterDesc);
            }
            faultDesc.setParameters(parameterTypes);
        }
        return faultDesc;
    }

    private void mapReturnType() throws DeploymentException {
        QName returnType = null;
        QName returnQName = null;
        Class returnClass = null;

        if (output == null) {
            throw new DeploymentException("No output message, but a mapping for it for operation " + operationName);
        }
        WsdlReturnValueMappingType wsdlReturnValueMapping = methodMapping.getWsdlReturnValueMapping();
        String returnClassName = wsdlReturnValueMapping.getMethodReturnValue().getStringValue().trim();
        try {
            returnClass = ClassLoading.loadClass(returnClassName, classLoader);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load return type for operation " + operationName, e);
        }

        QName wsdlMessageQName = wsdlReturnValueMapping.getWsdlMessage().getQNameValue();

        if (!wsdlMessageQName.equals(output.getQName())) {
            throw new DeploymentException("OutputMessage has QName: " + output.getQName() + " but mapping specifies: " + wsdlMessageQName + " for operation " + operationName);
        }

        if (wsdlReturnValueMapping.isSetWsdlMessagePartName()) {
            String wsdlMessagePartName = wsdlReturnValueMapping.getWsdlMessagePartName().getStringValue().trim();
            if (outParamNames.contains(wsdlMessagePartName)) {
                throw new DeploymentException("output message part " + wsdlMessagePartName + " has both an INOUT or OUT mapping and a return value mapping for operation " + operationName);
            }
            Part part = output.getPart(wsdlMessagePartName);
            returnQName = part.getElementName();
            returnType = part.getTypeName();

            outParamNames.add(wsdlMessagePartName);

        } else {
            //what does this mean????
        }

        operationDesc.setReturnQName(returnQName);
        operationDesc.setReturnType(returnType);
        operationDesc.setReturnClass(returnClass);
    }

    private ParameterDesc mapParameter(MethodParamPartsMappingType paramMapping, boolean wrappedElement) throws DeploymentException {
        WsdlMessageMappingType wsdlMessageMappingType = paramMapping.getWsdlMessageMapping();
        QName wsdlMessageQName = wsdlMessageMappingType.getWsdlMessage().getQNameValue();
        String wsdlMessagePartName = wsdlMessageMappingType.getWsdlMessagePartName().getStringValue().trim();

        String parameterMode = wsdlMessageMappingType.getParameterMode().getStringValue().trim();
        byte mode = ParameterDesc.modeFromString(parameterMode);
        boolean isInParam = mode == ParameterDesc.IN || mode == ParameterDesc.INOUT;
        boolean isOutParam = mode == ParameterDesc.OUT || mode == ParameterDesc.INOUT;

        if (isOutParam && output == null) {
            throw new DeploymentException("Mapping for output parameter " + wsdlMessagePartName + " found, but no output message for operation " + operationName);
        }
        boolean isSoapHeader = wsdlMessageMappingType.isSetSoapHeader();
        boolean inHeader = isSoapHeader && isInParam;
        boolean outHeader = isSoapHeader && isOutParam;

        Part part;
        if (isInParam) {
            if (!wsdlMessageQName.equals(input.getQName())) {
                throw new DeploymentException("QName of input message: " + input.getQName() +
                        " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
            }
            part = input.getPart(wsdlMessagePartName);
            if (part == null) {
                throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in input message for operation " + operationName);
            }
            inParamNames.add(wsdlMessagePartName);
            if (isOutParam) {
                //inout, check that part of same name and type is in output message
                Part outPart = output.getPart(wsdlMessagePartName);
                if (outPart == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for INOUT parameter of operation " + operationName);
                }
                if (!part.getName().equals(outPart.getName())) {
                    throw new DeploymentException("Mismatched input part name: " + part.getName() + " and output part name: " + outPart.getName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                }
                if (!(part.getElementName() == null ? outPart.getElementName() == null : part.getElementName().equals(outPart.getElementName()))) {
                    throw new DeploymentException("Mismatched input part element name: " + part.getElementName() + " and output part element name: " + outPart.getElementName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                }
                if (!(part.getTypeName() == null ? outPart.getTypeName() == null : part.getTypeName().equals(outPart.getTypeName()))) {
                    throw new DeploymentException("Mismatched input part type name: " + part.getTypeName() + " and output part type name: " + outPart.getTypeName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                }
                outParamNames.add(wsdlMessagePartName);
            }
        } else if (isOutParam) {
            if (!wsdlMessageQName.equals(output.getQName())) {
                throw new DeploymentException("QName of output message: " + output.getQName() +
                        " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
            }
            part = output.getPart(wsdlMessagePartName);
            if (part == null) {
                throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
            }
            outParamNames.add(wsdlMessagePartName);
        } else {
            throw new AssertionError("a param mapping has to be IN or OUT or INOUT");
        }

        //TODO this makes little sense but may be correct, see comments in axis Parameter class
        //the part name qname is really odd.
        QName partQName = wrappedElement ? part.getElementName() : new QName("", part.getName());
        QName partTypeQName = part.getTypeName();

        //use complexTypeMap
        boolean isComplexType = complexTypeMap.containsKey(partTypeQName);
        String paramJavaTypeName = paramMapping.getParamType().getStringValue().trim();
        boolean isInOnly = mode == ParameterDesc.IN;
        Class actualParamJavaType = WSDescriptorParser.getHolderType(paramJavaTypeName, isInOnly, partTypeQName, isComplexType, mapping, classLoader);

        ParameterDesc parameterDesc = new ParameterDesc(partQName, mode, partTypeQName, actualParamJavaType, inHeader, outHeader);
        return parameterDesc;
    }

    /**
     * Supporting the Document/Literal Wrapped pattern
     *
     * See http://www-106.ibm.com/developerworks/webservices/library/ws-whichwsdl/ for a nice explanation and example
     * 
     * wrapped-element tag is used
     * WSDL message with a single part
     * part uses the 'element' attribute to point to an elemement in the types section
     * the element type and the element's name match the operation name
     */


}
