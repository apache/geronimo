/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.datastore.impl.remote.datastore;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;


import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.impl.remote.messaging.GInputStream;
import org.apache.geronimo.datastore.impl.remote.messaging.StreamInputStream;
import org.apache.geronimo.datastore.impl.remote.messaging.StreamOutputStream;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class GFileStub
    implements GFile, Externalizable
{
    
    /**
     * Stub identifier.
     */
    private Integer id;

    /**
     * File path.
     */
    private String path;
    
    /**
     * Properties of the file.
     */
    private Map properties;
    
    /**
     * New content of the file.
     */
    private InputStream content;

    /**
     * Client owning this stub.
     */
    private GFileManagerClient client;

    /**
     * Required for externalization.
     */
    public GFileStub() {
    }
    
    /**
     * Creates a stub to be used by a client.
     * 
     * @param aPath Path of the stubbed GFile.
     * @param anId Stub identifier. It is allocated by a GFileManagerServer
     * and is used to match a stub to a remove GFile owned by a
     * GFileManagerProxy.
     */
    public GFileStub(String aPath, Integer anId) {
        if ( null == aPath ) {
            throw new IllegalArgumentException("Path is required.");
        } else if ( null == anId ) {
            throw new IllegalArgumentException("Id is required.");
        }
        path = aPath;
        id = anId;
    }
    
    /**
     * Sets the GFileManagerClient owning this stub. 
     * 
     * @param aClient Client containing this file.
     */
    void setGFileManagerClient(GFileManagerClient aClient) {
        client = aClient;
    }

    /**
     * Gets the identifier of this stub. This identifier is used on the
     * server-side to retrieve the GFile mirrored by this stub.
     * 
     * @return Stub identifier.
     */
    Integer getID() {
        return id;
    }
    
    public String getPath() {
        return path;
    }

    public boolean exists() throws IOException {
        return ((Boolean)
            client.sendSyncRequest(
                new GFileCommand(id, "exists", null))).booleanValue();
    }

    public boolean isDirectory() throws IOException {
        return ((Boolean)
            client.sendSyncRequest(
                new GFileCommand(id, "isDirectory", null))).booleanValue();
    }

    public boolean isFile() throws IOException {
        return ((Boolean)
            client.sendSyncRequest(
                new GFileCommand(id, "isFile", null))).booleanValue();
    }

    public String[] listFiles() throws IOException {
        return (String[])
            client.sendSyncRequest(
                new GFileCommand(id, "listFiles", null));
    }

    public void lock() throws IOException {
    }

    public void unlock() throws IOException {
    }

    public Map getProperties() throws IOException {
        return (Map)
            client.sendSyncRequest(
                new GFileCommand(id, "getProperties", null));
    }

    public void setContent(InputStream anIn) {
        client.sendSyncRequest(
                new GFileCommand(id, "setContent",
                    new Object[]{new GInputStream(anIn)}));
    }

    public InputStream getContent() {
        return (InputStream) client.sendSyncRequest(
                new GFileCommand(id, "getContent", null));
    }

    public InputStream getInputStream() throws IOException {
        return (InputStream) client.sendSyncRequest(
                new GFileCommand(id, "getInputStream", null));
    }

    public Map getPropertiesByName(Collection aCollOfNames) throws IOException {
        return (Map)
            client.sendSyncRequest(
                new GFileCommand(id, "getPropertiesByName",
                new Object[] {aCollOfNames}));
    }

    public void addProperty(String aName, String aValue) throws IOException {
        client.sendSyncRequest(
            new GFileCommand(id, "addProperty",
            new Object[] {aName, aValue}));
    }

    public void removeProperty(String aName) throws IOException {
        client.sendSyncRequest(
            new GFileCommand(id, "removeProperty",
            new Object[] {aName}));
    }

    /**
     * A GFileStub MUST be serialized by a Custom ObjectOutputStream, which 
     * knows how to encode an InputStream (the content of the GFile) into the
     * ObjectOutput.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        if ( !(out instanceof StreamOutputStream.CustomObjectOutputStream) ) {
            throw new IOException("Must be serialized by a StreamOutputStream.");
        }
        StreamOutputStream.CustomObjectOutputStream objOut =
            (StreamOutputStream.CustomObjectOutputStream) out;
        objOut.writeObject(id);
        objOut.writeUTF(path);
        objOut.writeObject(properties);
        objOut.writeObject(new GInputStream(content));
    }

    /**
     * See writeExternal.
     */
    public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
        if ( !(in instanceof StreamInputStream.CustomObjectInputStream) ) {
            throw new IOException("Must be deserialized by a StreamInputStream.");
        }
        StreamInputStream.CustomObjectInputStream objIn =
            (StreamInputStream.CustomObjectInputStream) in;
        id = (Integer) objIn.readObject();
        path = objIn.readUTF();
        properties = (Map) objIn.readObject();
        GInputStream stream = (GInputStream) objIn.readObject();
        content = stream.getRawInputStream();
    }
    
}
