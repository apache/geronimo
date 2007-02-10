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

package org.apache.geronimo.axis2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.RequestWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.ResponseWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.ws.commons.schema.XmlSchema;



public class Axis2WebServiceContainer implements WebServiceContainer {

	private static final Log log = LogFactory.getLog(Axis2WebServiceContainer.class);

    public static final String REQUEST = Axis2WebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = Axis2WebServiceContainer.class.getName() + "@Response";

    private transient final ClassLoader classLoader;
    private final String endpointClassName;
    private final org.apache.geronimo.jaxws.PortInfo portInfo;
    private ConfigurationContext configurationContext;
    private String contextRoot = null;
    private Map servicesMap;
    private Definition wsdlDefinition;
    
    
    public Axis2WebServiceContainer(org.apache.geronimo.jaxws.PortInfo portInfo, String endpointClassName, Definition wsdlDefinition, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.portInfo = portInfo;
        this.wsdlDefinition = wsdlDefinition;
        try {
            AxisService service = null;
            
            configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
            configurationContext.setServicePath(portInfo.getLocation());
          
            if(wsdlDefinition != null){ //WSDL Has been provided
           		WSDLToAxisServiceBuilder wsdlBuilder = null;
           		
		        WSDLFactory factory = WSDLFactory.newInstance();
		        WSDLWriter writer = factory.newWSDLWriter();
		        
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        writer.writeWSDL(wsdlDefinition, out);
		        String wsdlContent = out.toString("UTF-8"); //TODO Pass correct encoding from WSDL
		        
           		OMNamespace documentElementNS = ((OMElement)XMLUtils.toOM(new StringReader(wsdlContent))).getNamespace();
           		
           		Map<QName, Service> serviceMap = wsdlDefinition.getServices();
            	Service wsdlService = serviceMap.values().iterator().next();
            	
            	Map<String, Port> portMap = wsdlService.getPorts();
            	Port port = portMap.values().iterator().next();
            	String portName = port.getName();
           		QName serviceQName = wsdlService.getQName();

           		//Decide on WSDL Version : 
            	if(WSDLConstants.WSDL20_2006Constants.DEFAULT_NAMESPACE_URI.equals(documentElementNS.getNamespaceURI())){
            		wsdlBuilder = new WSDL20ToAxisServiceBuilder(new StringBufferInputStream(wsdlContent), serviceQName, null);
            	}
            	else if(Constants.NS_URI_WSDL11.equals(documentElementNS.getNamespaceURI())){
            		wsdlBuilder = new WSDL11ToAxisServiceBuilder(wsdlDefinition, serviceQName , portName);
            	}
            	//populate with axis2 objects
            	service = wsdlBuilder.populateService();
            	service.addParameter(new Parameter("ServiceClass", endpointClassName));
                service.setWsdlFound(true);
                service.setClassLoader(classLoader);
            	
            	//Goind to create annotations by hand
            	DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
            	dbc.setClassLoader(classLoader);
            	HashMap<String, DescriptionBuilderComposite> dbcMap = new HashMap<String, DescriptionBuilderComposite>();
            	
            	//Service related annotations
                WebServiceAnnot serviceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
                serviceAnnot.setPortName(portName);
                serviceAnnot.setServiceName(service.getName());
                serviceAnnot.setName(service.getName());
                serviceAnnot.setTargetNamespace(service.getTargetNamespace());
                //don't set endpointinterface now otherwise you'll get a
                //Validation error: SEI must not set a value for @WebService.endpointInterface.
                //The default value is "".
                //serviceAnnot.setEndpointInterface(endpointClassName);
          	
           	 	Class endPointClass = classLoader.loadClass(endpointClassName);
    	 		Method[] classMethods = endPointClass.getMethods();
    	 		
           	 	for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
           	 		AxisOperation operation = opIterator.next();
           	 		operation.setMessageReceiver(JAXWSMessageReceiver.class.newInstance());
           	 		
           	 		for(Method method : classMethods){
           	 			// TODO Is this correct method?
           	 			String axisOpName = operation.getName().getLocalPart();
           	 			
           	 			if(method.getName().equals(axisOpName)){
           	 				//Method level annotations
           	 				MethodDescriptionComposite mdc = new MethodDescriptionComposite();
           	 				WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
           	 				webMethodAnnot.setOperationName(method.getName());
//           	 				methodAnnot.setAction(operation.get);
//           	 				methodAnnot.setExclude(false);
           	 				
           	 				mdc.setWebMethodAnnot(webMethodAnnot);

           	 				mdc.setMethodName(method.getName());
           	 				mdc.setDescriptionBuilderCompositeRef(dbc);

	               	 		String MEP = operation.getMessageExchangePattern();
	               	 		
	               	 		if (WSDLUtil.isInputPresentForMEP(MEP)) {
	               	 			AxisMessage inAxisMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
	               	 			if(inAxisMessage != null){
		               	 			//TODO: Has to explore more
		               	 			Class[] paramTypes = method.getParameterTypes();
		               	 			int i = 0;
		               	 			for(Class paramType : paramTypes){
		               	 				//Parameter level annotations
		               	 				ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
		               	 				WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();

		               	 				webParamAnnot.setName("arg"+i);
		               	 				webParamAnnot.setName(inAxisMessage.getElementQName().getLocalPart());
		               	 				webParamAnnot.setTargetNamespace(inAxisMessage.getElementQName().getNamespaceURI());
		               	 				
		               	 				pdc.setWebParamAnnot(webParamAnnot);
		               	 				String strParamType = paramType.toString();
		               	 				
		               	 				pdc.setMethodDescriptionCompositeRef(mdc);
		               	 				pdc.setParameterType(strParamType.split(" ")[1]);
		               	 				mdc.addParameterDescriptionComposite(pdc, i);
		               	 				//TODO: Do we need to set these things?
		               	 				RequestWrapperAnnot requestWrapAnnot = RequestWrapperAnnot.createRequestWrapperAnnotImpl();
		               	 				requestWrapAnnot.setLocalName(inAxisMessage.getElementQName().getLocalPart());
		               	 				requestWrapAnnot.setTargetNamespace(inAxisMessage.getElementQName().getNamespaceURI());
		               	 				
		               	 				mdc.setRequestWrapperAnnot(requestWrapAnnot);
		               	 				i++;
		               	 			}
	               	 			}
	               	 		}
	               	 		
	               	 		if (WSDLUtil.isOutputPresentForMEP(MEP)) {
	               	 			AxisMessage outAxisMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
	               	 			
	               	 			if(outAxisMessage != null){
	               	 				mdc.setReturnType(method.getReturnType().toString().split(" ")[1]);
	               	 				//TODO:
	               	 				ResponseWrapperAnnot responseWrapAnnot = ResponseWrapperAnnot.createResponseWrapperAnnotImpl();
	               	 				responseWrapAnnot.setLocalName(outAxisMessage.getElementQName().getLocalPart());
	               	 				responseWrapAnnot.setTargetNamespace(outAxisMessage.getElementQName().getNamespaceURI());
	               	 				mdc.setResponseWrapperAnnot(responseWrapAnnot);
	               	 			}
	               	 		}
	               	 		mdc.setWebMethodAnnot(webMethodAnnot);
	               	 		dbc.addMethodDescriptionComposite(mdc);
	               	 		
           	 				break;
           	 			}
           	 		}
           	 	}
            	
            	dbc.setWebServiceAnnot(serviceAnnot);
           	 	dbc.setWsdlDefinition(wsdlDefinition);
           	 	dbc.setClassName(endpointClassName);
           	 	dbc.setCustomWsdlGenerator(new WSDLGeneratorImpl(wsdlDefinition));
           	 	dbcMap.put(endpointClassName, dbc);
                List<ServiceDescription> serviceDescList = DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
                ServiceDescription sd = serviceDescList.get(0);
                Parameter serviceDescription = new Parameter(EndpointDescription.AXIS_SERVICE_PARAMETER, sd.getEndpointDescriptions()[0]);
                service.addParameter(serviceDescription);
            	        	            	
            }else { //No WSDL, Axis2 will handle it. Is it ?
            	service = AxisService.createService(endpointClassName, configurationContext.getAxisConfiguration(), JAXWSMessageReceiver.class);
            }

            configurationContext.getAxisConfiguration().addService(service);
            
        } catch (AxisFault af) {
            throw new RuntimeException(af);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    public void getWsdl(Request request, Response response) throws Exception {
        doService(request, response);
    }

    public void invoke(Request request, Response response) throws Exception {
        doService(request, response);
    }

    protected void doService(final Request request, final Response response)
            throws Exception {
        initContextRoot(request);

        if (log.isDebugEnabled()) {
        	log.debug("Target URI: " + request.getURI());
        }

        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());
        

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(new QName(Constants.TRANSPORT_HTTP));
            
            

            msgContext.setConfigurationContext(this.configurationContext);

            //TODO: Port this segment for session support.
//            String sessionKey = (String) this.httpcontext.getAttribute(HTTPConstants.COOKIE_STRING);
//            if (this.configurationContext.getAxisConfiguration().isManageTransportSession()) {
//                SessionContext sessionContext = this.sessionManager.getSessionContext(sessionKey);
//                msgContext.setSessionContext(sessionContext);
//            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
            msgContext.setServerSide(true);
            

//            // set the transport Headers
//            HashMap headerMap = new HashMap();
//            for (Iterator it = request.headerIterator(); it.hasNext();) {
//                Header header = (Header) it.next();
//                headerMap.put(header.getName(), header.getValue());
//            }
//            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
//
//            this.httpcontext.setAttribute(AxisParams.MESSAGE_CONTEXT, msgContext);

            doService2(request, response, msgContext);
        } catch (Throwable e) {
            try {
                AxisEngine engine = new AxisEngine(this.configurationContext);

                msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

                MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, e);
                // If the fault is not going along the back channel we should be 202ing
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(202);
                } else {
                    response.setStatusCode(500);
                    response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/plain");
                    PrintWriter pw = new PrintWriter(response.getOutputStream());
                    e.printStackTrace(pw);
                    pw.flush();
                    String msg = "Exception occurred while trying to invoke service method doService()";
                    log.error(msg, e);
                }
                engine.sendFault(faultContext);
            } catch (Exception ex) {
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(202);
                } else {
                    response.setStatusCode(500);
                    response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/plain");
                    PrintWriter pw = new PrintWriter(response.getOutputStream());
                    ex.printStackTrace(pw);
                    pw.flush();
                    String msg = "Exception occurred while trying to invoke service method doService()";
                    log.error(msg, ex);
                }
            }
        }

    }

    private void initContextRoot(Request request) {
        if (contextRoot == null || "".equals(contextRoot)) {
            String[] parts = JavaUtils.split(request.getContextPath(), '/');
            if (parts != null) {
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        contextRoot = parts[i];
                        break;
                    }
                }
            }
            if (contextRoot == null || request.getContextPath().equals("/")) {
                contextRoot = "/";
            }
            configurationContext.setContextRoot(contextRoot);
            
