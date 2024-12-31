# Notice of javax.xml.soap

The message below shows up when compiling `AppHttpSOAPServer.java`

`cannot access javax.xml.soap.Name`

`cannot access javax.xml.soap.SOAPBodyElement`

It is due to the removal of JAX-WS from Java 11. 

The solution is to use alternative versions of the Java EE technologies, 
at least Jakarta EE 8 update (Mar 2020) is working.


