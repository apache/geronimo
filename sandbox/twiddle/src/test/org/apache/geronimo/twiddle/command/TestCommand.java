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

package org.apache.geronimo.twiddle.command;

/**
 * A simple test command.
 *
 * @version <code>$Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $</code>
 */
public class TestCommand
    extends AbstractCommand
{
    private String text = "my default text";
    
    public void setText(String text)
    {
        this.text = text;
    }
    
    public String getText()
    {
        return text;
    }
    
    public int execute(String[] args) throws Exception
    {
        System.out.println("Name: " + getCommandInfo().getName());
        System.out.println("Description: " + getCommandInfo().getDescription());
        System.out.println("Text: " + text);
        
        return Command.SUCCESS;
    }
}
