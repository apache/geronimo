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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.adapter.Adapter;
import org.apache.geronimo.interop.adapter.AdapterManager;
import org.apache.geronimo.interop.naming.NameService;
import org.apache.geronimo.interop.rmi.iiop.BadMagicException;
import org.apache.geronimo.interop.rmi.iiop.CdrInputStream;
import org.apache.geronimo.interop.rmi.iiop.CdrOutputStream;
import org.apache.geronimo.interop.rmi.iiop.GiopMessage;
import org.apache.geronimo.interop.rmi.iiop.ObjectInputStream;
import org.apache.geronimo.interop.rmi.iiop.ObjectOutputStream;
import org.apache.geronimo.interop.rmi.iiop.UnsupportedProtocolVersionException;
import org.apache.geronimo.interop.util.UTF8;
import org.omg.GIOP.KeyAddr;
import org.omg.GIOP.LocateReplyHeader_1_2;
import org.omg.GIOP.LocateRequestHeader_1_2;
import org.omg.GIOP.LocateStatusType_1_2;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.ProfileAddr;
import org.omg.GIOP.ReferenceAddr;
import org.omg.GIOP.ReplyHeader_1_2;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.GIOP.RequestHeader_1_2;
import org.omg.GIOP.TargetAddress;
import org.omg.IOP.ServiceContext;

public class MessageHandler {

    private AdapterManager      adapterManager;
    private boolean             simpleIDL;
    private boolean             writeSystemExceptionStackTrace;
    private NameService         nameService = NameService.getInstance();

    public MessageHandler( AdapterManager adapterManager, boolean simpleIDL,
                           boolean writeSystemExceptionStackTrace )
    {
        this.adapterManager = adapterManager;
        this.simpleIDL = simpleIDL;
        this.writeSystemExceptionStackTrace = writeSystemExceptionStackTrace;
    }

    public void service(Socket socket) throws Exception {

        InputStream in;
        OutputStream out;

        String clientHostName;
        String clientHostAddress;
        String clientInfo;

        in = socket.getInputStream();
        out = socket.getOutputStream();

        InetAddress addr = socket.getInetAddress();
        clientHostName = addr.getHostName();
        clientHostAddress = addr.getHostAddress();
        clientInfo = clientHostName;

        if (!clientHostAddress.equals(clientHostName)) {
            clientInfo += " (" + clientHostAddress + ")";
        }

        boolean firstMessage = true;
        CdrInputStream input = CdrInputStream.getInstance();
        CdrOutputStream output = CdrOutputStream.getInstance();
        CdrOutputStream results = CdrOutputStream.getInstance();

        for (; ;) {
            boolean sendResponse = true;
            GiopMessage inputMessage;

            try {
                inputMessage = input.receive_message( in, clientInfo );
                firstMessage = false;
            } catch (BadMagicException ex) {
                if (firstMessage) {
                    warnBadMagic(clientInfo, ex);
                } else {
                    warnBadMagicBadSize(clientInfo, ex);
                }
                closeStreams( in, out );
                return;
            } catch (UnsupportedProtocolVersionException ex) {
                warnGiopVersion( clientInfo, ex);
                closeStreams( in, out );
                return;
            } catch (Exception ex) {
                if (input.getOffset() > 0) {
                    ex.printStackTrace();
                    warnReceiveFailed( clientInfo, ex);
                }
                // Otherwise client shutdown was not in the middle of a
                // request, i.e. probably 'normal' and unworthy of a
                // log message.
                closeStreams( in, out );
                return;
            }

            output.setGiopVersion(input.getGiopVersion());

            switch (inputMessage.type) {
                case MsgType_1_1._Request:
                    processRequest(input, output, results, inputMessage.request, clientInfo);
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
                    if(inputMessage.httpTunneling)
                    {
                        output.send_http_response( out, clientInfo );
                    }
                    else
                    {
                        output.send_message( out, clientInfo );
                    }
                } catch (Exception ex) {
                    warnSendFailed(clientInfo, ex);
                    closeStreams( in, out );
                    return;
                }
            }

            input.reset();
            output.reset();
            results.reset();
        }
    }

    protected void closeStreams( InputStream in, OutputStream out ) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception ignore) {
        }

        try {
            if (out != null) {
                out.close();
            }
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

    protected void processRequest(CdrInputStream parameters,
                                  CdrOutputStream output,
                                  CdrOutputStream results,
                                  RequestHeader_1_2 request,
                                  String clientInfo
                                  ) {
        byte[] objectKey = getObjectKey(request.target);
        int keyLength = objectKey.length;
        int keyType = keyLength == 0 ? 0 : objectKey[0];

        ReplyHeader_1_2 reply = new ReplyHeader_1_2();
        reply.request_id = request.request_id;

        ObjectInputStream objectIn;
        ObjectOutputStream objectOut;

        if (simpleIDL || keyType == 'N' || keyType == 'J') {
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

            processServiceContext(request);

            Object object;
            try
            {
                object = nameService.lookup(objectName);
            }
            catch (javax.naming.NameNotFoundException notFound)
            {
                warnLookupFailed(clientInfo, notFound);
                throw new org.omg.CORBA.OBJECT_NOT_EXIST(objectName);
            }

            Adapter adapter = (Adapter)object;
            if (adapter != null)
            {
                adapter.invoke(request.operation, objectKey, objectIn, objectOut);

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
            warnSystemException(clientInfo, ex);
            results = CdrOutputStream.getInstance(); // in case we already wrote to it
            results.write_SystemException(ex, writeSystemExceptionStackTrace);
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

            // TODO: Is the ServiceContext a CSIv2 Security Context?
        }
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
