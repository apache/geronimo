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

package org.apache.geronimo.remoting.transport.async.bio;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.proxy.SimpleComponent;
import org.apache.geronimo.remoting.transport.ConnectionFailedException;
import org.apache.geronimo.remoting.transport.TransportException;
import org.apache.geronimo.remoting.transport.URISupport;
import org.apache.geronimo.remoting.transport.async.AsyncMsg;
import org.apache.geronimo.remoting.transport.async.Channel;
import org.apache.geronimo.remoting.transport.async.ChannelListner;
/**
 * The Blocking implementation of the AsynchChannel interface.  
 * 
 * This implemenation uses the standard Java 1.3 blocking socket IO.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:20 $
 */
public class BlockingChannel extends SimpleComponent implements Runnable, Channel {

    static final private Log log = LogFactory.getLog(BlockingChannel.class);

    private ChannelListner listner;
    private Thread worker;
    private SocketChannel socketChannel;
    private boolean closing = false;

    private Inflater inflator;
    private Deflater deflater;

    private URI remoteURI;
    private URI requestedURI;

    public void open(URI remoteURI, URI backConnectURI, ChannelListner listner) throws TransportException {

        if (log.isTraceEnabled())
            log.trace("Connecting to : " + remoteURI);

        this.listner = listner;
        this.remoteURI = remoteURI;
        int port = remoteURI.getPort();
        boolean enableTcpNoDelay = true;

        Properties params = URISupport.parseQueryParameters(remoteURI);
        enableTcpNoDelay = params.getProperty("tcp.nodelay", "true").equals("true");
        int compression = Integer.parseInt(params.getProperty("compression", "-1"));

        try {
            InetAddress addr = InetAddress.getByName(remoteURI.getHost());
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(addr, port));
        } catch (Exception e) {
            throw new ConnectionFailedException("" + e);
        }
        try {
            socketChannel.socket().setTcpNoDelay(enableTcpNoDelay);

            DataOutputStream out = new DataOutputStream(socketChannel.socket().getOutputStream());
            out.writeUTF(remoteURI.toString());
            out.writeUTF(backConnectURI.toString());
            out.flush();
            
            if (compression != -1) {
                inflator = new Inflater(true);
                deflater = new Deflater(compression, true);
            }
            
        } catch (Exception e) {
            throw new TransportException("Connection handshake failed: " + e);
        }

