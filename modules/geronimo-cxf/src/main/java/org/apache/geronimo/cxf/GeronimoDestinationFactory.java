package org.apache.geronimo.cxf;

import java.io.IOException;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.Bus;

public class GeronimoDestinationFactory extends HTTPTransportFactory {

    public GeronimoDestinationFactory(Bus bus) {
        super();
        setBus(bus);
    }

    public Destination getDestination(EndpointInfo endpointInfo)
        throws IOException {
        GeronimoDestination destination = new GeronimoDestination(getBus(), this, endpointInfo);
        Configurer configurer = getBus().getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(destination);
        }
        return destination;
    }

}
