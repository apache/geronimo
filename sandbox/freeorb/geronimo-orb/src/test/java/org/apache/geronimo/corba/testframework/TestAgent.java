/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.testframework;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

class TestAgent {

    private final String agentName;
    private int exitCode = -1;
    private VMController controller;
    private boolean running = false;
    
    private final Object shutdownOrReadyMonitor = new Object();
	private final boolean fork;

    public TestAgent(String agentName, boolean fork) {
        this.agentName = agentName;
		this.fork = fork;
    }
    
    public void start(String driverName, String testClassName, Properties props) throws IOException {
        // fork vm

        final String[] javaArgs = {testClassName, agentName, "localhost", 
                Integer.toString(Registry.REGISTRY_PORT), driverName, fork ? "true" : "false" };
        
        running = true;
        
        if (fork) {
        
        final Process proc = RemoteTestUtil.execJava(this.getClass().getName(), props, javaArgs);
        
        RemoteTestUtil.redirectStream(proc.getInputStream(), System.out, agentName+":out");
        RemoteTestUtil.redirectStream(proc.getErrorStream(), System.err, agentName+":err");

        try {
            synchronized(shutdownOrReadyMonitor) {
                // Create a thread to notify us if the agent JVM terminates
                new Thread() {
                    public void run() {
                        try {
                            proc.waitFor();
                        }
                        catch(InterruptedException e) {}
                        exitCode = proc.exitValue();
                        running = false;
                        synchronized(shutdownOrReadyMonitor) {
                            shutdownOrReadyMonitor.notify();
                        }
                    }
                }.start();
                
                while(controller == null) {
                  	shutdownOrReadyMonitor.wait();
                }
            }
        }
        catch(InterruptedException e) { throw new Error(e); }
        
        } else {
        
        		new Thread() {
        			
        			public void run() {
        				
        				try {
							TestAgent.main(javaArgs);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        				
                        running = false;
                        synchronized(shutdownOrReadyMonitor) {
                            shutdownOrReadyMonitor.notify();
                        }

        			}
        			
        		}.start();
        	
        }
        

        if(!running) {
            throw new Error("Unable to start agent:" + 
                    " Process terminated unexpectedly with exit code " + 
                    getExitCode());
        }        
    }
    
    public int getExitCode() { return exitCode; }
    
    public void agentReady(VMController controller) {
        this.controller = controller;
        synchronized(shutdownOrReadyMonitor) {
            shutdownOrReadyMonitor.notify();
        }
    }
    
    public VMController getController() {
    	synchronized(shutdownOrReadyMonitor) {
    		while(controller == null) {
    			try {
    				shutdownOrReadyMonitor.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	return controller;
    }
    
    
    /** this is the main routine run in test agents */
    public static void main(String[] args) throws Exception {
        String testClassName = args[0];
        String agentName = args[1];
        String registryHost = args[2];
        int registryPort = Integer.parseInt(args[3]);
        String driverName = args[4];
        String fork = args[5];

        Registry reg = LocateRegistry.getRegistry(registryHost, registryPort);

        Class c = Class.forName(testClassName);
        if (!RemoteTest.class.isAssignableFrom(c)) {
            throw new Exception("Unable to cast to RemoteTst class");
        }

        TestDriver driver = (TestDriver) reg.lookup(driverName);

        RemoteTest test = (RemoteTest) c.newInstance();
        test.setTstDriver(driver);
        VMControllerImpl controller = new VMControllerImpl(test);

        test.setAgentName(agentName);
        
        driver.agentReady(agentName, controller);
        
        synchronized(controller.terminateMonitor) {
            controller.terminateMonitor.wait();
        }
        
        if ("true".equals(fork)) {
            System.out.println("Client exiting (1)");
            System.exit(0);        	
        } else {
            System.out.println("Client exiting (2)");
        }
    }


}
