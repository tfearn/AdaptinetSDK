The Adaptinet Distributed Computing SDK Version 0.91


Prerequisites
-------------
The Adaptinet SDK requires JDK version 1.5 or greater. See the Java web site for more details
at http://www.java.com.


Quick Start
-----------
Start the Adaptinet TransCeiver from the Start Menu Item - Start Adaptinet TransCeiver or (on UNIX)
via the bin/transceiver shell script.  Feel free to modify this script for your environment.  If the
TransCeiver does not start, the JRE probably cannot be found.  Check your classpath or JRE
installation.


Validate the Installation
-------------------------
1. Start the Adaptinet TransCeiver (see Quick Start above).  The RunTime Console should start with
   the server.  If it does not, check the transceiver.properties file and make sure that
   showconsole=true.  Note: Errors will display in the command window, THIS IS NORMAL because the
   TransCeiver is trying to connect to other peers.  If no other peers are running, you will see
   consistent errors trying to connect.
2. In the RunTime Console, change the Peer List item to point to port 8082 (ourselves).  The Peer 
   List item should be changed to 127.0.0.1:8082 (the local TransCeiver is running on port 8082).
3. Press the Ping button.  You should see a ping from yourself to yourself.
4. You are validated.

If you have problems validating the installation, check the Programmers Guide for more information.


Running the Sample Programs
---------------------------
The sample programs are located in the ./src/Samples directory of the installation.  The classes
have been prebuilt and the plugins.xml file has been configured with each sample.  To run a
specific sample change the plugins.xml file <preload> element for a specific plug-in to 1 instead
of 0.  Or you can use the Administrators Console to change the plugin preload setting.

Refer to the Programmers Guide for more information.


http://www.adaptinet.org