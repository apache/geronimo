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

package javax.mail;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:27 $
 */
public abstract class Multipart {
    protected String contentType;
    protected Part parent;
    protected Vector parts = new Vector();
    protected Multipart() {
    }
    public void addBodyPart(BodyPart part) throws MessagingException {
        parts.add(part);
    }
    public void addBodyPart(BodyPart part, int pos) throws MessagingException {
        parts.add(pos, part);
    }
    public BodyPart getBodyPart(int index) throws MessagingException {
        return (BodyPart) parts.get(index);
    }
    public String getContentType() {
        return contentType;
    }
    public int getCount() throws MessagingException {
        return parts.size();
    }
    public Part getParent() {
        return parent;
    }
    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        return parts.remove(part);
    }
    public void removeBodyPart(int index) throws MessagingException {
        parts.remove(index);
    }
    protected void setMultipartDataSource(MultipartDataSource mds)
        throws MessagingException {
        // TODO review implementation
        contentType = mds.getContentType();
        int size = mds.getCount();
        for (int i = 0; i < size; i++) {
            addBodyPart(mds.getBodyPart(i));
        }
    }
    public void setParent(Part part) {
        parent = part;
    }
    public abstract void writeTo(OutputStream out)
        throws IOException, MessagingException;
}
