package org.apache.geronimo.console.javabean;

import org.osgi.framework.Bundle;

/**
 * Java bean converted from TabularData and is mostly used in jsps.
 *
 */
public class OSGiBundle {
    private long identifier;
    private String symbolicName;
    private String state;
    private String version;
    public OSGiBundle(long identifier, String symbolicName,String version,int state) {
    	this.identifier=identifier;
    	this.symbolicName=symbolicName;
    	this.version=version;
    	this.state = OSGiBundle.getStateName(state);

    }
    
    public OSGiBundle(Bundle bundle) {
		this(bundle.getBundleId(),
		        bundle.getSymbolicName(),
		        bundle.getVersion().toString(),
		        bundle.getState()
		        
			);
	}

	public long getIdentifier() {
		return identifier;
	}

	public void setIdentifier(long identifier) {
		this.identifier = identifier;
	}

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public static String getStateName(int state){
	    switch(state){
            case Bundle.ACTIVE : return "Active"; 
            case Bundle.INSTALLED : return "Installed"; 
            case Bundle.RESOLVED : return "Resolved"; 
            case Bundle.STARTING : return "Starting"; 
            case Bundle.STOPPING : return "Stopping"; 
            case Bundle.UNINSTALLED: return "Uninstalled"; 
	    }
	    return "Wrong state value";
	}
}
