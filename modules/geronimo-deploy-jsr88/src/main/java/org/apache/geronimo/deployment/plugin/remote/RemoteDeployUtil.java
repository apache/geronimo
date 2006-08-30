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
package org.apache.geronimo.deployment.plugin.remote;

import org.apache.geronimo.deployment.plugin.local.AbstractDeployCommand;
import org.apache.geronimo.util.encoders.Base64;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

/**
 * Knows how to upload files to a server
 *
 * @version $Rev$ $Date$
 */
public class RemoteDeployUtil {
    public static void uploadFilesToServer(File[] files, AbstractDeployCommand progress) {
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
            try {
                URL url = progress.getRemoteDeployUploadURL();
                URLConnection con = connectToServer(url, progress.getCommandContext().getUsername(), progress.getCommandContext().getPassword());
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(con.getOutputStream()));
                out.writeInt(valid.size());
                byte[] buf = new byte[1024];
                int size, total, length, threshold, next;
                for (Iterator it = valid.iterator(); it.hasNext();) {
                    Integer index = (Integer) it.next();
                    File file = files[index.intValue()];
                    out.writeInt(length = (int)file.length());
                    threshold = Math.max(length / 100, 10240);
                    next = threshold;
                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                    total = 0;
                    while((size = in.read(buf)) > -1) {
                        out.write(buf, 0, size);
                        total += size;
                        if(total > next) {
                            progress.updateStatus("Uploading "+file.getName()+": "+(total/1024)+" kB");
                            while(total > next) next += threshold;
                        }
                    }
                }
                out.flush();
                out.close();
                DataInputStream in = new DataInputStream(new BufferedInputStream(con.getInputStream()));
                String status = in.readUTF();
                if(!status.equals("OK")) {
                    progress.fail("Unable to upload files to server: "+status);
                    return;
                }
                progress.updateStatus("File upload complete (Server: "+status+")");
                int count = in.readInt();
                if(count != valid.size()) {
                    progress.fail("Server did not receive all "+valid.size()+" files ("+count+")");
                }
                for (Iterator it = valid.iterator(); it.hasNext();) {
                    Integer index = (Integer) it.next();
                    String serverFileName = in.readUTF();
                    files[index.intValue()] = new File(serverFileName);
                }
                in.close();
                progress.updateStatus(count+" file(s) transferred to server.  Resuming deployment operation.");
            } catch (Exception e) {
                progress.doFail(e);
            }
        }
    }

    private static URLConnection connectToServer(URL url, String username, String password) throws IOException {
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
