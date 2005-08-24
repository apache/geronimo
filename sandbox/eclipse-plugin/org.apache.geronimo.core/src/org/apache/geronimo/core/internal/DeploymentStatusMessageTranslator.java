/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.geronimo.core.internal;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;

import org.eclipse.core.resources.IProject;

public class DeploymentStatusMessageTranslator {

	public static String getTranslatedMessage(ProgressEvent event, IProject project) {
		DeploymentStatus status = event.getDeploymentStatus();
		if (status != null) {
			String result = translateCommand(status.getCommand()) + " ";
			result = result.concat("project " + project.getName()) + " ";			
			return result.concat((status.getState().toString())) + getMessageSuffix(status.getState());
		}
		return "";
	}
	
	private static String getMessageSuffix(StateType state) {
		if(state == StateType.RUNNING) {
			return "...";
		} 
		return ".";
	}

	private static String translateCommand(CommandType type) {
		if (type == CommandType.DISTRIBUTE) {
			return "Distributing";
		}

		if (type == CommandType.START) {
			return "Starting";
		}

		if (type == CommandType.REDEPLOY) {
			return "Redeploying";
		}

		if (type == CommandType.UNDEPLOY) {
			return "Undeploying";
		}

		return "";
	}

}
