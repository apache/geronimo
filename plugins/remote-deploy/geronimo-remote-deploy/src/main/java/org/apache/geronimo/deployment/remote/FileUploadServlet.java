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
package org.apache.geronimo.deployment.remote;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 * A servlet that accepts file uploads.  It takes only POST requests, which should
 * contain a Java "DataOutput" formatted stream from RemoteDeployUtil containing:
 *
 * RemoteDeployer data stream format:
 *   0) an int, the version of this datastream format - REMOTE_DEPLOY_REQUEST_VER
 *   1) an int, the number of files being uploaded
 *   2) for each file:
 *     2.0) a UTF String, the filename of the file being uploaded
 *     2.1) a long, the length of the file in bytes
 *     2.2) byte[], byte count equal to the number above for the file
 *
 * RemoteDeployer response stream format:
 *   It returns a serialized stream containing:
 *   0) an int, the version of this datastream format - REMOTE_DEPLOY_RESPONSE_VER
 *   1) a UTF string, the status (should be "OK")
 *   2) an int, the number of files received
 *   3) for each file:
 *     3.1) a UTF String, the path to the file as saved to the server's filesystem
 *
 *   The file positions in the response will be the same as in the request.
 *   That is, a name for upload file #2 will be in response position #2.
 *
 * @version $Rev$ $Date$
 */
public class FileUploadServlet extends HttpServlet {

    /** Note:  The below versions should be kept in sync with those in RemoteDeployUtil.java **/
    // Starting RemoteDeploy datastream versions
    public static final int REMOTE_DEPLOY_REQUEST_VER_0 = 0;
    public static final int REMOTE_DEPLOY_RESPONSE_VER_0 = 0;
    // Current RemoteDeploy datastream versions
    public static final int REMOTE_DEPLOY_REQUEST_VER = 0;
    public static final int REMOTE_DEPLOY_RESPONSE_VER = 0;


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int fileCount = 0, filesCreated = 0;
        String names[] = null;
        String status = "OK";

        /* --------------------
         * RemoteDeploy Request
         * --------------------
         *
         * Note:  The below code has to match RemoteDeployUtil.java
         *
         * RemoteDeployer data stream format:
         *   0) an int, the version of this datastream format - REMOTE_DEPLOY_REQUEST_VER
         *   1) an int, the number of files being uploaded
         *   2) for each file:
         *     2.0) a UTF String, the filename of the file being uploaded
         *     2.1) a long, the length of the file in bytes
         *     2.2) byte[], byte count equal to the number above for the file
         */
        DataInputStream in = null;
        try {
            String fileName;
            in = new DataInputStream(request.getInputStream());
            // 0) an int, the version of this datastream format - REMOTE_DEPLOY_REQUEST_VER
            int reqVer = in.readInt();
            // whenever we update the stream version, the next line needs to
            // be changed to just - (reqVer >= REMOTE_DEPLOY_REQUEST_VER_0)
            // but until then, be more restrictive so we can handle old deployers
            // that don't send a version as the first thing, but a file count instead...
            if ((reqVer >= REMOTE_DEPLOY_REQUEST_VER_0) && (reqVer <= REMOTE_DEPLOY_REQUEST_VER)) {
                // 1) an int, the number of files being uploaded
                fileCount = in.readInt();
                names = new String[fileCount];
                // 2) for each file:
                for(int i=0; i<fileCount; i++) {
                    // 2.0) a UTF String, the filename of the file being uploaded
                    fileName = in.readUTF();
                    // 2.1) a long, the length of the file in bytes
                    long length = in.readLong();
                    // create the local temp file
                    //File temp = File.createTempFile("remote-deploy", "");
                    // Note: Doing this because WAR files have to be their original names to
                    // handle the case where no web.xml or context root was provided
                    File tempDir = new File(System.getProperty("java.io.tmpdir"));                    
                    File temp = new File(tempDir, fileName.trim());
                    if (!temp.getAbsolutePath().startsWith(tempDir.getAbsolutePath())) {
                        throw new IOException("Invalid upload filename");                        
                    }
                    temp.createNewFile();
                    temp.deleteOnExit();
                    names[i] = temp.getAbsolutePath();
                    // 2.2) raw bytes, equal to the number above for the file
                    readToFile(in, temp, length);
                    filesCreated++;
                }
            }
        } catch (IOException e) {
            status = "ERROR: "+e.getMessage();
        } finally {
            if (in != null) {
                in.close();
                in = null;
            }
        }

        /* ---------------------
         * RemoteDeploy Response
         * ---------------------
         *
         * Note:  The below code has to match RemoteDeployUtil.java
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
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(response.getOutputStream());
            // 0) an int, the version of this datastream format - REMOTE_DEPLOY_RESPONSE_VER
            out.writeInt(REMOTE_DEPLOY_RESPONSE_VER);
            // 1) a UTF string, the status (should be "OK")
            out.writeUTF(status);
            if (filesCreated == fileCount) {
                // 2) an int, the number of files received
                out.writeInt(fileCount);
                // 3) for each file:
                for (int i = 0; i < names.length; i++) {
                    // 3.1) a UTF String, the path to the file as saved to the server's filesystem
                    out.writeUTF(names[i]);
                }
                // x) new data would be added here
                // only send newer data depending on the REQUEST_VER that came in
            } else {
                // error occurred, so don't send back any filenames, just a zero count
                // 2) an int, the number of files received
                out.writeInt(0);
            }
        } finally {
            if (out != null) {
                out.flush();
                out.close();
                out = null;
            }
        }
    }

    private static void readToFile(DataInputStream in, File temp, long length) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
        int read;
        long total;
        try {
            byte[] buf = new byte[8192];
            total = 0;
            while((read = in.read(buf, 0, (int)Math.min(buf.length, length - total))) > -1) {
                out.write(buf, 0, read);
                total += read;
                if(total == length) {
                    break;
                }
            }
        } finally {
            try {out.flush();} catch (IOException e) {}
            out.close();
            out = null;
        }
        if(total != length) {
            throw new IOException("Unable to read entire upload file ("+total+"B expecting "+length+"B)");
        }
    }
}