//            Parameter servicePath = new Parameter(Constants.PARAM_SERVICE_PATH, new String(""));
            
        }
    }

    public void doService2(
            final Request request,
            final Response response,
            final MessageContext msgContext) throws Exception {

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        final String servicePath = configurationContext.getServiceContextPath();
        final String contextPath = (servicePath.startsWith("/") ? servicePath : "/" + servicePath);

        URI uri = request.getURI();
        String path = uri.getPath();
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        
        AxisService service = findServiceWithEndPointClassName(configurationContext, endpointClassName);
        String serviceName = service.getName();

        // TODO: Port this section
//        // Adjust version and content chunking based on the config
//        boolean chunked = false;
//        TransportOutDescription transportOut = msgContext.getTransportOut();
//        if (transportOut != null) {
//            Parameter p = transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);
//            if (p != null) {
//                if (HTTPConstants.HEADER_PROTOCOL_10.equals(p.getValue())) {
//                    ver = HttpVersion.HTTP_1_0;
//                }
//            }
//            if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
//                p = transportOut.getParameter(HTTPConstants.HEADER_TRANSFER_ENCODING);
//                if (p != null) {
//                    if (HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(p.getValue())) {
//                        chunked = true;
//                    }
//                }
//            }
//        }
        

        if (request.getMethod() == Request.GET) {
            if (!path.startsWith(contextPath)) {
                response.setStatusCode(301);
                response.setHeader("Location", contextPath);
                return;
            }
            if (uri.toString().indexOf("?") < 0) {
                if (!path.endsWith(contextPath)) {
                    if (serviceName.indexOf("/") < 0) {
                        String res = HTTPTransportReceiver.printServiceHTML(serviceName, configurationContext);
                        PrintWriter pw = new PrintWriter(response.getOutputStream());
                        pw.write(res);
                        pw.flush();
                        return;
                    }
                }
            }
            
            //TODO: Has to implement 
            if (uri.getQuery().startsWith("wsdl2")) {
                if (service != null) {
                    service.printWSDL2(response.getOutputStream());
                    return;
                }
            }
            if (uri.getQuery().startsWith("wsdl")) {
            	if(wsdlDefinition != null){
            		WSDLFactory factory = WSDLFactory.newInstance();
            		WSDLWriter writer = factory.newWSDLWriter();            		
            		writer.writeWSDL(wsdlDefinition, response.getOutputStream());
            		return;
            	}else {
                    service.printWSDL(response.getOutputStream());
                    return;
            	}
            }
            //TODO: Not working properly and do we need to have these requests ?
            if (uri.getQuery().startsWith("xsd=")) {
            	String schemaName = uri.getQuery().substring(uri.getQuery().lastIndexOf("=") + 1);

                if (service != null) {
                    //run the population logic just to be sure
                    service.populateSchemaMappings();
                    //write out the correct schema
                    Map schemaTable = service.getSchemaMappingTable();
                    final XmlSchema schema = (XmlSchema) schemaTable.get(schemaName);
                    //schema found - write it to the stream
                    if (schema != null) {
                        schema.write(response.getOutputStream());
                        return;
                    } else {
                        // no schema available by that name  - send 404
                        response.setStatusCode(404);
                        return;
                    }
                }                
            }
            //cater for named xsds - check for the xsd name
            if (uri.getQuery().startsWith("xsd")) {
            	if (service != null) {
            		response.setContentType("text/xml");
            		response.setHeader("Transfer-Encoding", "chunked");
                    service.printSchema(response.getOutputStream());
                    response.getOutputStream().close();
                    return;
                }
            }

            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

            // deal with GET request
            boolean processed = HTTPTransportUtils.processHTTPGetRequest(
                    msgContext,
                    response.getOutputStream(),
                    soapAction,
                    path,
                    configurationContext,
                    HTTPTransportReceiver.getGetRequestParameters(path));

            if (!processed) {
                response.setStatusCode(200);
                String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                PrintWriter pw = new PrintWriter(response.getOutputStream());
                pw.write(s);
                pw.flush();
            }

        } else if (request.getMethod() == Request.POST) {
            // deal with POST request
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));
            msgContext.setAxisService(service);
            msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                    new Axis2RequestResponseTransport(response));
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, request.getURI().toString());
            msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
            //msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, request);
            //msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, contextPath);
            
            String contenttype = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            HTTPTransportUtils.processHTTPPostRequest(
                    msgContext,
                    request.getInputStream(),
                    response.getOutputStream(),
                    contenttype,
                    soapAction,
                    path);

        } else {
            throw new UnsupportedOperationException("[" + request.getMethod() + " ] method not supported");
        }

        // Finalize response
        OperationContext operationContext = msgContext.getOperationContext();
        Object contextWritten = null;
        Object isTwoChannel = null;
        if (operationContext != null) {
            contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);
            isTwoChannel = operationContext.getProperty(Constants.DIFFERENT_EPR);
        }


        if ((contextWritten != null) && Constants.VALUE_TRUE.equals(contextWritten)) {
            if ((isTwoChannel != null) && Constants.VALUE_TRUE.equals(isTwoChannel)) {
                response.setStatusCode(202);
                return;
            }
            response.setStatusCode(200);
        } else {
            response.setStatusCode(202);
        }
    }
    
    public void destroy() {
	}
    
    /**
     * Resolves the Axis Service associated with the endPointClassName
     * @param cfgCtx
     * @param endPointClassName
     */
    private AxisService findServiceWithEndPointClassName(ConfigurationContext cfgCtx, String endPointClassName) {

        // Visit all the AxisServiceGroups.
        Iterator svcGrpIter = cfgCtx.getAxisConfiguration().getServiceGroups();
        while (svcGrpIter.hasNext()) {

            // Visit all the AxisServices
            AxisServiceGroup svcGrp = (AxisServiceGroup) svcGrpIter.next();
            Iterator svcIter = svcGrp.getServices();
            while (svcIter.hasNext()) {
                AxisService service = (AxisService) svcIter.next();

                // Grab the Parameter that stores the ServiceClass.
                String epc = (String)service.getParameter("ServiceClass").getValue();
                
                if (epc != null) {
                    // If we have a match, then just return the AxisService now.
                    if (endPointClassName.equals(epc)) {
                        return service;
                    }
                }
            }
        }
        return null;
    }    
    
    public class Axis2TransportInfo implements OutTransportInfo {
        private Response response;

        public Axis2TransportInfo(Response response) {
            this.response = response;
        }

        public void setContentType(String contentType) {
            response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);
        }
    }
    
    class Axis2RequestResponseTransport implements RequestResponseTransport
    {
      private Response response;
      private CountDownLatch responseReadySignal = new CountDownLatch(1);
      RequestResponseTransportStatus status = RequestResponseTransportStatus.INITIAL;
      
      Axis2RequestResponseTransport(Response response)
      {
        this.response = response;
}     
      public void acknowledgeMessage(MessageContext msgContext) throws AxisFault
      {
        if (log.isDebugEnabled()) {
            log.debug("acknowledgeMessage");
        }
         
        if (log.isDebugEnabled()) {
            log.debug("Acking one-way request");
        }

        response.setContentType("text/xml; charset="
                                + msgContext.getProperty("message.character-set-encoding"));
        
        response.setStatusCode(202);
        try
        {
          response.flushBuffer();
        }
        catch (IOException e)
        {
          throw new AxisFault("Error sending acknowledgement", e);
        }
        
        signalResponseReady();
      }
      
      public void awaitResponse() throws InterruptedException
      {
        if (log.isDebugEnabled()) {
            log.debug("Blocking servlet thread -- awaiting response");
        }
        status = RequestResponseTransportStatus.WAITING;
        responseReadySignal.await();
      }

      public void signalResponseReady()
      {
        if (log.isDebugEnabled()) {
            log.debug("Signalling response available");
        }
        status = RequestResponseTransportStatus.SIGNALLED;
        responseReadySignal.countDown();
      }

      public RequestResponseTransportStatus getStatus() {
        return status;
      }
    }
    
    class WSDLGeneratorImpl implements WsdlGenerator {

    	private Definition def;
    	
    	public WSDLGeneratorImpl(Definition def) {
    		this.def = def;
    	}
    	
    	public WsdlComposite generateWsdl(String implClass, String bindingType) throws WebServiceException {
    		// Need WSDL generation code
    		WsdlComposite composite = new WsdlComposite();
    		composite.setWsdlFileName(implClass);
    		HashMap<String, Definition> testMap = new HashMap<String, Definition>();
    		testMap.put(composite.getWsdlFileName(), def);
    		composite.setWsdlDefinition(testMap);
    		return composite;
    	}
    }

}
