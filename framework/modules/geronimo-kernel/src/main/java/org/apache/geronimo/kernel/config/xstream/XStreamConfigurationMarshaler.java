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
package org.apache.geronimo.kernel.config.xstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.PushbackInputStream;
import java.io.ObjectStreamConstants;
import java.util.Collection;

import com.thoughtworks.xstream.XStream;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationMarshaler;
import org.apache.geronimo.kernel.config.GBeanState;
import org.apache.geronimo.kernel.config.SerializedConfigurationMarshaler;

/**
 * @version $Rev$ $Date$
 */
public class XStreamConfigurationMarshaler implements ConfigurationMarshaler {
    private static byte[] SERIALIZED_MAGIC = new byte[] {
            (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
            (byte) ((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF)
    };

    public XStreamConfigurationMarshaler() {
        // create an xstream just to assuer all the required libraries are present
        XStreamUtil.createXStream();
    }

    public ConfigurationData readConfigurationData(InputStream in) throws IOException, ClassNotFoundException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(in, 2);
        byte[] streamHeader = new byte[2];
        if (pushbackInputStream.read(streamHeader) != 2) throw new AssertionError("Cound not read stream header");
        pushbackInputStream.unread(streamHeader);

        // if this is a serialized config, fallback to the serialization marshaler
        if (SERIALIZED_MAGIC[0] == streamHeader[0] && SERIALIZED_MAGIC[1] == streamHeader[1]) {
            return new SerializedConfigurationMarshaler().readConfigurationData(pushbackInputStream);
        }

        XStream xstream = XStreamUtil.createXStream();
        Reader reader = new InputStreamReader(pushbackInputStream);
        ConfigurationData configurationData = (ConfigurationData)xstream.fromXML(reader);
        return configurationData;
    }

    public void writeConfigurationData(ConfigurationData configurationData, OutputStream out) throws IOException {
        XStream xstream = XStreamUtil.createXStream();
        String xml = xstream.toXML(configurationData);

        out.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<!-- ======================================================== -->\n" +
                "<!-- Warning - Modification of this file may cause server     -->\n" +
                "<!-- instability.  Also, the format of this XML file is       -->\n" +
                "<!-- undocumented and subject to change without notice.       -->\n" +
                "<!-- ======================================================== -->\n" +
                "\n").getBytes());

        out.write(xml.getBytes());
        out.flush();
    }

    public GBeanState newGBeanState(Collection gbeans) {
        return new XStreamGBeanState(gbeans);
    }
}