        worker = new Thread(this, "Channel -> " + remoteURI);
        worker.setDaemon(true);
        worker.start();
    }

    public void init(URI localURI, SocketChannel socketChannel) throws IOException, URISyntaxException {
        this.socketChannel = socketChannel;

        DataOutputStream out = new DataOutputStream(socketChannel.socket().getOutputStream());
        out.flush();

        DataInputStream in = new DataInputStream(socketChannel.socket().getInputStream());
        // Use to get connection options.
        String destURI = in.readUTF();
        // Use in case we need to establish new connections back to 
        // the source vm.  Could be null.
        String sourceURI = in.readUTF();
        this.remoteURI = new URI(sourceURI);
        this.requestedURI = new URI(destURI);
        if (log.isTraceEnabled()) {
            log.trace("Remote URI    : " + remoteURI);
            log.trace("Requested URI : " + requestedURI);
        }
        
        boolean enableTcpNoDelay = true;
        Properties params = URISupport.parseQueryParameters(requestedURI);
        enableTcpNoDelay = params.getProperty("tcp.nodelay", "true").equals("true");
        int compression = Integer.parseInt((String) params.getProperty("compression", "-1"));

        if (compression != -1) {
            inflator = new Inflater(true);
            deflater = new Deflater(compression, true);
        }

        /*
        */
        socketChannel.socket().setTcpNoDelay(enableTcpNoDelay);
        if (log.isTraceEnabled()) {
            log.trace("Compression level : " + compression);
            log.trace("tcp no delay : " + enableTcpNoDelay);
        }
    }

    public void open(ChannelListner listner) throws TransportException {
        this.listner = listner;
        worker = new Thread(this, "Channel <- " + remoteURI);
        worker.setDaemon(true);
        worker.start();
    }

    static int nextId = 0;
    /**
     * @return
     */
    synchronized private int getNextID() {
        return nextId++;
    }

    private Object sendMutex = new Object();
    public void send(AsyncMsg data) throws TransportException {
        try {
            ByteBuffer buffers[] = serialize(data);
            synchronized (sendMutex) {
                if (closing)
                    throw new TransportException("connection has been closed.");
                // should block. 
                socketChannel.write(buffers);
            }
        } catch (IOException e) {
            throw new TransportException("" + e);
        }
    }

    /**
     * @param data
     * @return
     */
    private ByteBuffer[] serialize(AsyncMsg data) throws IOException {
        ByteBuffer rc[] = new ByteBuffer[2];
        rc[0] = ByteBuffer.allocate(4);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream t=baos;
        if (deflater!=null) 
            t = new DeflaterOutputStream(t, deflater);
        DataOutputStream out = new DataOutputStream(t);

        data.writeExternal(out);
        out.close();
        rc[1] = ByteBuffer.wrap(baos.toByteArray());
        rc[0].putInt(rc[1].limit());
        
        rc[0].rewind();        
        rc[1].rewind();        
        return rc;
    }

    /**
     * @param buffer
     */
    public AsyncMsg deserialize(ByteBuffer[] message) throws IOException  {
        AsyncMsg asyncMsg = new AsyncMsg();

        InputStream t=new ByteArrayInputStream( message[1].array());
        if (inflator!=null) 
            t = new InflaterInputStream(t, inflator);
        DataInputStream in = new DataInputStream( t );

        asyncMsg.readExternal(in);
        in.close();
        return asyncMsg;        
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        
        ByteBuffer message[] = new ByteBuffer[]{
                ByteBuffer.allocate(4), 
                ByteBuffer.allocate(1024*10) 
                };
        
        try {
            while (true) {
                
                log.trace("Waiting for message");
                message[0].clear();
                socketChannel.read(message[0]);
                while( message[0].position()!=4 ) {
                    socketChannel.read(message[0]);
                }
                message[0].flip();
                int size = message[0].getInt();
                
                // Signal from remote that the socket is being closed.
                if( size==-1 )
                    break;

                // Do we need a bigger buffer?
                // TODO: should we have a max size the buffer can grow to?                     
                if( size > message[1].capacity() )
                    message[1] = ByteBuffer.allocate(size);
                    
                // Load the body.                     
                message[1].clear();
                message[1].limit(size);
                socketChannel.read(message[1]);
                while( message[1].position()!=size ) {
                    socketChannel.read(message[1]);
                }
                message[1].flip();                
                listner.receiveEvent(deserialize(message));
            }
            log.trace("Stopping due to remote end closing.");
        } catch (IOException e) {
            log.trace("Stopping due to exception.", e);
        } finally {
            asyncClose();
        }
        log.trace("Stopped");
    }

    /**
     * Starts to terminate the connection.  Lets the remote end
     * know that we are closing.
     * 
     * The server side calls this close.  Could be called in response to
     * 2 events:
     * - we initiated the close() (so we finish the close)
     * - An asynch error initiated the close(). (so we start the close process)
     * We keep state to know if we started the socket close().  
     */
    synchronized private void asyncClose() {
        // socket is null when we finish close()
        if (socketChannel == null)
            return;
        try {
            socketChannel.socket().shutdownInput();
            // were we allready closing??		
            if (closing) {
                // both side should be shutdown now.  finish close.
                socketChannel.close();
            } else {
                closing = true;
                listner.closeEvent();
            }
        } catch (IOException e) {
            // If the 'nice' shutdown fails at any point,
            // then do the forced shutdown.
            forcedClose();
        }
    }

    /**
     * Starts to terminate the connection.  Lets the remote end
     * know that we are closing.
     * 
     * The client side calls this close.  Could be called in response to
     * 2 events:
     * - the remote sever initiated the close(). (so we finish the close)
     * - we initiated the close() (so we wait for the remote side to finish the close)
     * We keep state to know if we started the socket close().  
     *   
     */
    synchronized public void close() {
        // socket is null when we finish close()
        if (socketChannel == null)
            return;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.asIntBuffer().put(-1);
            synchronized (sendMutex) {
                socketChannel.write(buffer);
                socketChannel.socket().shutdownOutput();
            }
            // were we allready closing??		
            if (closing) {
                // both side should be shutdown now.  finish close.
                socketChannel.close();
                socketChannel = null;
            } else {
                closing = true;
            }
        } catch (IOException e) {
            forcedClose();
        }
    }

    /**
     * forcibly terminates the connection without telling the remote end 
     * that the connection is being closed. 
     */
    private void forcedClose() {
        try {
            socketChannel.close();
        } catch (Throwable e) {
        }
        socketChannel = null;
    }

    /**
     * @return
     */
    public URI getRemoteURI() {
        return remoteURI;
    }
    
    /**
     * @return Returns the requestedURI.
     */
    public URI getRequestedURI() {
        return requestedURI;
    }

}
