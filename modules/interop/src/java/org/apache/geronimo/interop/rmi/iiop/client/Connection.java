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

import java.net.Socket;

import org.apache.geronimo.interop.GIOP.*;
import org.apache.geronimo.interop.IOP.*;
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
import org.apache.geronimo.interop.rmi.iiop.UnsupportedProtocolVersionException;
import org.apache.geronimo.interop.util.ExceptionUtil;
import org.apache.geronimo.interop.util.InstancePool;
import org.apache.geronimo.interop.util.StringUtil;
import org.apache.geronimo.interop.util.ThreadContext;
import org.apache.geronimo.interop.util.UTF8;

public class Connection {

    public Connection() {
    }

    public static Connection getInstance(String endpoint, ObjectRef objectRef, PropertyMap connProps) {
        Connection conn = new Connection();
        conn.init(endpoint, objectRef, connProps);
        return conn;
    }

    public static final BooleanProperty simpleIDLProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.simpleIDL");

    public static final IntProperty socketTimeoutProperty =
            new IntProperty(Connection.class, "socketTimeout")
            .defaultValue(SystemProperties.rmiSocketTimeoutProperty.getInt());

    private static final boolean SIMPLE_IDL = false; // simpleIDLProperty.getBoolean();
    private ServiceContext[]    EMPTY_SERVICE_CONTEXT = {};
    private String              url;
    private boolean             ok;
    private InstancePool        pool;
    private String              serverHost;
    private Socket              socket;
    private CdrOutputStream     parameters;
    private CdrOutputStream     requestOut;
    private CdrInputStream      results;
    private String              exceptionType;
    private Exception           exception;
    private RequestHeader_1_2   requestHeader;
    private int                 callForget;

    private org.apache.geronimo.interop.rmi.iiop.ObjectInputStream  input;
    private org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream output;

    protected java.io.InputStream   socketIn;
    protected java.io.OutputStream  socketOut;

    public String getInstanceName() {
        return url;
    }

    public void close() {
        parameters = null;
        input = null;
        output = null;
        if (ok) {
            pool.put(this);
        } else {
            shutdown();
        }
    }

    public void beforeInvoke() {
        ok = false;
        parameters = CdrOutputStream.getInstance();
    }

    public void forget(Object requestKey) {
    }

