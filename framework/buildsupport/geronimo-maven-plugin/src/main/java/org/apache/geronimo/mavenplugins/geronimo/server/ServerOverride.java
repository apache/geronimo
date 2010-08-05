package org.apache.geronimo.mavenplugins.geronimo.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.geronimo.system.configuration.AttributesXmlUtil;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class ServerOverride {

    /**
     * server these overides apply to
     * @parameter
     */
    private String server;

    /**
     * file containing overrides
     * @parameter
     */
    private String overrides;

    public String getServer() {
        return server;
    }

    public AttributesType getOverrides(File base) throws IOException, JAXBException, ParserConfigurationException, SAXException, XMLStreamException {
        File file = new File(base, overrides);
        FileReader input = new FileReader(file);
        try {
            return AttributesXmlUtil.loadAttributes(input);
        } finally {
            input.close();
        }

    }
}
