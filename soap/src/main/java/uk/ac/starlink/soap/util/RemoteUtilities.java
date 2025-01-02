/*
 * Copyright (C) 2003 Central Laboratory of the Research Councils
 *
 *  History:
 *     29-SEP-2004 (Alasdair Allan):
 *       Modified for generic use and put into the sopaserver package
 *     03-MAR-2004 (Alasdair Allan):
 *       Adapted for Web Services applications in ORAC-DR
 *     12-JUL-2001 (Peter W. Draper):
 *       Original version.
 */
 
package uk.ac.starlink.soap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.lang.System;

import uk.ac.starlink.util.AsciiFileParser;

/**
 * This class provides utility methods to allow the Soap Server to read and
 * write a contact file. Uses the adam user directory (if this has been pushed
 * into the Java properties), or ~/.soap directory if this is undefined.
 *
 * @author Peter W. Draper, Alasdair Allan
 * @version $Id$
 */
public class RemoteUtilities
{

    /**
     * System properties
     */
     static Properties javaProp = System.getProperties();
  
    /**
     *  Create an instance. Private all methods static.
     */
    private RemoteUtilities()
    {
        // Nothing to do.
    }

    /**
     * Write contact details to a file only readable by the owner
     * process and privileged local users.
     * <p>
     * These details are used to authenticate any requests.
     * <p>
     * The file contains the simple line:
     * <pre>
     *    hostname port_number cookie
     * </pre>
     * The cookie is generated by this method and returned as its
     * result.
     *
     * @param port the port number being used by a server process that
     *             is listening for connections.
     * @return the cookie for authenticating connections. This is null
     *         if a failure to write the contact file is encountered.
     */
    public static String writeContactFile( int port, String appName )
    {
        String cookie = null;

        //  Open the contact file. This needs protection from prying
        //  eyes, so the next is _UNIX_ specific (TODO: something
        //  about this?).  The file is re-created every time, so
        //  connections are only available to the last instance.
        File contactFile = getConfigFile( appName + ".remote" );
        if ( contactFile != null ) {
            try {
                contactFile.createNewFile();
                Runtime.getRuntime().exec( "chmod 600 " +
                                           contactFile.getPath() );
            } 
            catch (Exception e) {
                // Do nothing, chmod can fail validly under Windows.
                //e.printStackTrace();
            }

            //  Add the information we want.
            try {
                PrintStream out =
                    new PrintStream( new FileOutputStream( contactFile ) );
                InetAddress addr = InetAddress.getLocalHost();
                String hexVal = Integer.toHexString((int)(Math.random()*12345));
                cookie = hexVal + addr.hashCode();
                out.println( addr.getHostName() + " " + port + " " + cookie );

            } 
            catch (Exception e) {
                //  Do nothing
            }
        }
        //  Return the cookie which should be used to authenticate any
        //  remote connections.
        return cookie;
    }

    /**
     * Parse the contact file returning its contents as an Object array.
     *
     * @return array of three Objects. These are really the hostname
     *         String, an Integer with the port number and a String
     *         containing the validation cookie. Returns null if not
     *         available.
     */
    public static Object[] readContactFile( String appName )
    {
        File contactFile = getConfigFile( appName + ".remote" );
        if ( contactFile == null || ! contactFile.canRead() ) {
            return null;
        }

        //  Ok file exists and we can read it, so open it and get the
        //  contents.
        AsciiFileParser reader = new AsciiFileParser( contactFile );
        String host = reader.getStringField( 0, 0 );
        Integer port = Integer.valueOf( reader.getIntegerField( 0, 1 ) );
        String cookie = reader.getStringField( 0, 2 );

        //  Construct the result.
        Object[] result = new Object[3];
        result[0] = host;
        result[1] = port;
        result[2] = cookie;
        return result;
    }
    
    
    /**
     * The name of the directory used for storing configuration
     * information. This directory is created if it doesn't exist
     * already.
     */
    public static File getConfigDirectory()
    {
        File dir = null;
        if ( javaProp.getProperty("adam.user") != null ) {
           dir = new File ( javaProp.getProperty( "adam.user" ) );
        } else {
           dir = new File( javaProp.getProperty( "user.home" ), ".soap" );
        }
        if ( ! dir.exists() ) {
            try {
                dir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                dir = null;
            }
        } else if ( ! dir.isDirectory() ) {
            System.err.println( "Cannot create a directory: " +
                                dir.getName() + "as a file with "+
                                "this name already exists" );
            dir = null;
        }
        return dir;
    }

    /**
     * Construct the proper name of a file stored in the configuration
     * directory. 
     *
     * @param name the name of the file to be stored/located in the
     *             the configuration directory.
     */
    public static File getConfigFile( String name )
    {
        return new File( getConfigDirectory(), name );
    }


}