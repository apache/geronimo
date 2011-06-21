package org.apache.geronimo.cli.deployer;

import org.apache.geronimo.cli.CLParserException;

public class InstallBundleCommandMetaData extends BaseCommandMetaData {
    public static final CommandMetaData META_DATA = new InstallBundleCommandMetaData();

    private InstallBundleCommandMetaData() {
        super("install-bundle", 
                "2. Other Commands", 
                "[--inPlace] [--startLevel number] [--start] bundleFile",
                "Install and record an OSGi bundle file in Geronimo so that it can be automatically started " + 
                "even after you cleaned the cache directory of the OSGi framework."
                );
    }
    
    @Override
    public CommandArgs parse(String[] newArgs) throws CLParserException {
        return new InstallBundleCommandArgsImpl(newArgs);
    }
}
