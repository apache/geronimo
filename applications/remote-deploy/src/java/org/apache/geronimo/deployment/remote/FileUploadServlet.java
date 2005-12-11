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
 * contain a Java "DataOutput" formatted stream containing:
 *
 * 1) an int, the number of files being uploaded
 * 2) for each file:
 *    1) an int, the length of the file in bytes
 *    2) a number of raw bytes equal to the above for the file
 *
 * It returns a serialized stream containing:
 *
 * 1) a UTF string, the status (should be "OK")
 * 2) an int, the number of files received
 * 3) for each file:
 *    1) a UTF String, the path to the file as saved to the server's filesystem
 *
 * The file positions in the response will be the same as in the request.
 * The is, a name for upload file #2 will be in response position #2.
 *
 * @version $Rev$ $Date$
 */
public class FileUploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int fileCount;
        String names[];
        try {
            DataInputStream in = new DataInputStream(request.getInputStream());
            fileCount = in.readInt();
            names = new String[fileCount];
            for(int i=0; i<fileCount; i++) {
                int length = in.readInt();
                File temp = File.createTempFile("remote-deploy", "");
                temp.deleteOnExit();
                names[i] = temp.getAbsolutePath();
                readToFile(in, temp, length);
            }
            in.close();
        } catch (IOException e) {
            DataOutputStream out = new DataOutputStream(response.getOutputStream());
            out.writeUTF("ERROR: "+e.getMessage());
            out.close();
            return;
        }
        DataOutputStream out = new DataOutputStream(response.getOutputStream());
        out.writeUTF("OK");
        out.writeInt(fileCount);
        for (int i = 0; i < names.length; i++) {
            out.writeUTF(names[i]);
        }
        out.flush();
        out.close();
    }

    private static void readToFile(DataInputStream in, File temp, int length) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
        int total, read;
        try {
            byte[] buf = new byte[1024];
            total = 0;
            while((read = in.read(buf, 0, Math.min(buf.length, length - total))) > -1) {
                out.write(buf, 0, read);
                total += read;
                if(total == length) {
                    break;
                }
            }
        } finally {
            try {out.flush();} catch (IOException e) {}
            out.close();
        }
        if(total != length) {
            throw new IOException("Unable to read entire upload file ("+total+"b expecting "+length+"b)");
        }
    }
}

