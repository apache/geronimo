package org.apache.geronimo.shell.deploy;

import java.io.PrintWriter;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.geronimo.cli.deployer.BaseCommandArgs;
import org.apache.geronimo.deployment.cli.AbstractCommand;
import org.apache.geronimo.deployment.cli.CommandEncrypt;
import org.apache.geronimo.deployment.cli.ConsoleReader;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.cli.StreamConsoleReader;

@Command(scope = "deploy", name = "encrypt", description = "Encrypt strings")
public class EncryptCommand extends ConnectCommand{
    
    @Argument(required = true, description = "Encrypted Message")
    String message;
    
    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        AbstractCommand command = new CommandEncrypt();

        ConsoleReader consoleReader = new StreamConsoleReader(session.getKeyboard(),new PrintWriter(session.getConsole(),true));

        BaseCommandArgs args = new BaseCommandArgs(message.split(" "));

        command.execute(consoleReader, connection, args);

        return null;
    }

}
