package org.apache.geronimo.cli.deployer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.geronimo.cli.CLParserException;

public class InstallBundleCommandArgsImpl implements InstallBundleCommandArgs {

    private final static String ARGUMENT_IN_PLACE_SHORTFORM = "i";
    private final static String ARGUMENT_IN_PLACE = "inPlace";
    
    private final static String ARGUMENT_START_SHORTFORM = "s";
    private final static String ARGUMENT_START = "start";
        
    private final static String ARGUMENT_START_LEVEL_SHORTFORM = "l";
    private final static String ARGUMENT_START_LEVEL = "startLevel";
    
    protected final Options options;
    protected CommandLine commandLine;

    public InstallBundleCommandArgsImpl(String[] args) throws CLParserException {
        options = new Options();
        addInPlace();
        addStart();
        addStartLevel();
        
        CommandLineParser parser = new GnuParser();
        try {
            commandLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            throw new CLParserException(e.getMessage(), e);
        }
        
        if (0 == commandLine.getArgs().length) {
            throw new CLParserException("Must specify a bundle location");
        }
    }

    
    protected void addInPlace() {
        options.addOption(ARGUMENT_IN_PLACE_SHORTFORM,
                ARGUMENT_IN_PLACE,
                false,
                "If inPlace is provided, the bundle is not copied to the \"repository/recorded-bundles\" directory.");
    }
    
    protected void addStart() {
        options.addOption(ARGUMENT_START_SHORTFORM,
                ARGUMENT_START,
                false,
                "If start is provided, the bundle will be automatically started after recorded in Geronimo.");
    }
    
    protected void addStartLevel() {
        OptionBuilder optionBuilder = OptionBuilder.hasArg().withArgName("startLevel");
        optionBuilder = optionBuilder.withLongOpt(ARGUMENT_START_LEVEL);
        optionBuilder = optionBuilder.withDescription("If no start level are provided, will use the framework's initial bundle start level");
        Option option = optionBuilder.create(ARGUMENT_START_LEVEL_SHORTFORM);
        options.addOption(option);
        
    }

    
    
    @Override
    public boolean isInPlace() {
        return commandLine.hasOption(ARGUMENT_IN_PLACE_SHORTFORM);
    }
    
    @Override
    public boolean isStart() {
        return commandLine.hasOption(ARGUMENT_START_SHORTFORM);
    }
    
    @Override
    public int getStartLevel(){
        String startLevelStr = commandLine.getOptionValue(ARGUMENT_START_LEVEL_SHORTFORM);
        if (null == startLevelStr) {
            return -1;
        }
        return Integer.valueOf(startLevelStr);
    }
    
    
    
    @Override
    public String[] getArgs() {
        return commandLine.getArgs();
    }

}
