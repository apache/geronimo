/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.properties.BooleanProperty;
import org.apache.geronimo.interop.properties.IntProperty;
import org.apache.geronimo.interop.properties.PropertyMap;
import org.apache.geronimo.interop.properties.SystemProperties;
import org.apache.geronimo.interop.rmi.iiop.BadMagicException;
import org.apache.geronimo.interop.rmi.iiop.CdrInputStream;
import org.apache.geronimo.interop.rmi.iiop.CdrOutputStream;
import org.apache.geronimo.interop.rmi.iiop.GiopMessage;
import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.apache.geronimo.interop.rmi.iiop.SecurityInfo;
import org.apache.geronimo.interop.rmi.iiop.SimpleObjectInputStream;
import org.apache.geronimo.interop.rmi.iiop.UnsupportedProtocolVersionException;
import org.apache.geronimo.interop.util.ExceptionUtil;
import org.apache.geronimo.interop.util.InstancePool;
import org.apache.geronimo.interop.util.StringUtil;
import org.apache.geronimo.interop.util.ThreadContext;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.ReplyHeader_1_2;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.GIOP.RequestHeader_1_2;
import org.omg.GIOP.SystemExceptionReplyBody;
import org.omg.GIOP.SystemExceptionReplyBodyHelper;
import org.omg.GIOP.TargetAddress;
import org.omg.IOP.ServiceContext;

public class Connection
{

    private static final byte reservedBA[] = new byte[] { 0, 0, 0};
    private int requestid_ = 0;

    // http tunnelling related
    private boolean httpTunnelled;
    private String  httpHeaders;
    private String  webProxyHost;
    private int     webProxyPort;

    public Connection()
    {
    }

    public static Connection getInstance(String endpoint, ObjectRef objectRef, PropertyMap connProps)
    {
        Connection conn = new Connection();
        conn.init(endpoint, objectRef, connProps);
        return conn;
    }

    public static final BooleanProperty simpleIDLProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.simpleIDL");

    public static final IntProperty socketTimeoutProperty =
            new IntProperty(Connection.class, "socketTimeout")
            .defaultValue(600); // 10 minutes

    private static final boolean SIMPLE_IDL = simpleIDLProperty.getBoolean();

    private static final ServiceContext[] EMPTY_SERVICE_CONTEXT = {};

