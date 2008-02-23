import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.superbiz.counter.CounterRemote;


public class Launcher {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        properties.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
        InitialContext remoteContext = new InitialContext(properties);
        
        CounterRemote counterRemote = (CounterRemote) remoteContext.lookup("CounterImplRemote");
        counterRemote.increment();
        counterRemote.reset();
    }
    
}
