Welcome to Geronimo
===================

To build me please install Maven from here - version b10 or later.

	http://maven.apache.org/

In addition you should have JDK 1.4.x installed with JAVA_HOME
environment defined to point to this JDK.

Then just type:

	maven
	
To build Geronimo running all of the unit test cases:

	maven build

To do a clean rebuild of Geronimo type

	maven clean
    maven build

NOTE: maven rebuild currently has some problems due to a bug in the reactor.

To only build select modules (assumes that depends are installed already):

    maven -Dmodules=common,core

To clean your workspace:

    maven clean

NOTE: You need to build the server first for this to work due to a problem
      with dependencies & the reactor.

To removal all build generated files:

    maven clobber

To try run the Geronimo server type:

	maven run
		
For the HTML website:

	maven site