    private static final byte[] CODE_SET_ENCAPSULATION =
    {
        (byte)0, // big endian
        (byte)0, (byte)0, (byte)0, // padding
        (byte)0x05, (byte)0x01, (byte)0x00, (byte)0x01, // 0x05010001 = CodeSet ID for UTF-8
        (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x09, // 0x00010109 = CodeSet ID for UTF-16
    };

    private static final ServiceContext CODE_SET_SERVICE_CONTEXT = new ServiceContext(1, CODE_SET_ENCAPSULATION);

    private String              url;

    private boolean             ok;

    private InstancePool        pool;

    private Socket              socket;

    protected org.apache.geronimo.interop.rmi.iiop.ObjectInputStream input;

    protected org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output;

    private CdrOutputStream     parameters;

    private CdrOutputStream     requestOut;

    private CdrInputStream      results;

    private String              exceptionType;

    private Exception           exception;

    private RequestHeader_1_2   requestHeader;

    private int                 callForget;


    protected java.io.InputStream   socketIn;

    protected java.io.OutputStream  socketOut;

    public String getInstanceName()
    {
        return url;
    }

    public void close()
    {
        parameters = null;
        input = null;
        output = null;
        if (ok)
        {
            pool.put(this);
        }
        else
        {
            shutdown();
        }
    }

    public void beforeInvoke() {
        ok = false;
        parameters = CdrOutputStream.getInstance();
    }

    public void forget(Object requestKey) {
    }

    public void invoke(ObjectRef object, String method, Object requestKey, int retryCount)
    {
        if(object.$getForwardingAddress() != null)
        {
            object = object.$getForwardingAddress();
        }

        RequestHeader_1_2 request = requestHeader;

        request.request_id = requestid_++;
        request.response_flags = 3;
        request.target = new TargetAddress();
        request.target.object_key(object.$getObjectKey());
        request.operation = method;
        request.service_context = getServiceContext(object, requestKey, retryCount);
        request.reserved = reservedBA;  // Sun's generated org.omg.GIOP.RequestHeader_1_2Helper wants this....

        if (requestOut == null)
        {
            requestOut = CdrOutputStream.getInstance();
        }
        requestOut.write_request(request, parameters);


        try
        {
            if(httpTunnelled)
            {
                requestOut.send_http_request(socketOut, url, httpHeaders);
            }
            else
            {
                requestOut.send_message(socketOut, url);
            }
        } catch (RuntimeException ex) {
            throw ex;
        }

        requestOut.reset();

        if (results == null)
        {
            results = CdrInputStream.getInstance();
        }
        else
        {
            results.reset();
        }

        results.setNamingContext(object.$getNamingContext());
        GiopMessage message;
        try
        {
            if(httpTunnelled)
            {
                message = results.receive_http_response(socketIn, url);
            }
            else
            {
                message = results.receive_message(socketIn, url);//_serverHost);
            }
        }
        catch (BadMagicException ex)
        {
            throw new SystemException(ex);
        }
        catch (UnsupportedProtocolVersionException ex)
        {
            throw new SystemException(ex);
        }
        catch (RuntimeException ex)
        {
            throw new RetryInvokeException(ex);
        }

        switch (message.type)
        {
          case MsgType_1_1._Reply:
                processReply(message.reply, object);
                break;

            default:
                throw new SystemException("TODO: message type = " + message.type);
        }

        ok = true;
    }

    public InstancePool getInstancePool() {
        return pool;
    }

    public void setInstancePool(InstancePool pool) {
        this.pool = pool;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectInputStream getInputStream() {
        if (SIMPLE_IDL) {
            return getSimpleInputStream();
        }
        if (input == null) {
            input = org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.getInstance(results);
        }
        return input;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream getOutputStream() {
        if (SIMPLE_IDL) {
            return getSimpleOutputStream();
        }
        if (output == null) {
            output = org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.getInstance(parameters);
        }
        return output;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectInputStream getSimpleInputStream() {
        if (input == null) {
            input = org.apache.geronimo.interop.rmi.iiop.SimpleObjectInputStream.getInstance(results);
        }
        return input;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream getSimpleOutputStream() {
        if (output == null) {
            output = org.apache.geronimo.interop.rmi.iiop.SimpleObjectOutputStream.getInstance(parameters);
        }
        return output;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public Exception getException() {
        if (exception == null) {
            if (exceptionType != null) {
                return new SystemException(exceptionType, new org.omg.CORBA.UNKNOWN());
            } else {
                throw new IllegalStateException("no exception");
            }
        } else {
            return exception;
        }
    }

    public void clearException()
    {
        exceptionType = null;
        exception = null;
    }

    // TODO: check why we have 'objectRef' parameter???
    protected void init(String endpoint, ObjectRef objectRef, PropertyMap connProps)
    {
        setHttpTunnelledPropsIfTrue(connProps);

        if(httpTunnelled)
        {
            httpInit(endpoint, connProps);
            return;
        }

        url = "iiop://" + endpoint;
        UrlInfo urlInfo = UrlInfo.getInstance(url);
        String host = urlInfo.getHost();
        int port = urlInfo.getPort();
        int socketTimeout = socketTimeoutProperty.getInt(endpoint, connProps);
        try
        {
            socket = new Socket(host, port);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
            socket.setSoTimeout(1000 * socketTimeout);
        }
        catch (Exception ex)
        {
            throw new SystemException(ex);
        }
        requestHeader = new RequestHeader_1_2();
        requestHeader.reserved = reservedBA;
    }

    private void httpInit(String endpoint, PropertyMap connProps)
    {
        String host = null;
        int port;
        url = "iiop://" + endpoint;
        int socketTimeout = socketTimeoutProperty.getInt(endpoint, connProps);

        if(webProxyHost != null)
        {
            host = webProxyHost;
            port = webProxyPort;
        }
        else
        {
            UrlInfo urlInfo = UrlInfo.getInstance(url);
            host = urlInfo.getHost();
            port = urlInfo.getPort();
        }

        try
        {
            socket = new Socket(host, port);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
            socket.setSoTimeout(1000 * socketTimeout);
        }
        catch (IOException ex)
        {
            throw new SystemException(ex);
        }
        requestHeader = new RequestHeader_1_2();
        requestHeader.reserved = reservedBA;
    }

    public ServiceContext[] getServiceContext(ObjectRef object, Object requestKey, int retryCount) {
        String username;
        String password;
        SecurityInfo securityInfo = SecurityInfo.getCurrent();
        if (securityInfo == null) {
            ClientNamingContext namingContext = object.$getNamingContext();
            if (namingContext != null) {
                username = namingContext.getUsername();
                password = namingContext.getPassword();
            } else {
                username = null;
                password = null;
            }
        } else {
            username = securityInfo.username;
            password = securityInfo.password;
        }
        if (username != null && username.length() == 0) {
            username = null; // Save network bandwidth in service context.
        }
        if (password != null && password.length() == 0) {
            password = null; // Save network bandwidth in service context.
        }
        int count = 0;
        if (username != null) {
            count++;
        }
        if (password != null) {
            count++;
        }
        if (requestKey != null) {
            count++;
        }
        ServiceContext[] context = new ServiceContext[count];
        int index = 0;
        context[index++] = CODE_SET_SERVICE_CONTEXT;
        if (username != null) {
            context[index++] = new ServiceContext(SecurityInfo.TAG_USERNAME, SecurityInfo.encode(username));
        }
        if (password != null) {
            context[index++] = new ServiceContext(SecurityInfo.TAG_PASSWORD, SecurityInfo.encode(password));
        }
        return context;
    }

    protected void processReply(ReplyHeader_1_2 reply, ObjectRef object)
    {
        processReplyServiceContext(reply);
        int status = reply.reply_status.value();
        switch (status)
        {
          case ReplyStatusType_1_2._NO_EXCEPTION:
            processNormalReply(reply);
            break;
          case ReplyStatusType_1_2._USER_EXCEPTION:
            processUserException(reply);
            break;
          case ReplyStatusType_1_2._SYSTEM_EXCEPTION:
            processSystemException(reply);
            break;
          case ReplyStatusType_1_2._LOCATION_FORWARD:
            processLocationForward(reply, object);
            break;
          case ReplyStatusType_1_2._LOCATION_FORWARD_PERM:
            processLocationForward(reply, object);
            break;
          case ReplyStatusType_1_2._NEEDS_ADDRESSING_MODE:
            throw new SystemException("TODO");
          default:
            throw new SystemException("reply status = " + status);
        }
    }

    protected void processLocationForward(ReplyHeader_1_2 reply, ObjectRef object)
    {
        ObjectRef ref = (ObjectRef)results.read_Object();
        object.$setForwardingAddress(ref);
        throw new RetryInvokeException(new RuntimeException("LOCATION_FORWARD"));
    }

    protected void processReplyServiceContext(ReplyHeader_1_2 reply) {
        ServiceContext[] list = reply.service_context;
        int n = list.length;
        for (int i = 0; i < n; i++) {
            ServiceContext sc = list[i];
            if (sc.context_id == 0xCFCFCFCF
                || sc.context_id == 0xDFDFDFDF) {
                // "CF..." indicates "Call Forget Request"
                // "DF..." indicates "Call Forget Response"
                callForget = sc.context_id;
            }
        }
    }

    protected void processNormalReply(ReplyHeader_1_2 reply) {
        // Intentionally empty.
    }

    protected void processUserException(ReplyHeader_1_2 reply)
    {
        exception = null;
        String type = results.read_string();
        type = StringUtil.removePrefix(type, "IDL:");
        type = StringUtil.removeSuffix(type, ":1.0");
        if (! (input instanceof SimpleObjectInputStream))
        {
            if (type.endsWith("Ex"))
            {
                type = StringUtil.removeSuffix(type, "Ex") + "Exception";
            }
        }
        type = type.replace('/', '.');
        exceptionType = type;
        ok = true;
    }

    protected void processSystemException(ReplyHeader_1_2 reply)
    {
        exceptionType = "???";
        SystemExceptionReplyBody replyBody = SystemExceptionReplyBodyHelper.read(results);
        String id = replyBody.exception_id;
        id = StringUtil.removePrefix(id, "IDL:CORBA/"); // ancient servers might send this!
        id = StringUtil.removePrefix(id, "IDL:omg.org/CORBA/");
        id = StringUtil.removeSuffix(id, ":1.0");
        String causedBy = null;
        if (results.hasMoreData())
        {
            // This is non-standard for IIOP, but if the data isn't present,
            // we wont try to read it!
            causedBy = ExceptionUtil.causedBy(results.read_string());
        }
        ok = true;
        String exceptionClassName = "org.omg.CORBA." + id;
        try
        {
            Class exceptionClass = ThreadContext.loadClass(exceptionClassName);
            Constructor constructor = exceptionClass.getConstructor
            (
                new Class[] { String.class }
            );
            org.omg.CORBA.SystemException corbaException;
            corbaException = (org.omg.CORBA.SystemException)constructor.newInstance
            (
                new Object[] { causedBy == null ? "" : causedBy }
            );
            corbaException.minor = replyBody.minor_code_value;
            corbaException.completed = org.omg.CORBA.CompletionStatus.from_int(replyBody.completion_status);
            exception = corbaException;
        }
        catch (Exception ex)
        {
            // Shouldn't happen, but just in case
            ex.printStackTrace();
            if (causedBy == null)
            {
                causedBy = replyBody.exception_id;
            }
            else
            {
                causedBy = replyBody.exception_id + "\nCaused by: " + causedBy;
            }
            exception = new org.omg.CORBA.UNKNOWN(causedBy,
                replyBody.minor_code_value,
                org.omg.CORBA.CompletionStatus.from_int(replyBody.completion_status));
        }
    }

    private void setHttpTunnelledPropsIfTrue(PropertyMap connprops)
    {
        if(connprops.get("http") != null)
        {
            httpTunnelled = true;
        }
        else
        {
            httpTunnelled = false;
        }

        if(httpTunnelled)
        {
            // get http extra headers if present
            httpHeaders = connprops.getProperty("HttpExtraHeader");

            if(httpHeaders != null && httpHeaders.toLowerCase().indexOf("user-agent") == -1)
            {
                httpHeaders += "User-Agent: Geronimo/1.0\r\n";
            }

            if(httpHeaders == null)
            {
                httpHeaders = "User-Agent: Geronimo/1.0\r\n";
            }

            //get webproxy host/port if present:
            webProxyHost = connprops.getProperty("WebProxyHost");
            String port = connprops.getProperty("WebProxyPort");

            if(port != null)
            {
                try
                {
                    webProxyPort = java.lang.Integer.parseInt(port);
                }
                catch(java.lang.NumberFormatException e)
                {
                    throw new SystemException(org.apache.geronimo.interop.util.ExceptionUtil.causedBy(e));
                }
            }

            if(port == null && webProxyHost != null)
            {
                webProxyPort = 80;  //default
            }
        }
        else
        {
            webProxyHost = null;
            httpHeaders = null;
        }
    }

    public void shutdown() {
        if (socketOut != null) {
            try {
                socketOut.close();
            } catch (Exception ignore) {
            }
            socketOut = null;
        }
        if (socketIn != null) {
            try {
                socketIn.close();
            } catch (Exception ignore) {
            }
            socketIn = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignore) {
            }
            socket = null;
        }
    }
}