    public void invoke(ObjectRef object, String method, Object requestKey, int retryCount) {
        RequestHeader_1_2 request = requestHeader;

        System.out.println( "object = " + object );
        System.out.println( "method = " + method );
        System.out.println( "requestKey = " + requestKey );

        request.request_id = 0;
        request.response_flags = 3;
        request.target = new TargetAddress();
        request.target.object_key(object.$getObjectKey());
        request.operation = method;
        request.service_context = getServiceContext(object, requestKey, retryCount);

        request.reserved = new byte[3];  // Sun's generated org.omg.GIOP.RequestHeader_1_2Helper wants this....

        if (requestOut == null) {
            System.out.println( "requestOut == null" );
            requestOut = CdrOutputStream.getInstance();
        }
        System.out.println( "write_request" );
        requestOut.write_request(request, parameters);

        try {
            requestOut.send_message(socketOut, url);//_serverHost);
        } catch (RuntimeException ex) {
            //if (object.$getAutomaticFailover())
            //{
            //    throw new RetryInvokeException(ex);
            //}
            throw ex;
        }

        requestOut.reset();

        if (results == null) {
            results = CdrInputStream.getInstance();
        } else {
            results.reset();
        }

        results.setNamingContext(object.$getNamingContext());
        GiopMessage message;
        try {
            message = results.receive_message(socketIn, url);//_serverHost);
        } catch (BadMagicException ex) {
            throw new SystemException(ex);
        } catch (UnsupportedProtocolVersionException ex) {
            throw new SystemException(ex);
        } catch (RuntimeException ex) {
            throw new RetryInvokeException(ex);
        }

        switch (message.type) {
            case MsgType_1_1._Reply:
                processReply(message.reply);
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

    // TODO: check why we have 'objectRef' parameter???
    protected void init(String endpoint, ObjectRef objectRef, PropertyMap connProps) {
        url = "iiop://" + endpoint;
        UrlInfo urlInfo = UrlInfo.getInstance(url);
        String host = urlInfo.getHost();
        int port = urlInfo.getPort();
        int socketTimeout = socketTimeoutProperty.getInt(endpoint, connProps);
        try {
            socket = new Socket(host, port);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
            socket.setSoTimeout(1000 * socketTimeout);
            serverHost = host;
        } catch (Exception ex) {
            throw new SystemException(errorConnectFailed(host, port, ex));
        }
        requestHeader = new RequestHeader_1_2();
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
        if (count == 0) {
            return EMPTY_SERVICE_CONTEXT; // avoid allocating empty array.
        }
        ServiceContext[] context = new ServiceContext[count];
        int index = 0;
        if (username != null) {
            context[index++] = new ServiceContext(SecurityInfo.TAG_USERNAME, SecurityInfo.encode(username));
        }
        if (password != null) {
            context[index++] = new ServiceContext(SecurityInfo.TAG_PASSWORD, SecurityInfo.encode(password));
        }
        if (requestKey != null) {
            if (retryCount == 0) {
                // 'BF' indicates Before Failure
                context[index++] = new ServiceContext(0xBFBFBFBF, UTF8.fromString((String) requestKey));
            } else {
                // 'AF' indicates After Failure
                context[index++] = new ServiceContext(0xAFAFAFAF, UTF8.fromString((String) requestKey));
            }
        }
        return context;
    }

    protected void processReply(ReplyHeader_1_2 reply) {
        processReplyServiceContext(reply);
        int status = reply.reply_status.value();
        switch (status) {
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
                throw new SystemException("TODO");
            case ReplyStatusType_1_2._LOCATION_FORWARD_PERM:
                throw new SystemException("TODO");
            case ReplyStatusType_1_2._NEEDS_ADDRESSING_MODE:
                throw new SystemException("TODO");
            default:
                throw new SystemException("reply status = " + status);
        }
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

    protected void processUserException(ReplyHeader_1_2 reply) {
        exception = null;
        exceptionType = results.read_string();
        ok = true;
    }

    protected void processSystemException(ReplyHeader_1_2 reply) {
        exceptionType = "???";
        SystemExceptionReplyBody replyBody = SystemExceptionReplyBodyHelper.read(results);
        String id = replyBody.exception_id;
        id = StringUtil.removePrefix(id, "IDL:omg.org/CORBA/");
        id = StringUtil.removeSuffix(id, ":1.0");
        String stackTrace = null;
        if (results.hasMoreData()) {
            stackTrace = results.read_string() + ExceptionUtil.getDivider();
        }
        ok = true;
        String exceptionClassName = "org.omg.CORBA." + id;
        try {
            Class exceptionClass = ThreadContext.loadClass(exceptionClassName);
            org.omg.CORBA.SystemException corbaException = (org.omg.CORBA.SystemException) exceptionClass.newInstance();
            corbaException.minor = replyBody.minor_code_value;
            corbaException.completed = org.omg.CORBA.CompletionStatus.from_int(replyBody.completion_status);
            exception = new org.apache.geronimo.interop.SystemException(stackTrace, corbaException);
        } catch (Exception ex) {
            exception = new org.apache.geronimo.interop.SystemException(stackTrace,
                                                                         new org.omg.CORBA.UNKNOWN(replyBody.exception_id,
                                                                                                   replyBody.minor_code_value,
                                                                                                   org.omg.CORBA.CompletionStatus.from_int(replyBody.completion_status)));
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

    protected String errorConnectFailed(String host, int port, Exception ex) {
        String msg;
        msg = "Error: errorConnectFailed: host=" + host + ", port=" + port + ", ex = " + ex;
        return msg;
    }
}
