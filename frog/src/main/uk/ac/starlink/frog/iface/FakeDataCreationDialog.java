package uk.ac.starlink.frog.iface;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Double;
import java.lang.Math;

import uk.ac.starlink.frog.Frog;
import uk.ac.starlink.frog.iface.PlotControlFrame;
import uk.ac.starlink.frog.iface.images.ImageHolder;
import uk.ac.starlink.frog.data.TimeSeriesManager;
import uk.ac.starlink.frog.data.MEMTimeSeriesImpl;
import uk.ac.starlink.frog.data.TimeSeriesComp;
import uk.ac.starlink.frog.data.TimeSeries;
import uk.ac.starlink.frog.data.Ephemeris;
import uk.ac.starlink.frog.util.FrogDebug;
import uk.ac.starlink.frog.util.FrogException;

/**
 * Class that displays a dialog window to allow the user to do
 * create some fake data by adding sin functions.
 *
 * @since 22-MAR-2003
 * @author Alasdair Allan
 * @version $Id$
 * @see TimeSeries
 */
public class FakeDataCreationDialog extends JInternalFrame
{

   /**
     * Image for sin() function
     */ 
    protected static ImageIcon sinImage = 
        new ImageIcon( ImageHolder.class.getResource( "sin_function.gif" ) );

    /**
     *  Application wide debug manager
     */
    protected FrogDebug debugManager = FrogDebug.getReference();

    /**
     *  Manager class for TimeSeries
     */
    protected TimeSeriesManager 
         seriesManager = TimeSeriesManager.getReference();
   
    /**
     * TextEntryField for the gamma vlaue
     */   
     JTextField gammaEntry = new JTextField();
        
    /**
     * Gamma value
     */
     double gamma;
   
    /**
     * TextEntryField for the number of data points
     */   
     JTextField numEntry = new JTextField();
        
    /**
     * Number of data points
     */
     int num;
   
    /**
     * TextEntryField for the start time
     */   
     JTextField lowEntry = new JTextField();
        
    /**
     * Start time
     */
     double low;
   
    /**
     * TextEntryField for the end time
     */   
     JTextField highEntry = new JTextField();
        
    /**
     * End time
     */
     double high; 
              
    /**
     * TextEntryField for the amplitude vlaue
     */   
     JTextField amplitudeEntry = new JTextField();
             
    /**
     * Amplitude value
     */
     double amplitude;  

    /**
     * TextEntryField for the period vlaue
     */   
     JTextField periodEntry = new JTextField();
                      
    /**
     * Period value
     */
     double period;      

    /**
     * TextEntryField for the zero point vlaue
     */   
     JTextField zeroPointEntry = new JTextField();
                      
    /**
     * Zero Point value
     */
     double zeroPoint;        
                
    /**
     * Frog object that this is being created from...
     */
     Frog frame = null;
        
    /**
     * Newly constructed  TimeSeries object
     */
     TimeSeries newSeries = null;
    
