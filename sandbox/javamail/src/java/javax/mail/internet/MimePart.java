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

package javax.mail.internet;
import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.Part;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:28 $
 */
public interface MimePart extends Part {
    public abstract void addHeaderLine(String line) throws MessagingException;
    public abstract Enumeration getAllHeaderLines() throws MessagingException;
    public abstract String getContentID() throws MessagingException;
    public abstract String[] getContentLanguage() throws MessagingException;
    public abstract String getContentMD5() throws MessagingException;
    public abstract String getEncoding() throws MessagingException;
    public abstract String getHeader(String header, String delimiter)
        throws MessagingException;
    public abstract Enumeration getMatchingHeaderLines(String[] names)
        throws MessagingException;
    public abstract Enumeration getNonMatchingHeaderLines(String[] names)
        throws MessagingException;
    public abstract void setContentLanguage(String[] languages)
        throws MessagingException;
    public abstract void setContentMD5(String content)
        throws MessagingException;
    public abstract void setText(String text) throws MessagingException;
    public abstract void setText(String text, String charset)
        throws MessagingException;
}
