package org.apache.geronimo.web.jetty;

import java.net.URI;
import org.apache.geronimo.core.service.AbstractComponent;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.web.WebApplication;
import org.mortbay.jetty.servlet.WebApplicationContext;



/**
 * JettyWebApplication.java
 *
 *
 * Created: Sun Sep 14 16:40:17 2003
 *
 * @author <a href="mailto:janb@mortbay.com">Jan Bartel</a>
 * @version 1.0
 */
public class JettyWebApplication extends AbstractComponent implements WebApplication
{
    private WebApplicationContext _jettyContext = null;
    private URI _uri = null;


    public JettyWebApplication() 
    {
        _jettyContext = new WebApplicationContext();
    }
    
    
    public void setURI (URI uri)
    {
        _uri = uri;
        _jettyContext.setWAR (_uri.toString());
    }
    
    public URI getURI ()
    {
        return _uri;
    }

    public void setParentClassLoader (ClassLoader loader)
    {
        _jettyContext.setParentClassLoader (loader);
    }

    public ClassLoader getParentClassLoader()
    {
        return _jettyContext.getParentClassLoader();
    }


    public String[] getServlets ()
    {
        //TODO
        return null;
    }

    public void setContextPath (String path)
    {
        _jettyContext.setContextPath(path);
    }

    public String getContextPath ()
    {
        return _jettyContext.getContextPath();
    }


    public String getDeploymentDescriptor ()
    {
        //TODO
        return null;
    }



    public boolean getJava2ClassloadingCompliance ()
    {
        return _jettyContext.isClassLoaderJava2Compliant();
    }

    public void setJava2ClassloadingCompliance (boolean state)
    {
        _jettyContext.setClassLoaderJava2Compliant(state);
    }


    WebApplicationContext getJettyContext ()
    {
        return _jettyContext;
    }
} 