    /**
     * Create an instance of the dialog and proceed to do arithmetic
     *
     * @param f The PlotControlFrame holding the TimeSeries of interest
     */
    public FakeDataCreationDialog( )
    {
        super( "Fake Data", false, true, false, false );
            
        //enableEvents( AWTEvent.WINDOW_EVENT_MASK ); 
        try {
            // Build the User Interface
            initUI( );
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the user interface components.
     */
    protected void initUI( ) throws Exception
    {

       // Setup the JInternalFrame
       setDefaultCloseOperation( JInternalFrame.DISPOSE_ON_CLOSE );
       addInternalFrameListener( new DialogListener() );  
         
       // grab Frog object  
       frame = seriesManager.getFrog();
             
       // grab location and position window
       Dimension fakeSize = getSize();
       Dimension frameSize = frame.getSize();
       setLocation( ( frameSize.width - fakeSize.width ) / 4 ,
                    ( frameSize.height - fakeSize.height ) / 4  );

       // create entry widgets
       // --------------------
       JLabel numLabel = new JLabel("<html>&nbsp;Number of Points&nbsp;<html>");
       numLabel.setBorder( BorderFactory.createEtchedBorder() );
       numEntry.setColumns(14);      

       JLabel lowLabel = new JLabel("<html>&nbsp;Start Time&nbsp;<html>");
       lowLabel.setBorder( BorderFactory.createEtchedBorder() );
       lowEntry.setColumns(14); 

       JLabel highLabel = new JLabel("<html>&nbsp;End Time&nbsp;<html>");
       highLabel.setBorder( BorderFactory.createEtchedBorder() );
       highEntry.setColumns(14);    
           
       // create the function line
       // ------------------------
       JLabel functionLabel = new JLabel( sinImage );
       functionLabel.setBorder( BorderFactory.createEtchedBorder() );

       // create entry widgets
       // --------------------
       JLabel gammaLabel = new JLabel("<html>&nbsp;Gamma&nbsp;<html>");
       gammaLabel.setBorder( BorderFactory.createEtchedBorder() );
       gammaEntry.setColumns(14);

       JLabel ampLabel = new JLabel("<html>&nbsp;Amplitude&nbsp;<html>");
       ampLabel.setBorder( BorderFactory.createEtchedBorder() );
       amplitudeEntry.setColumns(14);

       JLabel perLabel = new JLabel("<html>&nbsp;Period&nbsp;<html>");
       perLabel.setBorder( BorderFactory.createEtchedBorder() );
       periodEntry.setColumns(14);
       
       JLabel zpLabel = new JLabel("<html>&nbsp;Zero Point&nbsp;<html>");
       zpLabel.setBorder( BorderFactory.createEtchedBorder() );
       zeroPointEntry.setColumns(14);             

       // Stuff them into the main panel
       // ------------------------------

       GridBagConstraints constraints = new GridBagConstraints();
       constraints.weightx = 1.0;
       constraints.weighty = 1.0;
       constraints.fill = GridBagConstraints.BOTH;
       
       JPanel mainPanel = new JPanel();
           
       // create the main panel
       mainPanel.setLayout( new GridBagLayout() );
       
       constraints.gridx = 0;  
       constraints.gridy = 0;  
       mainPanel.add( numLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 0;        
       mainPanel.add( numEntry, constraints );
       
       constraints.gridx = 0;  
       constraints.gridy = 1;        
       mainPanel.add( lowLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 1;        
       mainPanel.add( lowEntry, constraints );
      
       constraints.gridx = 0;  
       constraints.gridy = 2;        
       mainPanel.add( highLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 2;        
       mainPanel.add( highEntry, constraints );
              
       constraints.gridx = 0;  
       constraints.gridy = 3;        
       constraints.gridwidth = 2;       
       mainPanel.add( functionLabel, constraints );
       constraints.gridwidth = 1;       
         
       constraints.gridx = 0;  
       constraints.gridy = 4;        
       mainPanel.add( gammaLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 4;        
       mainPanel.add( gammaEntry, constraints );         
         
       constraints.gridx = 0;  
       constraints.gridy = 5;        
       mainPanel.add( ampLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 5;        
       mainPanel.add( amplitudeEntry, constraints );          
         
       constraints.gridx = 0;  
       constraints.gridy = 6;        
       mainPanel.add( perLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 6;        
       mainPanel.add( periodEntry, constraints ); 
         
       constraints.gridx = 0;  
       constraints.gridy = 7;        
       mainPanel.add( zpLabel, constraints );
       
       constraints.gridx = 1;  
       constraints.gridy = 7;        
       mainPanel.add( zeroPointEntry, constraints );        
       
                                    
       // create the button panels
       JPanel buttonPanel = new JPanel( new BorderLayout() );
       JPanel buttons = new JPanel( new BorderLayout() );
              
       // create the button panel
       // -----------------------
       JLabel buttonLabel = new JLabel ( "          " );
       JButton okButton = new JButton( "Ok" );
       okButton.addActionListener( new ActionListener() {
           public void actionPerformed(ActionEvent e) { 
              debugManager.print( "  Faking periodic data..." );
              
              boolean status = doFake( );
              
              // If we have bad status recursively call ourselves 
              // and get better numbers.
              if ( status != true ) {
                 
                 // dispose of the current incarnation
                 dispose();
                 
                 // create a new version
                 debugManager.print(  "    Respawning the dialog..." );
                 FakeDataCreationDialog fake = new FakeDataCreationDialog( );
                 seriesManager.getFrog().getDesktop().add(fake);
                 fake.show();     
              }  
              
              // we must have sucessfully folded the series, or at least
              // handled all the errors, or we won't be here. Get rid of
              // the dialog here because currently its only hidden.
              dispose();   
           }
        }); 
               
       JButton cancelButton = new JButton( "Cancel" );
       cancelButton.addActionListener( new ActionListener() {
           public void actionPerformed(ActionEvent e) { 
              dispose();
           }
        }); 
        
       buttons.add( okButton, BorderLayout.CENTER );
       buttons.add( cancelButton, BorderLayout.EAST );
 
       buttonPanel.add( buttonLabel, BorderLayout.CENTER );
       buttonPanel.add( buttons, BorderLayout.EAST );
    
       // display everything
       JPanel contentPane = (JPanel) this.getContentPane();
       contentPane.setLayout( new BorderLayout() );
       
       contentPane.add(mainPanel, BorderLayout.CENTER );
       contentPane.add(buttonPanel, BorderLayout.SOUTH );
       
       pack();

    }

    /**
     * Method that actualy does the work to faking of data  and creates 
     * a new TimeSeriesComp and associated PlotControlFrame.
     */
     protected boolean doFake( )
     {
         debugManager.print( "    Calling doFake()..." );

         // Grab selected series values
         String numString = numEntry.getText();
         String lowString = lowEntry.getText();
         String highString = highEntry.getText();

         String gammaString = gammaEntry.getText();
         String amplitudeString = amplitudeEntry.getText();
         String periodString = periodEntry.getText();
         String zeroPointString = zeroPointEntry.getText();
         
         // define floating or Java will have a cow
         num = 0; // int!
         low = 0.0;
         high = 0.0;
         gamma = 0.0;
         amplitude = 0.0;
         period = 0.0;
         zeroPoint = 0.0;
        
         // convert primitive types
         try {
             num = (new Integer(numString)).intValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid number of data points..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid number of data points",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }

         try {
             low = (new Double(lowString)).doubleValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid Start Time (need double)..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid Start Time",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }

         try {
             high = (new Double(highString)).doubleValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid End Time (need double)..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid End Time",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }
         
         try {
             gamma = (new Double(gammaString)).doubleValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid Gamma..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid Gamma entered",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }
         
         try {
             amplitude = (new Double(amplitudeString)).doubleValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid Amplitude..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid Amplitude entered",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }
         
         try {
             period = (new Double(periodString)).doubleValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid Period..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid Period entered",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }
         
         try {
             zeroPoint = (new Double(zeroPointString)).doubleValue();
         } catch ( Exception e ) {
             debugManager.print( "      Invalid Zero Point..." );
             dispose();      
             JOptionPane message = new JOptionPane();
             message.setLocation( this.getLocation() );
             message.showMessageDialog( this,
                                        "Invalid entry: " + e.getMessage(),
                                        "Invalid Zero Point entered",
                                        JOptionPane.ERROR_MESSAGE);
             return false;
         }         
         
         // We have valid entries, at least in theory
         debugManager.print( "      Number     " + numString );
         debugManager.print( "      Start      " + lowString );
         debugManager.print( "      End        " + highString );
         debugManager.print( "      Gamma      " + gammaString ); 
         debugManager.print( "      Amplitude  " + amplitudeString );
         debugManager.print( "      Period     " + periodString ); 
         debugManager.print( "      Zero Point " + zeroPointString );
         
         // Hide the dialog, we'll dispose of it later...
         hide();
             
         // Call the algorithim
         debugManager.print( "      Calling threadFakeTimeSeries()..." ); 
         threadFakeTimeSeries();
          
         // doArithmetic() seems to have completed sucessfully.
         return true;
    }
    
    /**
     * Do faking inside its own thread so we don't block the 
     * GUI or event threads. Called from doCombine().
     */
    protected void threadFakeTimeSeries()
    {
        debugManager.print( "        threadArithTimeSeries()" );
        setWaitCursor();

        //  Now create the thread that reads the spectra.
        Thread loadThread = new Thread( "Fake Series" ) {
                    public void run() {
                        try {
                            debugManager.print("        Spawned thread...");
                            fakeSeries();
     
                        }
                        catch (Exception error) {
                            error.printStackTrace();
                        }
                        finally {

                            //  Always tidy up and rewaken interface
                            //  when complete (including if an error
                            //  is thrown).
                            resetWaitCursor();
                        }
                    }
         };

         //  Start loading the time series files.
         loadThread.start();
    }

       
    /**
     * The method that carried out the required stuff to fake the
     * data. Called as a new thread from threadFoldTimeSeries().
     */ 
    protected void fakeSeries() 
    {
         debugManager.print( "         fakeSeries()" );
         
         // copy the arrays
         double xData[] = new double[num];
         double yData[] = new double[num];

         // Do arithmetic
         // -------------
         
         // work out step size
         double step = (high-low)/num;
         
         for ( int k = 0; k < xData.length; k++ ) {
             
             // fill the xData array
             xData[k] = low + ((double)k*step);
             
             // calculate the sin() at x position
             yData[k] = 
               gamma + ( amplitude * Math.sin( ((2.0*Math.PI)/ period ) *
                         ( xData[k] - zeroPoint )));
                              
         }         
        
         // Create a new TimeSeriesComp object
         MEMTimeSeriesImpl memImpl = null;
         
         // Create a MEMTimeSeriesImpl  
         memImpl = new MEMTimeSeriesImpl( "Fake Periodic Data" );
         
         // Add data to the MEMTimeSeriesImpl 
         memImpl.setData( yData, xData );
         
         // create a real timeseries
         try {
           newSeries = new TimeSeries( memImpl );  
           newSeries.setType( TimeSeries.FAKEDATA );
         } catch ( FrogException e ) {
           debugManager.print("          FrogException creating TimeSeries...");
           e.printStackTrace();
           return;
         }        
          
         // Load the new series into a PlotControlFrame
         debugManager.print( "            Calling threadLoadNewSeries()..." );
         threadLoadNewSeries(); 
    }
    
    /**
     * Load the time series stored in the newSeries object. Use a new
     * Thread so that we do not block the GUI or event threads. 
     */
    protected void threadLoadNewSeries()
    {
        debugManager.print( "              threadLoadNewSeries()" );
        
        if ( newSeries != null ) {

            setWaitCursor();

            //  Now create the thread that reads the spectra.
            Thread loadThread = new Thread( "New Series loader" ) {
                    public void run() {
                        try {
                            debugManager.print(
                                       "                Spawned thread...");
                            addNewSeries();
                        }
                        catch (Exception error) {
                            error.printStackTrace();
                        }
                        finally {

                            //  Always tidy up and rewaken interface
                            //  when complete (including if an error
                            //  is thrown).
                            resetWaitCursor();
                        }
                    }
                };

            //  Start loading the time series files.
            loadThread.start();
        } else {
        
           // Something has gone very wrong, oh dear!
           debugManager.print( "                newSeries == null");   
           debugManager.print( "                Aborting process...");        
 
       }

    }             

    
    /**
     * Add the new series to the main desktop inside a new PlotControlFrame
     */ 
    protected void addNewSeries() 
    {
          // Debugging is slow, tedious and annoying
          debugManager.print( "                  addnewSeries()" );

          debugManager.print( "                    Full Name    = " + 
                          newSeries.getFullName() );
          debugManager.print( "                    Short Name   = " +  
                          newSeries.getShortName() ); 
          debugManager.print( "                    Series Type  = " +  
                          newSeries.getType() );
          debugManager.print( "                    Has Errors?  = " +  
                          newSeries.haveYDataErrors() );
          debugManager.print( "                    Draw Errors? = " +  
                          newSeries.isDrawErrorBars() );
          debugManager.print( "                    Data Points  = " +  
                          newSeries.size() );
          debugManager.print( "                    Data Format  = " +  
                          newSeries.getDataFormat() );
          debugManager.print( "                    Plot Style   = " +  
                          newSeries.getPlotStyle() );
          debugManager.print( "                    Mark Style   = " +  
                          newSeries.getMarkStyle() );
          debugManager.print( "                    Mark Size    = " +  
                          newSeries.getMarkSize() ); 
    
          // add the series to the MainWindow by calling addSeries() in
          // the main Frog Object, by grabbing the reference to the main
          // frog object stored in the TimeSeriesManager. This is an unGodly
          // hack and I should be shot for doing this...
          seriesManager.getFrog().addSeries( newSeries );
   
    }
    
    /**
     * Set the main cursor to indicate waiting for some action to
     * complete and lock the interface by trapping all mouse events.
     */
    protected void setWaitCursor()
    {
        //debugManager.print("        setWaitCursor()");
        Cursor wait = Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR );
        Component glassPane = seriesManager.getFrog().getGlassPane();
        glassPane.setCursor( wait );
        glassPane.setVisible( true );
        glassPane.addMouseListener( new MouseAdapter() {} );
    }

    /**
     * Undo the action of the setWaitCursor method.
     */
    protected void resetWaitCursor()
    {
        //debugManager.print("        resetWaitCursor()");
        seriesManager.getFrog().getGlassPane().setCursor( null );
        seriesManager.getFrog().getGlassPane().setVisible( false );
    }
   
}
