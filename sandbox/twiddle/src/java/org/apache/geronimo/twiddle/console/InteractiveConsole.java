/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.twiddle.console;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.ThrowableHandler;

/**
 * ???
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $
 */
public class InteractiveConsole
    implements Runnable
{
    protected Console console;
    protected boolean running;
    
    public InteractiveConsole(final Console console)
    {
        if (console == null) {
            throw new NullArgumentException("console");
        }
        
        this.console = console;
    }
    
    public Console getConsole()
    {
        return console;
    }
    
    public boolean isRunning()
    {
        return running;
    }
    
    public void stop()
    {
        running = false;
    }
    
    public void run()
    {
        try {
            doRun();
        }
        catch (Exception e) {
            ThrowableHandler.add(e);
        }
    }
    
    protected void doRun() throws Exception
    {
        while (running) {
            //
            // TODO: Factor out handler/listener to deal with prompt and input processing
            //
            
            String prompt = "";
            String input = console.getLine(prompt);
            
            if (running) {
            }
            else {
                // warn about wasted input ?
            }
        }
    }
}
