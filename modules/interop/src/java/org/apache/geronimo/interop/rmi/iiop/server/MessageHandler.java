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
package org.apache.geronimo.interop.rmi.iiop.server;

import java.net.InetAddress;
import java.net.Socket;

import org.apache.geronimo.interop.GIOP.*;
import org.apache.geronimo.interop.IOP.*;
import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.adapter.Adapter;
import org.apache.geronimo.interop.adapter.AdapterManager;
import org.apache.geronimo.interop.naming.NameService;
import org.apache.geronimo.interop.properties.BooleanProperty;
import org.apache.geronimo.interop.properties.SystemProperties;
import org.apache.geronimo.interop.rmi.iiop.BadMagicException;
import org.apache.geronimo.interop.rmi.iiop.CdrInputStream;
import org.apache.geronimo.interop.rmi.iiop.CdrOutputStream;
import org.apache.geronimo.interop.rmi.iiop.GiopMessage;
import org.apache.geronimo.interop.rmi.iiop.ListenerInfo;
import org.apache.geronimo.interop.rmi.iiop.UnsupportedProtocolVersionException;
import org.apache.geronimo.interop.util.ExceptionUtil;
import org.apache.geronimo.interop.util.ThreadContext;
import org.apache.geronimo.interop.util.UTF8;


public class MessageHandler extends Thread {
    public static MessageHandler getInstance(ListenerInfo listenerInfo, Socket socket) {
        MessageHandler object = new MessageHandler();
        object.init(listenerInfo, socket);
        return object;
    }

    // -----------------------------------------------------------------------
    // properties
    // -----------------------------------------------------------------------


    public static final BooleanProperty simpleIDLProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.simpleIDL");

    public static BooleanProperty writeSystemExceptionStackTraceProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.rmi.iiop.writeSystemExceptionStackTrace")
            .defaultValue(true);

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static final boolean SIMPLE_IDL = simpleIDLProperty.getBoolean();

    private static boolean _writeSystemExceptionStackTrace = writeSystemExceptionStackTraceProperty.getBoolean();

    private static RequestHandler[] _handlers = new RequestHandler[128];

    private NameService _nameService;
    private ListenerInfo _listenerInfo;
    private java.net.Socket _socket;
    private java.io.InputStream _socketIn;
    private java.io.OutputStream _socketOut;
    private String _clientHostName;
    private String _clientHostAddress;
    private String _clientInfo;
    private org.apache.geronimo.interop.rmi.iiop.ObjectInputStream _objectInput;
    private org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream _objectOutput;
    private org.apache.geronimo.interop.rmi.iiop.ObjectInputStream _simpleInput;
    private org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream _simpleOutput;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public static void registerHandler(char keyType, RequestHandler handler) {
        _handlers[keyType] = handler;
    }

