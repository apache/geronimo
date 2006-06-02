package org.apache.geronimo.plugin.packaging;

import java.io.PrintStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class AbstractPackagingMojo extends AbstractMojo {
	protected final String lineSep = "===========================================";

	private PrintStream logStream = System.out;

	private boolean failOnError = true;

	public abstract void execute() throws MojoExecutionException;

	protected void handleError(Exception e) throws MojoExecutionException {
		// seeLog();
		e.printStackTrace(logStream);
		logStream.println(lineSep);
		if (failOnError) {
			throw (MojoExecutionException) new MojoExecutionException(e
					.toString(), e);
		} else {
			try {
				throw (MojoFailureException) new MojoFailureException(e, e
						.toString(), e.getMessage());
			} catch (MojoFailureException e1) {
				e1.printStackTrace(logStream);
				logStream.println(lineSep);
			}
		}
	}

}
