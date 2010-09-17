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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.OperationType;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;

import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.FaultDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.encoding.XMLType;
import org.apache.geronimo.axis.client.OperationInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.openejb.jee.ConstructorParameterOrder;
import org.apache.openejb.jee.ExceptionMapping;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JavaXmlTypeMapping;
import org.apache.openejb.jee.MethodParamPartsMapping;
import org.apache.openejb.jee.ServiceEndpointMethodMapping;
import org.apache.openejb.jee.WsdlMessageMapping;
import org.apache.openejb.jee.WsdlReturnValueMapping;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.objectweb.asm.Type;
import org.apache.geronimo.webservices.builder.SchemaInfoBuilder;
import org.apache.geronimo.webservices.builder.WSDescriptorParser;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class HeavyweightOperationDescBuilder extends OperationDescBuilder {

    private final JavaWsdlMapping mapping;
    private final ServiceEndpointMethodMapping methodMapping;
    private final SOAPBody soapBody;


    private final Map exceptionMap;
    private final SchemaInfoBuilder schemaInfoBuilder;
    private final Bundle bundle;
    private final boolean rpcStyle;
    private final boolean documentStyle;
    private final boolean wrappedStyle;
    private final boolean isEncoded;
    private final Map publicTypes = new HashMap();
    private final Map anonymousTypes = new HashMap();

    /* Keep track of in and out parameter names so we can verify that
     * everything has been mapped and mapped correctly
     */
    private final Set inParamNames = new HashSet();
    private final Set outParamNames = new HashSet();
    private final Class serviceEndpointInterface;

    /**
     * Track the wrapper elements
     */
    private final Set wrapperElementQNames = new HashSet();

    public HeavyweightOperationDescBuilder(BindingOperation bindingOperation, JavaWsdlMapping mapping, ServiceEndpointMethodMapping methodMapping, Style defaultStyle, Map exceptionMap, SchemaInfoBuilder schemaInfoBuilder, List<JavaXmlTypeMapping> javaXmlTypeMappingTypes, Bundle bundle, Class serviceEndpointInterface) throws DeploymentException {
        super(bindingOperation);
        this.mapping = mapping;
        this.methodMapping = methodMapping;
        this.exceptionMap = exceptionMap;
        this.schemaInfoBuilder = schemaInfoBuilder;
        for (JavaXmlTypeMapping javaXmlTypeMappingType: javaXmlTypeMappingTypes) {
            String javaClassName = javaXmlTypeMappingType.getJavaType();
            String anonymousTypeQName = javaXmlTypeMappingType.getAnonymousTypeQname(); 
            if (anonymousTypeQName != null) {
                anonymousTypes.put(anonymousTypeQName, javaClassName);
            } else {
                QName qname = javaXmlTypeMappingType.getRootTypeQname();
                if (qname != null) {
                    publicTypes.put(qname, javaClassName);
                }
            }
        }
        this.bundle = bundle;
        this.serviceEndpointInterface = serviceEndpointInterface;
        BindingInput bindingInput = bindingOperation.getBindingInput();
        this.soapBody = (SOAPBody) SchemaInfoBuilder.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());

        wrappedStyle = methodMapping.getWrappedElement() != null;
        if (false == wrappedStyle) {
            Style style = Style.getStyle(soapOperation.getStyle(), defaultStyle);
            if (style == Style.RPC) {
                rpcStyle = true;
                documentStyle = false;
            } else {
                rpcStyle = false;
                documentStyle = true;
            }
        } else {
            rpcStyle = false;
            documentStyle = false;
        }
        isEncoded = Use.getUse(soapBody.getUse()) == Use.ENCODED;
    }

    public Set getWrapperElementQNames() throws DeploymentException {
        buildOperationDesc();

        return Collections.unmodifiableSet(wrapperElementQNames);
    }

    public boolean isEncoded() {
        return isEncoded;
    }

    public OperationInfo buildOperationInfo(SOAPConstants soapVersion) throws DeploymentException {
        buildOperationDesc();

        String soapActionURI = soapOperation.getSoapActionURI();
        boolean usesSOAPAction = (soapActionURI != null);
        QName operationQName = getOperationQName();

        String methodName = methodMapping.getJavaMethodName();

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

    private QName getOperationQName() {
        if (wrappedStyle) {
            Map parts = operation.getInput().getMessage().getParts();
            if (parts != null && !parts.isEmpty()) {
                for (Iterator iterator = parts.values().iterator(); iterator.hasNext();) {
                    Part part = (Part) iterator.next();
                    return part.getElementName();
                }
            }
        }
        return getOperationNameFromSOAPBody();

    }

    public OperationDesc buildOperationDesc() throws DeploymentException {
        if (built) {
            return operationDesc;
        }
        built = true;

        operationDesc.setName(operationName);

        // Set to 'document', 'rpc' or 'wrapped'
        if (wrappedStyle) {
            operationDesc.setStyle(Style.WRAPPED);
        } else if (rpcStyle) {
            operationDesc.setStyle(Style.RPC);
        } else {
            operationDesc.setStyle(Style.DOCUMENT);
        }

        // Set to 'encoded' or 'literal'
        Use use = Use.getUse(soapBody.getUse());
        operationDesc.setUse(use);


        List<MethodParamPartsMapping> paramMappings = methodMapping.getMethodParamPartsMapping();

        /* Put the ParameterDesc instance in an array so they can be ordered properly
         * before they are added to the the OperationDesc.
         */
        ParameterDesc[] parameterDescriptions = new ParameterDesc[paramMappings.size()];


        // MAP PARAMETERS
        for (MethodParamPartsMapping paramMapping: paramMappings) {
            int position = paramMapping.getParamPosition().intValue();

            ParameterDesc parameterDesc = mapParameter(paramMapping);

            parameterDescriptions[position] = parameterDesc;
        }

        if (wrappedStyle) {
            Part inputPart = getWrappedPart(input);
            QName name = inputPart.getElementName();
            SchemaType operationType = (SchemaType) schemaInfoBuilder.getComplexTypesInWsdl().get(name);

            Set expectedInParams = new HashSet();

            // schemaType should be complex using xsd:sequence compositor
            SchemaParticle parametersType = operationType.getContentModel();
            //parametersType can be null if the element has empty content such as
//            <element name="getMarketSummary">
//             <complexType>
//              <sequence/>
//             </complexType>
//            </element>

            if (parametersType != null) {
                if (SchemaParticle.ELEMENT == parametersType.getParticleType()) {
                    expectedInParams.add(parametersType.getName().getLocalPart());
                } else if (SchemaParticle.SEQUENCE == parametersType.getParticleType()) {
                    SchemaParticle[] parameters = parametersType.getParticleChildren();
                    for (int i = 0; i < parameters.length; i++) {
                        expectedInParams.add(parameters[i].getName().getLocalPart());
                    }
                }
            }
            if (!inParamNames.equals(expectedInParams)) {
                throw new DeploymentException("Not all wrapper children were mapped for operation name" + operationName);
            }
        } else {
            //check that all input message parts are mapped
            if (!inParamNames.equals(input.getParts().keySet())) {
                throw new DeploymentException("Not all input message parts were mapped for operation name" + operationName);
            }
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

        String methodName = methodMapping.getJavaMethodName();
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
        operationDesc.setMep(operation.getStyle());
        if (methodMapping.getWsdlReturnValueMapping() != null) {
            mapReturnType();
        } else if (operation.getStyle() == OperationType.REQUEST_RESPONSE) {
            //TODO WARNING THIS APPEARS TO SUBVERT THE COMMENT IN j2ee_jaxrpc_mapping_1_1.xsd IN service-endpoint-method-mappingType:
            //The wsdl-return-value-mapping is not specified for one-way operations.
            operationDesc.setReturnQName(null);             //??
            operationDesc.setReturnType(XMLType.AXIS_VOID);
            operationDesc.setReturnClass(void.class);
        }

        if (null != output && wrappedStyle) {
            Part inputPart = getWrappedPart(output);
            QName name = inputPart.getElementName();
            SchemaType operationType = (SchemaType) schemaInfoBuilder.getComplexTypesInWsdl().get(name);

            Set expectedOutParams = new HashSet();

            // schemaType should be complex using xsd:sequence compositor
            SchemaParticle parametersType = operationType.getContentModel();
            //again, no output can give null parametersType
            if (parametersType != null) {
                if (SchemaParticle.ELEMENT == parametersType.getParticleType()) {
                    expectedOutParams.add(parametersType.getName().getLocalPart());
                } else if (SchemaParticle.SEQUENCE == parametersType.getParticleType()) {
                    SchemaParticle[] parameters = parametersType.getParticleChildren();
                    for (int i = 0; i < parameters.length; i++) {
                        expectedOutParams.add(parameters[i].getName().getLocalPart());
                    }
                }
            }
            if (!outParamNames.equals(expectedOutParams)) {
                throw new DeploymentException("Not all wrapper children were mapped to parameters or a return value for operation " + operationName);
            }
        } else if (null != output) {
            if (!outParamNames.equals(output.getParts().keySet())) {
                throw new DeploymentException("Not all output message parts were mapped to parameters or a return value for operation " + operationName);
            }
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
        ExceptionMapping exceptionMapping = (ExceptionMapping) exceptionMap.get(messageQName);
        if (exceptionMapping == null) {
            throw new DeploymentException("No exception mapping for fault " + faultName + " and fault message " + messageQName + " for operation " + operationName);
        }
        String className = exceptionMapping.getExceptionType();
        //TODO investigate whether there are other cases in which the namespace of faultQName can be determined.
        //this is weird, but I can't figure out what it should be.
        //if part has an element rather than a type, it should be part.getElementName() (see below)
        QName faultQName = new QName("", faultName);
        Part part;
        if (exceptionMapping.getWsdlMessagePartName() != null) {
            //According to schema documentation, this will only be set when several headerfaults use the same message.
            String headerFaultMessagePartName = exceptionMapping.getWsdlMessagePartName();
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
            faultQName = part.getElementName();
            faultTypeQName = (QName) schemaInfoBuilder.getElementToTypeMap().get(part.getElementName());
            if (faultTypeQName == null) {
                throw new DeploymentException("Can not find type for: element: " + part.getElementName() + ", known elements: " + schemaInfoBuilder.getElementToTypeMap());
            }
        }
        SchemaType complexType = (SchemaType) schemaInfoBuilder.getComplexTypesInWsdl().get(faultTypeQName);
        boolean isComplex = complexType != null;
        FaultDesc faultDesc = new FaultDesc(faultQName, className, faultTypeQName, isComplex);

        //constructor parameters
        if (exceptionMapping.getConstructorParameterOrder() != null) {
            if (!isComplex) {
                throw new DeploymentException("ConstructorParameterOrder can only be set for complex types, not " + faultTypeQName);
            }
            Map elementMap = new HashMap();
            SchemaProperty[] properties = complexType.getProperties();
            for (int i = 0; i < properties.length; i++) {
                SchemaProperty property = properties[i];
                QName elementName = property.getName();
                SchemaType elementType = property.getType();
                elementMap.put(elementName.getLocalPart(), elementType);
            }
            ArrayList parameterTypes = new ArrayList();
            ConstructorParameterOrder constructorParameterOrder = exceptionMapping.getConstructorParameterOrder();
            for (String elementName: constructorParameterOrder.getElementName()) {
                SchemaType elementType = (SchemaType) elementMap.get(elementName);
                Class javaElementType;

                QName elementTypeQName = elementType.getName();
                if (elementTypeQName != null) {
                    if (schemaInfoBuilder.getComplexTypesInWsdl().containsKey(elementType)) {
                        String javaClassName = (String) publicTypes.get(elementTypeQName);
                        if (javaClassName == null) {
                            throw new DeploymentException("No class mapped for element type: " + elementType);
                        }
                        javaElementType = getJavaClass(javaClassName);
                    } else {
                        javaElementType = (Class) qnameToClassMap.get(elementTypeQName);
                        if (javaElementType == null) {
                            throw new DeploymentException("Unknown type: " + elementType + " of name: " + elementName + " and QName: " + elementTypeQName);
                        }
                    }
                } else {
                    //anonymous type
                    //anonymous type qname is constructed using rules 1.b and 2.b
                    String anonymousQName = complexType.getName().getNamespaceURI() + ":>" + complexType.getName().getLocalPart() + ">" + elementName;
                    String javaClassName = (String) anonymousTypes.get(anonymousQName);
                    if (javaClassName == null) {
                        if (elementType.isSimpleType()) {
                            //maybe it's a restriction of a built in simple type
                            SchemaType baseType = elementType.getBaseType();
                            QName simpleTypeQName = baseType.getName();
                            javaElementType = (Class) qnameToClassMap.get(simpleTypeQName);
                            if (javaElementType == null) {
                                throw new DeploymentException("Unknown simple type: " + elementType + " of name: " + elementName + " and QName: " + simpleTypeQName);
                            }
                        } else {
                            throw new DeploymentException("No class mapped for anonymous type: " + anonymousQName);
                        }
                    } else {
                        javaElementType = getJavaClass(javaClassName);
                    }
                }
                //todo faultTypeQName is speculative
                //todo outheader might be true!
                ParameterDesc parameterDesc = new ParameterDesc(faultTypeQName, ParameterDesc.OUT, elementTypeQName, javaElementType, false, false);
                parameterTypes.add(parameterDesc);
            }
            faultDesc.setParameters(parameterTypes);
        }
        return faultDesc;
    }

    private Class getJavaClass(String javaClassName) throws DeploymentException {
        try {
            Class javaClass = ClassLoading.loadClass(javaClassName, bundle);
            return javaClass;
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load class", e);
        }
    }

    private void mapReturnType() throws DeploymentException {
        QName returnType = null;
        QName returnQName = null;
        Class returnClass = null;

        if (output == null) {
            throw new DeploymentException("No output message, but a mapping for it for operation " + operationName);
        }
        WsdlReturnValueMapping wsdlReturnValueMapping = methodMapping.getWsdlReturnValueMapping();
        String returnClassName = wsdlReturnValueMapping.getMethodReturnValue();
        try {
            returnClass = ClassLoading.loadClass(returnClassName, bundle);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load return type for operation " + operationName, e);
        }

        QName wsdlMessageQName = wsdlReturnValueMapping.getWsdlMessage();

        if (!wsdlMessageQName.equals(output.getQName())) {
            throw new DeploymentException("OutputMessage has QName: " + output.getQName() + " but mapping specifies: " + wsdlMessageQName + " for operation " + operationName);
        }

        if (wsdlReturnValueMapping.getWsdlMessagePartName() != null) {
            String wsdlMessagePartName = wsdlReturnValueMapping.getWsdlMessagePartName();
            if (outParamNames.contains(wsdlMessagePartName)) {
                throw new DeploymentException("output message part " + wsdlMessagePartName + " has both an INOUT or OUT mapping and a return value mapping for operation " + operationName);
            }

            if (wrappedStyle) {
                Part outPart = getWrappedPart(output);
                SchemaParticle returnParticle = getWrapperChild(outPart, wsdlMessagePartName);
                //TODO this makes little sense but may be correct, see comments in axis Parameter class
                //the part name qname is really odd.
                returnQName = new QName("", returnParticle.getName().getLocalPart());
                returnType = returnParticle.getType().getName();
            } else if (rpcStyle) {
                Part part = output.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }
                returnQName = new QName("", part.getName());
                returnType = part.getTypeName();
            } else {
                Part part = output.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }
                returnQName = getPartName(part);
                returnType = returnQName;
            }

            outParamNames.add(wsdlMessagePartName);
        } else {
            //what does this mean????
        }

        operationDesc.setReturnQName(returnQName);
        operationDesc.setReturnType(returnType);
        operationDesc.setReturnClass(returnClass);
    }

    private ParameterDesc mapParameter(MethodParamPartsMapping paramMapping) throws DeploymentException {
        WsdlMessageMapping wsdlMessageMapping = paramMapping.getWsdlMessageMapping();
        QName wsdlMessageQName = wsdlMessageMapping.getWsdlMessage();
        String wsdlMessagePartName = wsdlMessageMapping.getWsdlMessagePartName();

        String parameterMode = wsdlMessageMapping.getParameterMode();
        byte mode = ParameterDesc.modeFromString(parameterMode);
        boolean isInParam = mode == ParameterDesc.IN || mode == ParameterDesc.INOUT;
        boolean isOutParam = mode == ParameterDesc.OUT || mode == ParameterDesc.INOUT;

        if (isOutParam && output == null) {
            throw new DeploymentException("Mapping for output parameter " + wsdlMessagePartName + " found, but no output message for operation " + operationName);
        }
        boolean isSoapHeader = wsdlMessageMapping.getSoapHeader() != null;
        boolean inHeader = isSoapHeader && isInParam;
        boolean outHeader = isSoapHeader && isOutParam;

        QName paramQName;
        QName paramTypeQName;

        Part part = null;
        SchemaParticle inParameter = null;
        if (isInParam) {
            if (!wsdlMessageQName.equals(input.getQName())) {
                throw new DeploymentException("QName of input message: " + input.getQName() +
                        " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
            }
            if (wrappedStyle) {
                Part inPart = getWrappedPart(input);
                // the local name of the global element refered by the part is equal to the operation name
                QName name = inPart.getElementName();
                if (false == name.getLocalPart().equals(operationName)) {
                    throw new DeploymentException("message " + input.getQName() + " refers to a global element named " +
                            name.getLocalPart() + ", which is not equal to the operation name " + operationName);
                }
                inParameter = getWrapperChild(inPart, wsdlMessagePartName);
                //TODO this makes little sense but may be correct, see comments in axis Parameter class
                //the part name qname is really odd.
                paramQName = new QName("", inParameter.getName().getLocalPart());
                paramTypeQName = inParameter.getType().getName();
            } else if (rpcStyle) {
                part = input.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in input message for operation " + operationName);
                }
                //TODO this makes little sense but may be correct, see comments in axis Parameter class
                //the part name qname is really odd.
                paramQName = new QName("", part.getName());
                paramTypeQName = part.getTypeName();
            } else {
                part = input.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in input message for operation " + operationName);
                }
                paramQName = getPartName(part);
                paramTypeQName = paramQName;
            }
            inParamNames.add(wsdlMessagePartName);
            if (isOutParam) {
                if (wrappedStyle) {
                    Part outPart = getWrappedPart(output);
                    SchemaParticle outParameter = getWrapperChild(outPart, wsdlMessagePartName);
                    if (inParameter.getType() != outParameter.getType()) {
                        throw new DeploymentException("The wrapper children " + wsdlMessagePartName +
                                " do not have the same type for operation " + operationName);
                    }
                } else if (rpcStyle) {
                    //inout, check that part of same name and type is in output message
                    Part outPart = output.getPart(wsdlMessagePartName);
                    if (outPart == null) {
                        throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for INOUT parameter of operation " + operationName);
                    }
                    // TODO this cannot happen.
                    if (!part.getName().equals(outPart.getName())) {
                        throw new DeploymentException("Mismatched input part name: " + part.getName() + " and output part name: " + outPart.getName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    if (!(part.getElementName() == null ? outPart.getElementName() == null : part.getElementName().equals(outPart.getElementName()))) {
                        throw new DeploymentException("Mismatched input part element name: " + part.getElementName() + " and output part element name: " + outPart.getElementName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    if (!(part.getTypeName() == null ? outPart.getTypeName() == null : part.getTypeName().equals(outPart.getTypeName()))) {
                        throw new DeploymentException("Mismatched input part type name: " + part.getTypeName() + " and output part type name: " + outPart.getTypeName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                } else {
                    part = output.getPart(wsdlMessagePartName);
                    if (part == null) {
                        throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                    }
                    paramQName = getPartName(part);
                    paramTypeQName = paramQName;
                }
                outParamNames.add(wsdlMessagePartName);
            }
        } else if (isOutParam) {
            if (!wsdlMessageQName.equals(output.getQName())) {
                throw new DeploymentException("QName of output message: " + output.getQName() +
                        " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
            }
            if (wrappedStyle) {
                Part outPart = getWrappedPart(output);
                SchemaParticle outParameter = getWrapperChild(outPart, wsdlMessagePartName);
                //TODO this makes little sense but may be correct, see comments in axis Parameter class
                //the part name qname is really odd.
                paramQName = new QName("", outParameter.getName().getLocalPart());
                paramTypeQName = outParameter.getType().getName();
            } else if (rpcStyle) {
                part = output.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }
                //TODO this makes little sense but may be correct, see comments in axis Parameter class
                //the part name qname is really odd.
                paramQName = new QName("", part.getName());
                paramTypeQName = part.getTypeName();
            } else {
                part = output.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new DeploymentException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }
                paramQName = getPartName(part);
                paramTypeQName = paramQName;
            }
            outParamNames.add(wsdlMessagePartName);
        } else {
            throw new AssertionError("a param mapping has to be IN or OUT or INOUT");
        }

        //use complexTypeMap
        boolean isComplexType = schemaInfoBuilder.getComplexTypesInWsdl().containsKey(paramTypeQName);
        String paramJavaTypeName = paramMapping.getParamType();
        boolean isInOnly = mode == ParameterDesc.IN;
        Class actualParamJavaType = WSDescriptorParser.getHolder(paramJavaTypeName, isInOnly, paramTypeQName, isComplexType, mapping, bundle);

        ParameterDesc parameterDesc = new ParameterDesc(paramQName, mode, paramTypeQName, actualParamJavaType, inHeader, outHeader);
        return parameterDesc;
    }

    private QName getPartName(Part part) {
        return null == part.getElementName() ? part.getTypeName() : part.getElementName();
    }

    private Part getWrappedPart(Message message) throws DeploymentException {
        // in case of wrapped element, the message has only one part.
        Collection parts = message.getParts().values();
        if (1 != parts.size()) {
            throw new DeploymentException("message " + message.getQName() + " has " + parts.size() +
                    " parts and should only have one as wrapper style mapping is specified for operation " +
                    operationName);
        }
        return (Part) parts.iterator().next();
    }

    private SchemaParticle getWrapperChild(Part part, String wsdlMessagePartName) throws DeploymentException {
        QName name = part.getElementName();

        wrapperElementQNames.add(name);

        SchemaType operationType = (SchemaType) schemaInfoBuilder.getComplexTypesInWsdl().get(name);
        if (null == operationType) {
            throw new DeploymentException("No global element named " + name + " for operation " + operationName);
        }

        // schemaType should be complex using xsd:sequence compositor
        SchemaParticle parametersType = operationType.getContentModel();
        if (SchemaParticle.ELEMENT == parametersType.getParticleType()) {
            if (parametersType.getName().getLocalPart().equals(wsdlMessagePartName)) {
                return parametersType;
            }
            throw new DeploymentException("Global element named " + name +
                    " does not define a child element named " + wsdlMessagePartName +
                    " required by the operation " + operationName);
        } else if (SchemaParticle.SEQUENCE == parametersType.getParticleType()) {
            SchemaParticle[] parameters = parametersType.getParticleChildren();
            for (int i = 0; i < parameters.length; i++) {
                SchemaParticle parameter = parameters[i];
                QName element = parameter.getName();
                if (element.getLocalPart().equals(wsdlMessagePartName)) {
                    return parameter;
                }
            }
            throw new DeploymentException("Global element named " + name +
                    " does not define a child element named " + wsdlMessagePartName +
                    " required by the operation " + operationName);
        } else {
            throw new DeploymentException("Global element named " + name +
                    " is not a sequence for operation " + operationName);
        }
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
