/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import javax.mail.internet.MimeMessage;
public class TestData {
    public static Store getTestStore() {
        return new Store(
            getTestSession(),
            new URLName("http://alex@test.com")) {
            public Folder getDefaultFolder() throws MessagingException {
                return getTestFolder();
            }
            public Folder getFolder(String name) throws MessagingException {
                if (name.equals("test")) {
                    return getTestFolder();
                } else {
                    return null;
                }
            }
            public Folder getFolder(URLName name) throws MessagingException {
                return getTestFolder();
            }
        };
    }
    public static Session getTestSession() {
        return Session.getDefaultInstance(System.getProperties());
    }
    public static Folder getTestFolder() {
        return new Folder(getTestStore()) {
            public void appendMessages(Message[] messages)
                throws MessagingException {
            }
            public void close(boolean expunge) throws MessagingException {
            }
            public boolean create(int type) throws MessagingException {
                return false;
            }
            public boolean delete(boolean recurse) throws MessagingException {
                return false;
            }
            public boolean exists() throws MessagingException {
                return false;
            }
            public Message[] expunge() throws MessagingException {
                return null;
            }
            public Folder getFolder(String name) throws MessagingException {
                return null;
            }
            public String getFullName() {
                return null;
            }
            public Message getMessage(int id) throws MessagingException {
                return null;
            }
            public int getMessageCount() throws MessagingException {
                return 0;
            }
            public String getName() {
                return null;
            }
            public Folder getParent() throws MessagingException {
                return null;
            }
            public Flags getPermanentFlags() {
                return null;
            }
            public char getSeparator() throws MessagingException {
                return 0;
            }
            public int getType() throws MessagingException {
                return 0;
            }
            public boolean hasNewMessages() throws MessagingException {
                return false;
            }
            public boolean isOpen() {
                return false;
            }
            public Folder[] list(String pattern) throws MessagingException {
                return null;
            }
            public void open(int mode) throws MessagingException {
            }
            public boolean renameTo(Folder newName) throws MessagingException {
                return false;
            }
        };
    }
    public static Transport getTestTransport() {
        return new Transport(
            getTestSession(),
            new URLName("http://host.name")) {
            public void sendMessage(Message message, Address[] addresses)
                throws MessagingException {
                // TODO Auto-generated method stub
            }
        };
    }
    public static Message getMessage() {
        return new MimeMessage(getTestFolder(), 1) {
        };
    }
}
