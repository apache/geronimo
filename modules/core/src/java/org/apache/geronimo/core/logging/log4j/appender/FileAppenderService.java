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

package org.apache.geronimo.core.logging.log4j.appender;

import org.apache.geronimo.core.serverinfo.ServerInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.log4j.FileAppender;


/**
 * An extention of the default Log4j FileAppenderService which
 * will make the directory structure for the set log file.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/11 03:14:11 $
 */
public class FileAppenderService extends AbstractAppenderService {
    private final ServerInfo serverInfo;
    private String file;
    private boolean running = false;

    public FileAppenderService(ServerInfo serverInfo) {
        this(serverInfo, new FileAppender());
    }

    public FileAppenderService(ServerInfo serverInfo, FileAppender appender) {
        super(appender);
        this.serverInfo = serverInfo;
    }

    public boolean getAppend() {
        return ((FileAppender) appender).getAppend();
    }

    public void setAppend(boolean append) {
        ((FileAppender) appender).setAppend(append);
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
        if (running) {
            setAppenderFile(file);
        }
    }

    public void doStart() {
        running = true;
        setAppenderFile(file);
        super.doStart();
    }

    public void doStop() {
        super.doStop();
        running = false;
    }

    private void setAppenderFile(String file) {
        file = serverInfo.resolvePath(file);
        ((FileAppender) appender).setFile(file);
    }

    public boolean getBufferedIO() {
        return ((FileAppender) appender).getBufferedIO();
    }

    public void setBufferedIO(boolean bufferedIO) {
        ((FileAppender) appender).setBufferedIO(bufferedIO);
    }

    public int getBufferSize() {
        return ((FileAppender) appender).getBufferSize();
    }

    public void setBufferSize(int bufferSize) {
        ((FileAppender) appender).setBufferSize(bufferSize);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(FileAppenderService.class.getName(), AbstractAppenderService.GBEAN_INFO);
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"ServerInfo"},
                new Class[]{ServerInfo.class}
        ));
        infoFactory.addReference(new GReferenceInfo("ServerInfo", ServerInfo.class.getName()));
        infoFactory.addAttribute(new GAttributeInfo("Append", true));
        infoFactory.addAttribute(new GAttributeInfo("File", true));
        infoFactory.addAttribute(new GAttributeInfo("BufferedIO", true));
        infoFactory.addAttribute(new GAttributeInfo("BufferedSize", true));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
