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

    // -----------------------------------------------------------------------
    // properties
    // -----------------------------------------------------------------------

    public static final BooleanProperty simpleIDLProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.simpleIDL");

    public static final IntProperty socketTimeoutProperty =
            new IntProperty(Connection.class, "socketTimeout")
            .defaultValue(SystemProperties.rmiSocketTimeoutProperty.getInt());

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static final boolean SIMPLE_IDL = simpleIDLProperty.getBoolean();

    private ServiceContext[] EMPTY_SERVICE_CONTEXT = {};

    private String _url;

    private boolean _ok;

    private InstancePool _pool;

    private String _serverHost;

    private Socket _socket;

    private org.apache.geronimo.interop.rmi.iiop.ObjectInputStream _input;

    private org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream _output;

    private CdrOutputStream _parameters;

    private CdrOutputStream _requestOut;

    private CdrInputStream _results;

    private String _exceptionType;

    private Exception _exception;

    private RequestHeader_1_2 _requestHeader;

    private int _callForget;

    // -----------------------------------------------------------------------
    // protected data
    // -----------------------------------------------------------------------

    protected java.io.InputStream _socketIn;

    protected java.io.OutputStream _socketOut;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public String getInstanceName() {
        return _url;
    }

    public void close() {
        _parameters = null;
//        _results.recycle();
//        _results = null;
        _input = null;
        _output = null;
        if (_ok) {
            _pool.put(this);
        } else {
            shutdown();
        }
    }

    public void beforeInvoke() {
        _ok = false;
        _parameters = CdrOutputStream.getInstance();
    }

    public void forget(Object requestKey) {
        if (_callForget != 0) {
            /*
            String key = (String)requestKey;
            try
            {
                ClusterService cs = _results.getNamingContext().getClusterService();
                if (_callForget == 0xCFCFCFCF)
                {
                    cs.forgetRequest(key);
                }
                else if (_callForget == 0xDFDFDFDF)
                {
                    cs.forgetResponse(key);
                }
            }
            catch (Exception ignore)
            {
                // TODO: log in debug mode?
            }
            */
        }
    }

    public void invoke(ObjectRef object, String method, Object requestKey, int retryCount) {
        _callForget = 0; // see 'forget' and 'processReplyServiceContext'

        RequestHeader_1_2 request = _requestHeader;

        request.request_id = 0;
        request.response_flags = 3;
        request.target = new TargetAddress();
        request.target.object_key(object.$getObjectKey());
        request.operation = method;
        request.service_context = getServiceContext(object, requestKey, retryCount);

        request.reserved = new byte[3];  // Sun's generated org.omg.GIOP.RequestHeader_1_2Helper wants this....

        if (_requestOut == null) {
            _requestOut = CdrOutputStream.getInstance();
        }
        _requestOut.write_request(request, _parameters);

        try {
            _requestOut.send_message(_socketOut, _url);//_serverHost);
        } catch (RuntimeException ex) {
            //if (object.$getAutomaticFailover())
            //{
            //    throw new RetryInvokeException(ex);
            //}
            throw ex;
        }

        _requestOut.reset();

        if (_results == null) {
            _results = CdrInputStream.getInstance();
        } else {
            _results.reset();
        }

        _results.setNamingContext(object.$getNamingContext());
        GiopMessage message;
        try {
            message = _results.receive_message(_socketIn, _url);//_serverHost);
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

        _ok = true;
    }

    public InstancePool getInstancePool() {
        return _pool;
    }

    public void setInstancePool(InstancePool pool) {
        _pool = pool;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectInputStream getInputStream() {
        if (SIMPLE_IDL) {
            return getSimpleInputStream();
        }
        if (_input == null) {
            _input = org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.getInstance(_results);
        }
        return _input;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream getOutputStream() {
        if (SIMPLE_IDL) {
            return getSimpleOutputStream();
        }
        if (_output == null) {
            _output = org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.getInstance(_parameters);
        }
        return _output;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectInputStream getSimpleInputStream() {
        if (_input == null) {
            _input = org.apache.geronimo.interop.rmi.iiop.SimpleObjectInputStream.getInstance(_results);
        }
        return _input;
    }

    public org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream getSimpleOutputStream() {
        if (_output == null) {
            _output = org.apache.geronimo.interop.rmi.iiop.SimpleObjectOutputStream.getInstance(_parameters);
        }
        return _output;
    }

    public String getExceptionType() {
        return _exceptionType;
    }

    public Exception getException() {
        if (_exception == null) {
            if (_exceptionType != null) {
                return new SystemException(_exceptionType, new org.omg.CORBA.UNKNOWN());
            } else {
                throw new IllegalStateException("no exception");
            }
        } else {
            return _exception;
        }
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    // TODO: check why we have 'objectRef' parameter???
    protected void init(String endpoint, ObjectRef objectRef, PropertyMap connProps) {
        _url = "iiop://" + endpoint;
        UrlInfo urlInfo = UrlInfo.getInstance(_url);
        String host = urlInfo.getHost();
        int port = urlInfo.getPort();
        int socketTimeout = socketTimeoutProperty.getInt(endpoint, connProps);
        try {
            _socket = new Socket(host, port);
            _socketIn = _socket.getInputStream();
            _socketOut = _socket.getOutputStream();
            _socket.setSoTimeout(1000 * socketTimeout);
            _serverHost = host;
        } catch (Exception ex) {
            throw new SystemException(errorConnectFailed(host, port, ex));
        }
        _requestHeader = new RequestHeader_1_2();
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
                _callForget = sc.context_id;
            }
        }
    }

    protected void processNormalReply(ReplyHeader_1_2 reply) {
        // Intentionally empty.
    }

    protected void processUserException(ReplyHeader_1_2 reply) {
        _exception = null;
        _exceptionType = _results.read_string();
        _ok = true;
    }

    protected void processSystemException(ReplyHeader_1_2 reply) {
        _exceptionType = "???";
        SystemExceptionReplyBody replyBody = SystemExceptionReplyBodyHelper.read(_results);
        String id = replyBody.exception_id;
        id = StringUtil.removePrefix(id, "IDL:omg.org/CORBA/");
        id = StringUtil.removeSuffix(id, ":1.0");
        String stackTrace = null;
        if (_results.hasMoreData()) {
            stackTrace = _results.read_string() + ExceptionUtil.getDivider();
        }
        _ok = true;
        String exceptionClassName = "org.omg.CORBA." + id;
        try {
            Class exceptionClass = ThreadContext.loadClass(exceptionClassName);
            org.omg.CORBA.SystemException corbaException = (org.omg.CORBA.SystemException) exceptionClass.newInstance();
            corbaException.minor = replyBody.minor_code_value;
            corbaException.completed = org.omg.CORBA.CompletionStatus.from_int(replyBody.completion_status);
            _exception = new org.apache.geronimo.interop.SystemException(stackTrace, corbaException);
        } catch (Exception ex) {
            _exception = new org.apache.geronimo.interop.SystemException(stackTrace,
                                                                         new org.omg.CORBA.UNKNOWN(replyBody.exception_id,
                                                                                                   replyBody.minor_code_value,
                                                                                                   org.omg.CORBA.CompletionStatus.from_int(replyBody.completion_status)));
        }
    }

    public void shutdown() {
        if (_socketOut != null) {
            try {
                _socketOut.close();
            } catch (Exception ignore) {
            }
            _socketOut = null;
        }
        if (_socketIn != null) {
            try {
                _socketIn.close();
            } catch (Exception ignore) {
            }
            _socketIn = null;
        }
        if (_socket != null) {
            try {
                _socket.close();
            } catch (Exception ignore) {
            }
            _socket = null;
        }
    }

    // log methods

    protected String errorConnectFailed(String host, int port, Exception ex) {
        String msg;
        msg = "Error: errorConnectFailed: host=" + host + ", port=" + port + ", ex = " + ex;
        return msg;
    }
}
