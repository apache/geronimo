/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.deployment.plugin.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.crypto.encoders.Base64;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class FileUploadServletClient implements FileUploadClient {
    private static final Logger log = LoggerFactory.getLogger(FileUploadServletClient.class);

    /** Note:  The below versions should be kept in sync with those in FileUploadServlet.java **/
    // Starting RemoteDeploy datastream versions
    public static final int REMOTE_DEPLOY_REQUEST_VER_0 = 0;
    public static final int REMOTE_DEPLOY_RESPONSE_VER_0 = 0;
    // Current RemoteDeploy datastream versions
    public static final int REMOTE_DEPLOY_REQUEST_VER = 0;
    public static final int REMOTE_DEPLOY_RESPONSE_VER = 0;

    public URL getRemoteDeployUploadURL(Kernel kernel) {
        AbstractName deployerName = getDeployerName(kernel);
        String remoteDeployUpload;
        try {
            remoteDeployUpload = (String) kernel.getAttribute(deployerName, "remoteDeployUploadURL");
            return new URL(remoteDeployUpload);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
     }

    protected AbstractName getDeployerName(Kernel kernel) {
        Set<AbstractName> deployerNames = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.deployment.Deployer"));
        if (1 != deployerNames.size()) {
            throw new IllegalStateException("No Deployer GBean present in running Geronimo server. " +
                 "This usually indicates a serious problem with the configuration of " +
                 "your running Geronimo server.  If " +
                 "the deployer is present but not started, the workaround is to run " +
                 "a deploy command like 'start geronimo/geronimo-gbean-deployer/1.0/car'.  " +
                 "If the deployer service is not present at all (it was undeployed) then " +
                 "you need to either re-install Geronimo or get a deployment plan for the " +
                 "runtime deployer and distribute it while the server is not running and " +
                 "then start the server with a command like the above.  For help on this, " +
                 "write to user@geronimo.apache.org and include the contents of your " +
                 "var/config/config.xml file.");
        }
        return deployerNames.iterator().next();
    }

    public void uploadFilesToServer(URL uploadURL,
            String username,
            String password,
            File[] files,
            FileUploadProgress progress) {
        if(files == null) {
            return;
        }
        
        List valid = new LinkedList();
        for(int i=0; i<files.length; i++) {
            if(files[i] == null) {
                continue;
            }
            File file = files[i];
            if(!file.exists() || !file.canRead()) {
                continue;
            }
            valid.add(new Integer(i));
        }
        
        if(valid.size() > 0) {
            progress.updateStatus("Uploading "+valid.size()+" file(s) to server");
            if (log.isDebugEnabled()) {
                log.debug("Uploading "+valid.size()+" file(s) to server");
            }
            try {
                URLConnection con = connectToServer(uploadURL, username, password);
                writeRequest(con, files, valid, progress);
                readResponse(con, files, valid, progress);
            } catch (Exception e) {
                progress.fail(e);
            }
        }
    }

    protected void readResponse(URLConnection con, File[] files, List valid, FileUploadProgress progress)
            throws IOException {
        /* ---------------------
         * RemoteDeploy Response
         * ---------------------
         *
         * Note:  The below code has to match FileUploadServlet.java
         *
         * RemoteDeployer response stream format:
         *   It returns a serialized stream containing:
         *   0) an int, the version of this datastream format - REMOTE_DEPLOY_RESPONSE_VER
         *   1) a UTF string, the status (should be "OK")
         *   2) an int, the number of files received
         *   3) for each file:
         *     3.1) a UTF String, the path to the file as saved to the server's filesystem
         *   x) new data would be added here
         *
         *   The file positions in the response will be the same as in the request.
         *   That is, a name for upload file #2 will be in response position #2.
         */
        DataInputStream in = new DataInputStream(new BufferedInputStream(con.getInputStream()));
        // 0) an int, the version of this datastream format - REMOTE_DEPLOY_RESPONSE_VER
        int rspVer = in.readInt();
        // whenever we update the stream version, the next line needs to
        // be changed to just - (rspVer >= REMOTE_DEPLOY_RESPONSE_VER_0)
        // but until then, be more restrictive so we can handle old servers
        // that don't send a version as the first thing, but UTF instead...
        if ((rspVer >= REMOTE_DEPLOY_RESPONSE_VER_0) && (rspVer <= REMOTE_DEPLOY_RESPONSE_VER)) {
            // 1) a UTF string, the status (should be "OK")
            String status = in.readUTF();
            if(!status.equals("OK")) {
                progress.fail("Unable to upload files to server.  Server returned status="+status);
                log.error("Unable to upload files to server.  Server returned status="+status);
                return;
            }
            progress.updateStatus("File upload complete (Server status="+status+")");
            if (log.isDebugEnabled()) {
                log.debug("File upload complete (Server status="+status+")");
            }
            // 2) an int, the number of files received
            int count = in.readInt();
            if(count != valid.size()) {
                progress.fail("Server only received "+count+" of "+valid.size()+" files");
                log.warn("Server only received "+count+" of "+valid.size()+" files");
            }
            // 3) for each file:
            for (Iterator it = valid.iterator(); it.hasNext();) {
                Integer index = (Integer) it.next();
                // 3.1) a UTF String, the path to the file as saved to the server's filesystem
                String serverFileName = in.readUTF();
                if (serverFileName != null) {
                    files[index.intValue()] = new File(serverFileName);
                } else {
                    log.error("Received an invalid filename from the server");
                    files[index.intValue()] = null;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Server created file="+serverFileName);
                }
            }
            // x) new data would be added here
            if (rspVer > REMOTE_DEPLOY_RESPONSE_VER_0) {
                // additions in later datastream versions would be handled here

                if (rspVer > REMOTE_DEPLOY_RESPONSE_VER) {
                    // if the server is sending a newer version than we know about
                    // just ignore it and warn the user about the mismatch
                    log.warn("Received a newer server response ("+rspVer+") than expected ("+REMOTE_DEPLOY_RESPONSE_VER+").  Ignoring any additional server response data.");
                }
            }
        } else {
            // should never happen, but handle it anyway
            progress.fail("Received unknown server response version="+rspVer);
            log.warn("Received unknown server response version="+rspVer);
        }
        in.close();
        progress.updateStatus("File(s) transferred to server.  Resuming deployment operation.");
    }

    protected void writeRequest(URLConnection con, File[] files, List valid, FileUploadProgress progress)
            throws IOException, FileNotFoundException {
        /* --------------------
         * RemoteDeploy Request
         * --------------------
         *
         * Note:  The below code has to match FileUploadServlet.java
         *
         * RemoteDeployer data stream format:
         *   0) an int, the version of this datastream format - REMOTE_DEPLOY_REQUEST_VER
         *   1) an int, the number of files being uploaded
         *   2) for each file:
         *     2.0) a UTF String, the filename of the file being uploaded
         *     2.1) a long, the length of the file in bytes
         *     2.2) byte[], byte count equal to the number above for the file
         */
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(con.getOutputStream()));
        // 0) an int, the version of this datastream format - REMOTE_DEPLOY_REQUEST_VER
        out.writeInt(REMOTE_DEPLOY_REQUEST_VER);
        // 1) an int, the number of files being uploaded
        out.writeInt(valid.size());
        byte[] buf = new byte[1024];
        int size;
        long total, length, threshold, next;
        // 2) for each file:
        for (Iterator it = valid.iterator(); it.hasNext();) {
            Integer index = (Integer) it.next();
            File file = files[index.intValue()];
            // 2.0) a UTF String, the filename of the file being uploaded
            out.writeUTF(file.getName().trim());
            // 2.1) a long, the length of the file in bytes
            out.writeLong(length = file.length());
            threshold = Math.max(length / 100, (long)10240);
            next = threshold;
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            if (log.isDebugEnabled()) {
                log.debug("Uploading "+file.getName());
            }
            total = 0;
            // 2.2) raw bytes, equal to the number above for the file
            while((size = in.read(buf)) > -1) {
                out.write(buf, 0, size);
                total += size;
                if(total > next) {
                    progress.updateStatus("Uploading "+file.getName()+": "+(total/1024)+" KB");
                    while(total > next) next += threshold;
                }
            }
            in.close();
        }
        out.flush();
        out.close();
    }

    protected URLConnection connectToServer(URL url, String username, String password) throws IOException {
        URLConnection con = url.openConnection();
        String auth = username + ":" + password;
        byte[] data = auth.getBytes();
        String s = new String(Base64.encode(data));
        while(s.length() % 4 != 0) s += "=";
        con.setRequestProperty("Authorization", "Basic "+s);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.connect();
        return con;
    }

}
