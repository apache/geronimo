/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.io.IOException;

/**
 * An abstract implementation of {@link Console}.
 *
 * <p>Sub-classes only need to define {@link Console#getLine(String,boolean)}.
 *
 * <p>Also provides helpers to determine if history and/or completion is
 *    enabled.
 *
 * @version <code>$Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $</code>
 */
public abstract class AbstractConsole
    implements Console
{
    protected History history;
    protected Completer completer;
    protected IOContext ioContext;
    
    public void setIOContext(final IOContext ioContext)
    {
        this.ioContext = ioContext;
    }
    
    public IOContext getIOContext()
    {
        return ioContext;
    }
    
    public void setHistory(final History history)
    {
        this.history = history;
    }
    
    public History getHistory()
    {
        return history;
    }
    
    protected boolean isHistoryEnabled()
    {
        return history != null;
    }
    
    public void setCompleter(final Completer completer)
    {
        this.completer = completer;
    }
    
    public Completer getCompleter()
    {
        return completer;
    }
    
    protected boolean isCompletionEnabled()
    {
        return completer != null;
    }
    
    public String getLine(final String prompt) throws IOException
    {
        return getLine(prompt, true);
    }
}
