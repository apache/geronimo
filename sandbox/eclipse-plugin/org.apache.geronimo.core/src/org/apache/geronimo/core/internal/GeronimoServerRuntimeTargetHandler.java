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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.generic.core.internal.GenericServerRuntimeTargetHandler;
import org.eclipse.jst.server.generic.core.internal.ServerTypeDefinitionUtil;
import org.eclipse.jst.server.generic.servertype.definition.ArchiveType;
import org.eclipse.jst.server.generic.servertype.definition.Classpath;
import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
import org.eclipse.wst.server.core.IRuntime;

public class GeronimoServerRuntimeTargetHandler extends
		GenericServerRuntimeTargetHandler {
	
	String cachedArchiveString=null;
	IClasspathEntry[] cachedClasspath=null;
	

	/* (non-Javadoc)
	 * @see ClasspathRuntimeTargetHandler#resolveClasspathContainer(IRuntime, java.lang.String)
	 */
	public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime,String id){		
		return getServerClassPathEntry(runtime);
	}
	
	public IClasspathEntry[] getServerClassPathEntry(IRuntime runtime)
	{
		ServerRuntime serverDefinition = ServerTypeDefinitionUtil.getServerTypeDefinition(runtime);		
		String ref = serverDefinition.getProject().getClasspathReference();
		Classpath cp = serverDefinition.getClasspath(ref);
		List archives = cp.getArchive();
		
		// It's expensive to keep searching directories, so try to cache the result
		IClasspathEntry[] savedClasspath=getCachedClasspathFor(serverDefinition, archives);
		if(savedClasspath!=null)
			return savedClasspath;
		
		Iterator archiveIter = archives.iterator();
		ArrayList entryList = new ArrayList();
		while (archiveIter.hasNext()) {
			ArchiveType archive = (ArchiveType) archiveIter.next();
			String item = serverDefinition.getResolver().resolveProperties(archive.getPath());
			Path path=new Path(item);
			File file=path.toFile();
			if(file.isDirectory())
			{
				File[] list=file.listFiles();
				for(int i=0; i<list.length; i++)
				{
					if(!list[i].isDirectory())
					{
						Path p=new Path(list[i].getAbsolutePath());
						IClasspathEntry entry = JavaCore.newLibraryEntry(p,null,null );
						entryList.add(entry);	
					}					
				}
	
			}
			else
			{
				IClasspathEntry entry = JavaCore.newLibraryEntry(path,null,null );
				entryList.add(entry);
			}
		}
		
		IClasspathEntry[] classpath=(IClasspathEntry[])entryList.toArray(new IClasspathEntry[entryList.size()]);
		setCachedClasspath(classpath);

		return classpath;
	}

	private IClasspathEntry[] getCachedClasspathFor(ServerRuntime serverDefinition, List archives) {
		
		// Need to iterate through the list, and expand the variables (in case they have changed)
		// The simplest approach is to construct/cache a string for this
		// That will still save the overhead of going to the filesystem
		
		StringBuffer buffer=new StringBuffer();
		Iterator archiveIter = archives.iterator();
		while (archiveIter.hasNext()) {
			ArchiveType archive = (ArchiveType) archiveIter.next();
			String item = serverDefinition.getResolver().resolveProperties(archive.getPath());
			buffer.append(item);
			buffer.append(File.pathSeparatorChar);
		}
		
		String archiveString=buffer.toString();
		
		if(cachedArchiveString != null && cachedArchiveString.equals(archiveString))
			return cachedClasspath;
		
		// This is a cache miss - ensure the data is null (to be safe), but save the key (archiveString) now
		// The data will be set once it's calculated
		cachedClasspath=null;
		cachedArchiveString=archiveString;
		return null;
	}

	private void setCachedClasspath(IClasspathEntry[] classpath) {
		cachedClasspath=classpath;
	}
}