    public void run() {
        ThreadContext.setDefaultRmiHost(_listenerInfo.host);
        ThreadContext.setDefaultRmiPort(_listenerInfo.port);
        boolean firstMessage = true;
        CdrInputStream input = CdrInputStream.getInstance();
        CdrOutputStream output = CdrOutputStream.getInstance();
        CdrOutputStream results = CdrOutputStream.getInstance();
        for (; ;) {
            boolean sendResponse = true;
            GiopMessage inputMessage;
            try {
                inputMessage = input.receive_message(_socketIn, _clientInfo);
                firstMessage = false;
            } catch (BadMagicException ex) {
                if (firstMessage) {
                    warnBadMagic(_clientInfo, ex);
                } else {
                    warnBadMagicBadSize(_clientInfo, ex);
                }
                closeSocket();
                return;
            } catch (UnsupportedProtocolVersionException ex) {
                warnGiopVersion(_clientInfo, ex);
                closeSocket();
                return;
            } catch (Exception ex) {
                if (input.getOffset() > 0) {
                    ex.printStackTrace();
                    warnReceiveFailed(_clientInfo, ex);
                }
                // Otherwise client shutdown was not in the middle of a
                // request, i.e. probably 'normal' and unworthy of a
                // log message.
                closeSocket();
                return;
            }
            output.setGiopVersion(input.getGiopVersion());
            switch (inputMessage.type) {
                case MsgType_1_1._Request:
                    processRequest(input, output, results, inputMessage.request);
                    if ((inputMessage.request.response_flags & 1) == 0) {
                        sendResponse = false; // oneway request
                    }
                    break;
                case MsgType_1_1._LocateRequest:
                    processLocateRequest(output, inputMessage.locateRequest);
                    break;
                default:
                    throw new SystemException("TODO: message type = " + inputMessage.type);
            }
            if (sendResponse) {
                try {
                    output.send_message(_socketOut, _clientInfo);
                } catch (Exception ex) {
                    warnSendFailed(_clientInfo, ex);
                    closeSocket();
                    return;
                }
            }
            input.reset();
            output.reset();
            results.reset();
        }
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(ListenerInfo listenerInfo, Socket socket) {
        setDaemon(true);
        _nameService = NameService.getInstance();
        _listenerInfo = listenerInfo;
        _socket = socket;
        try {
            _socketIn = _socket.getInputStream();
            _socketOut = _socket.getOutputStream();
            InetAddress addr = _socket.getInetAddress();
            _clientHostName = addr.getHostName();
            _clientHostAddress = addr.getHostAddress();
            _clientInfo = _clientHostName;
            if (!_clientHostAddress.equals(_clientHostName)) {
                _clientInfo += " (" + _clientHostAddress + ")";
            }
        } catch (Throwable ex) {
            closeSocket();
            throw ExceptionUtil.rethrow(ex);
        }
    }

    protected void closeSocket() {
        try {
            if (_socketIn != null) {
                _socketIn.close();
            }
        } catch (Exception ignore) {
        }
        try {
            if (_socketOut != null) {
                _socketOut.close();
            }
        } catch (Exception ignore) {
        }
        try {
            _socket.close();
        } catch (Exception ignore) {
        }
    }

    protected byte[] getObjectKey(TargetAddress target) {
        switch (target.discriminator()) {
            case KeyAddr.value:
                return target.object_key();
            case ProfileAddr.value:
            case ReferenceAddr.value:
                throw new SystemException("TODO");
            default:
                throw new IllegalArgumentException("target discriminator = " + target.discriminator());
        }
    }

    protected void processRequest(CdrInputStream parameters, CdrOutputStream output, CdrOutputStream results, RequestHeader_1_2 request) {
        byte[] objectKey = getObjectKey(request.target);
        int keyLength = objectKey.length;
        int keyType = keyLength == 0 ? 0 : objectKey[0];
        if (keyType >= 'A' && keyType <= 'Z') {
            RequestHandler handler = _handlers[keyType];
            if (handler != null) {
                handler.processRequest(objectKey, request.operation, parameters, output);
                return;
            }
        }

        ReplyHeader_1_2 reply = new ReplyHeader_1_2();
        reply.request_id = request.request_id;

        org.apache.geronimo.interop.rmi.iiop.ObjectInputStream objectIn;
        org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream objectOut;

        if (SIMPLE_IDL || keyType == 'N' || keyType == 'J') {
            // Name Service and JMS use simple IDL interoperability.
            objectIn = org.apache.geronimo.interop.rmi.iiop.SimpleObjectInputStream.getInstance(parameters);
            objectOut = org.apache.geronimo.interop.rmi.iiop.SimpleObjectOutputStream.getInstance(results);
        } else {
            // Otherwise use RMI-IIOP interoperability.
            objectIn = org.apache.geronimo.interop.rmi.iiop.ObjectInputStream.getInstance(parameters);
            objectOut = org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream.getInstance(results);
        }

        try {
            String objectName = null;
            for (int colonPos = 0; colonPos < keyLength; colonPos++) {
                if (objectKey[colonPos] == ':') {
                    objectName = UTF8.toString(objectKey, 0, colonPos);
                    int newKeyLength = keyLength - colonPos - 1;
                    byte[] newObjectKey = new byte[newKeyLength];
                    System.arraycopy(objectKey, colonPos + 1, newObjectKey, 0, newKeyLength);
                    objectKey = newObjectKey;
                    break;
                }
            }

            if (objectName == null) {
                objectName = UTF8.toString(objectKey);
            }

            /*
            if (objectName.startsWith("EJB~"))
            {
                // Compact encoding of component class names,
                // saves 11 bytes per request.
                objectName = "ejb.components." + objectName.substring(4);
            }
            */

            processServiceContext(request);

            /*
            Object object;
            try
            {
                object = null; //_nameService.lookup(objectName);
            }
            catch (javax.naming.NameNotFoundException notFound)
            {
                warnLookupFailed(_clientInfo, notFound);
                throw new org.omg.CORBA.OBJECT_NOT_EXIST(objectName);
            }

            if (object instanceof RemoteInterface)
            {
                RemoteInterface skeleton = ((RemoteInterface)object).$getSkeleton();
                skeleton.$invoke(request.operation, objectKey, objectIn, objectOut);
                if (objectOut.hasException())
                {
                    reply.reply_status = ReplyStatusType_1_2.USER_EXCEPTION;
                }
                else
                {
                    reply.reply_status = ReplyStatusType_1_2.NO_EXCEPTION;
                }
                output.write_reply(reply, results);
            }
            else
            {
                warnInvokeFailedNoRemoteInterface(_clientInfo, object, object.getClass());
                throw new org.omg.CORBA.OBJECT_NOT_EXIST(objectName);
            }
            */

            Object object;
            try {
                object = _nameService.lookup(objectName);
            } catch (javax.naming.NameNotFoundException notFound) {
                object = AdapterManager.getInstance().getAdapter(objectName);

                if (object == null) {
                    warnLookupFailed(_clientInfo, notFound);
                    throw new org.omg.CORBA.OBJECT_NOT_EXIST(objectName);
                }
            }

//            Adapter a = AdapterManager.getInstance().getAdapter(objectName);
//            if (a != null)
            if (object != null && object instanceof Adapter) {
                Adapter a = (Adapter) object;
                //RemoteInterface skeleton = a.getRemoteInterface();
                a.invoke(request.operation, objectKey, objectIn, objectOut);

                if (objectOut.hasException()) {
                    reply.reply_status = ReplyStatusType_1_2.USER_EXCEPTION;
                } else {
                    reply.reply_status = ReplyStatusType_1_2.NO_EXCEPTION;
                }
                output.write_reply(reply, results);
            } else {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST(objectName);
            }
        } catch (Exception ex) {
            warnSystemException(_clientInfo, ex);
            results = CdrOutputStream.getInstance(); // in case we already wrote to it
            results.write_SystemException(ex, _writeSystemExceptionStackTrace);
            reply.reply_status = ReplyStatusType_1_2.SYSTEM_EXCEPTION;
            output.write_reply(reply, results);
        }
    }

    protected void processLocateRequest(CdrOutputStream output, LocateRequestHeader_1_2 request) {
        // Fake LocateReply, pretend we host any object.
        // Since we never move objects, this is sufficient.
        LocateReplyHeader_1_2 reply = new LocateReplyHeader_1_2();
        reply.request_id = request.request_id;
        reply.locate_status = LocateStatusType_1_2.OBJECT_HERE;
        output.write_reply(reply);
    }

    protected void processServiceContext(RequestHeader_1_2 request) {
        ServiceContext[] contextList = request.service_context;
        int n = contextList.length;
        String username = null;
        String password = null;

        for (int i = 0; i < n; i++) {
            ServiceContext context = contextList[i];
            int tag = context.context_id;

            /*
            if (tag == SecurityInfo.TAG_USERNAME)
            {
                username = SecurityInfo.decode(context.context_data);
            }
            else if (tag == SecurityInfo.TAG_PASSWORD)
            {
                password = SecurityInfo.decode(context.context_data);
            }
            */
            // Otherwise OK to ignore unknown tags.
        }

        // Default security info.
        /*
        if (username == null)
        {
            username = User.GUEST;
        }
        if (password == null)
        {
            password = "";
        }

        // Check if the password is correct.
        User user = User.getInstance(username);
        user.login(password); // may throw SecurityException
        User.setCurrent(user);
        SimpleSubject.setCurrent(new SimpleSubject(username, password));
        */
    }

    // log methods

    protected void warnBadMagic(String clientHost, Exception ex) {
        System.out.println("MH.warnBadMagic: clientHost: " + clientHost + ", ex = " + ex);
    }

    protected void warnBadMagicBadSize(String clientHost, Exception ex) {
        System.out.println("MH.warnBadMagicBadSize: clientHost: " + clientHost + ", ex = " + ex);
    }

    protected void warnGiopVersion(String clientHost, Exception ex) {
        System.out.println("MH.warnGiopVersion: clientHost: " + clientHost + ", ex = " + ex);
    }

    protected void warnInvokeFailedNoRemoteInterface(String clientHost, Object object, Class type) {
        System.out.println("MH.warnInvokeFailedNoRemoteInterface: clientHost: " + clientHost + ", object = " + object + ", type: " + type);
    }

    protected void warnLookupFailed(String clientHost, Exception ex) {
        System.out.println("MH.warnLookupFailed: clientHost: " + clientHost + ", ex = " + ex);
    }

    protected void warnReceiveFailed(String clientHost, Exception ex) {
        System.out.println("MH.warnReceiveFailed: clientHost: " + clientHost + ", ex = " + ex);
    }

    protected void warnSendFailed(String clientHost, Exception ex) {
        System.out.println("MH.warnSendFailed: clientHost: " + clientHost + ", ex = " + ex);
    }

    protected void warnSystemException(String clientHost, Exception ex) {
        System.out.println("MH.warnSystemException: clientHost: " + clientHost + ", ex = " + ex);
    }

}
