package org.apache.geronimo.cli.deployer;

public interface InstallBundleCommandArgs  extends CommandArgs {

    boolean isInPlace(); 
    
    boolean isStart();
    
    int getStartLevel();
    
    

}
