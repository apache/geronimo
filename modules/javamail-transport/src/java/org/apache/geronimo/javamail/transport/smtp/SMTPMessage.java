/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.transport.smtp;

import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class SMTPMessage extends MimeMessage {

    // never notify
    public static final int NOTIFY_NEVER = -1;

    // notify of successful deliveries.
    public static final int NOTIFY_SUCCESS = 1;

    // notify of delivery failures.
    public static final int NOTIFY_FAILURE = 2;

    // notify of delivery delays
    public static final int NOTIFY_DELAY = 4;

    // return full message with status notifications
    public static final int RETURN_FULL = 1;

    // return only message headers with status notifications
    public static final int RETURN_HDRS = 2;

    // support 8BitMime encodings
    protected boolean allow8bitMIME = false;

    // a from address specified in the message envelope. Overrides other from
    // sources.
    protected String envelopeFrom = null;

    // an option string to append to the MAIL command on sending.
    protected String mailExtension = null;

    // SMTP mail notification options if DSN is supported.
    protected int notifyOptions = 0;

    // DSN return option notification values.
    protected int returnOption = 0;

    // allow sending if some addresses give errors.
    protected boolean sendPartial = false;

    // an RFC 2554 AUTH= value.
    protected String submitter = null;

    /**
     * Default (and normal) constructor for an SMTPMessage.
     * 
     * @param session
     *            The hosting Javamail Session.
     */
    public SMTPMessage(Session session) {
        // this is a simple one.
        super(session);
    }

    /**
     * Construct an SMTPMessage instance by reading and parsing the data from
     * the provided InputStream. The InputStream will be left positioned at the
     * end of the message data on constructor completion.
     * 
     * @param session
     *            The hosting Javamail Session.
     */
    public SMTPMessage(Session session, InputStream source) throws MessagingException {
        // this is a simple one.
        super(session, source);
    }

    /**
     * Construct an SMTPMimeMessage from another source MimeMessage object. The
     * new object and the old object are independent of each other.
     * 
     * @param source
     *            The source MimeMessage object.
     */
    public SMTPMessage(MimeMessage source) throws MessagingException {
        super(source);
    }

    /**
     * Change the allow8BitMime attribute for the message.
     * 
     * @param a
     *            The new setting.
     */
    public void setAllow8bitMIME(boolean a) {
        allow8bitMIME = a;
    }

    /**
     * Retrieve the current 8bitMIME attribute.
     * 
     * @return The current attribute value.
     */
    public boolean getAllow8bitMIME() {
        return allow8bitMIME;
    }

    /**
     * Change the envelopeFrom attribute for the message.
     * 
     * @param from
     *            The new setting.
     */
    public void setEnvelopeFrom(String from) {
        envelopeFrom = from;
    }

    /**
     * Retrieve the current evelopeFrom attribute.
     * 
     * @return The current attribute value.
     */
    public String getEnvelopeFrom() {
        return envelopeFrom;
    }

    /**
     * Change the mailExtension attribute for the message.
     * 
     * @param e
     *            The new setting.
     */
    public void setMailExtension(String e) {
        mailExtension = e;
    }

    /**
     * Retrieve the current mailExtension attribute.
     * 
     * @return The current attribute value.
     */
    public String getMailExtension() {
        return mailExtension;
    }

    /**
     * Change the notifyOptions attribute for the message.
     * 
     * @param options
     *            The new setting.
     */
    public void setNotifyOptions(int options) {
        notifyOptions = options;
    }

    /**
     * Retrieve the current notifyOptions attribute.
     * 
     * @return The current attribute value.
     */
    public int getNotifyOptions() {
        return notifyOptions;
    }

    /**
     * Change the returnOptions attribute for the message.
     * 
     * @param option
     *            The new setting.
     */
    public void setReturnOption(int option) {
        returnOption = option;
    }

    /**
     * Retrieve the current returnOption attribute.
     * 
     * @return The current attribute value.
     */
    public int getReturnOption() {
        return returnOption;
    }

    /**
     * Change the sendPartial attribute for the message.
     * 
     * @param a
     *            The new setting.
     */
    public void setSendPartial(boolean a) {
        sendPartial = a;
    }

    /**
     * Retrieve the current sendPartial attribute.
     * 
     * @return The current attribute value.
     */
    public boolean getSendPartial() {
        return sendPartial;
    }

    /**
     * Change the submitter attribute for the message.
     * 
     * @param s
     *            The new setting.
     */
    public void setSubmitter(String s) {
        submitter = s;
    }

    /**
     * Retrieve the current submitter attribute.
     * 
     * @return The current attribute value.
     */
    public String getSubmitter() {
        return submitter;
    }
}
