package org.apache.geronimo.jetty.deployment;

import java.io.IOException;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.0$
 */
public class TemporaryPlanAdapter {
    private final static Log log = LogFactory.getLog(TemporaryPlanAdapter.class);
    private final static String CORRECT_NAMESPACE = "http://geronimo.apache.org/xml/ns/web";
    private final static String WRONG_NAMESPACE = "http://geronimo.apache.org/xml/ns/web/jetty";

    public static GerWebAppDocument convertJettyDocumentToWeb(XmlObject source) {
        XmlCursor cursor = source.newCursor();
        while(!cursor.isStart()) {
            cursor.toNextToken();
        }

        if(WRONG_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
            log.error("WAR includes a file using the old geronimo-jetty.xml format (including namespace http://geronimo.apache.org/xml/ns/web/jetty).  This is no longer supported.  Please change to the new geronimo-web.xml format.  The main difference is that it uses the namespace http://geronimo.apache.org/xml/ns/web");
            swapNamespace(cursor, CORRECT_NAMESPACE, WRONG_NAMESPACE);
        }

        XmlObject result = source.changeType(GerWebAppDocument.type);
        if (result != null) {
//            SchemaConversionUtils.validateDD(result);
            return (GerWebAppDocument) result;
        }
//        SchemaConversionUtils.validateDD(source);
        return (GerWebAppDocument) source;
    }

    public static GerWebAppType convertJettyElementToWeb(XmlObject source) {
        XmlCursor cursor = source.newCursor();
        while(!cursor.isStart()) {
            cursor.toNextToken();
        }

        if(WRONG_NAMESPACE.equals(cursor.getName().getNamespaceURI())) {
            log.error("EAR includes WAR deployment content using the old geronimo-jetty.xml format (including namespace http://geronimo.apache.org/xml/ns/web/jetty).  This is no longer supported.  Please change to the new geronimo-web.xml format.  The main difference is that it uses the namespace http://geronimo.apache.org/xml/ns/web");
            swapNamespace(cursor, CORRECT_NAMESPACE, WRONG_NAMESPACE);
        }

        XmlObject result = source.changeType(GerWebAppType.type);
        if (result != null) {
            return (GerWebAppType) result;
        }
        return (GerWebAppType) source;
    }

    /**
     * @return true if the schema was correct to begin with
     */
    public static boolean swapNamespace(XmlCursor cursor, String correct, String wrong) {
        while (cursor.hasNextToken()) {
            if (cursor.isStart()) {
                String current = cursor.getName().getNamespaceURI();
                if (correct.equals(current)) {
                    //already has correct schema, exit
                    return true;
                } else if(wrong.equals(current)) {
                    cursor.setName(new QName(correct, cursor.getName().getLocalPart()));
                }
            }
            cursor.toNextToken();
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            convertJettyDocumentToWeb(SchemaConversionUtils.parse(new java.io.File("/home/ammulder/cvs/geronimo/modules/jetty-builder/src/test-resources/deployables/war1/WEB-INF/geronimo-web.xml").toURL()));
            convertJettyDocumentToWeb(SchemaConversionUtils.parse(new java.io.File("/home/ammulder/cvs/geronimo/modules/jetty-builder/src/test-resources/deployables/war3/WEB-INF/geronimo-web.xml").toURL()));
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
