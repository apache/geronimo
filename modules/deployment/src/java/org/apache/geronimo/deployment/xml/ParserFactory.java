package org.apache.geronimo.deployment.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/19 06:40:07 $
 *
 * */
public interface ParserFactory {
    DocumentBuilder getParser()
            throws ParserConfigurationException;
}
