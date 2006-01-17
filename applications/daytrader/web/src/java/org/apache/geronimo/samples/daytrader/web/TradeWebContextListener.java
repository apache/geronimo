/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
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

package org.apache.geronimo.samples.daytrader.web;

import javax.servlet.*;

import org.apache.geronimo.samples.daytrader.direct.*;
import org.apache.geronimo.samples.daytrader.util.*;

public class TradeWebContextListener
	implements ServletContextListener 
{

	//receieve trade web app startup/shutown events to start(initialized)/stop TradeDirect
	public void contextInitialized(ServletContextEvent event)
	{
		Log.trace("TradeWebContextListener contextInitialized -- initializing TradeDirect");
		TradeDirect.init();		
	}
	public void contextDestroyed(ServletContextEvent event)
	{
		Log.trace("TradeWebContextListener  contextDestroy calling TradeDirect:destroy()");		
		TradeDirect.destroy();
	}

    

}

