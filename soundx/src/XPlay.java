import org.nlogo.api.*;

import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;
import javax.sound.sampled.*;

public class XPlay extends DefaultCommand {
    public Syntax getSyntax() {
      return Syntax.commandSyntax(new int[]{Syntax.StringType(), Syntax.NumberType(), Syntax.NumberType()});
    }

  public void perform(Argument args[], Context context)
      throws ExtensionException, LogoException{
	  try{
		// can be used instead to locate files related to model directory
		//File file1 = new File(context.attachModelDir(".") + "/" + args[0].getString());
		File file1 = new File(args[0].getString());
		if (!file1.exists())
			throw new IOException("File not found : " + file1.getAbsolutePath());
		ExecutorService exec = Executors.newFixedThreadPool(120);
		
		exec.execute(new PlaySoundThread(file1,(float)(args[1].getDoubleValue()),(float)(args[2].getDoubleValue())));
		//new Thread(new PlaySoundThread(file1,(float)(args[1].getDoubleValue()),(float)(args[2].getDoubleValue()))).start();
	    // Shut down the executor
		
		exec.shutdown();
		
      } catch (IOException ex) {
    	  throw new ExtensionException(ex);
      }
  }
}

class PlaySoundThread implements Runnable{
    private File soundurl;
    private Float gain,pan;
    private Clip clip;
    private int audioLength;
    private AudioInputStream ain;
    
    PlaySoundThread(File soundurl,Float gain, Float pan) {
      //super();
      this.soundurl = soundurl;
      this.gain = gain;
      this.pan = pan;
    }


    public void run() {
      try {   	  

    	  //AudioSystem.getAudioFileFormat(soundurl);
    	  
    	  ain = AudioSystem.getAudioInputStream(soundurl);

          try {
              DataLine.Info info =
                  new DataLine.Info(Clip.class,ain.getFormat( ));
              clip = (Clip) AudioSystem.getLine(info);
              clip.open(ain);
              info = null;
          }
          finally { // We're done with the input stream.
//              ain.close( );
          }
          // Get the clip length in microseconds and convert to milliseconds
          audioLength = (int)(clip.getMicrosecondLength( ) / 1000);
      
      //set FloatControls: i.e. gain, pan
          if (pan > 1) {pan = 1f;}
          if (pan < -1) {pan = -1f;}
          if (gain > 6) {gain = 6f;}
	  FloatControl panControl = (FloatControl)clip.getControl(FloatControl.Type.PAN);
      FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
      panControl.setValue(pan);
      gainControl.setValue(gain); 
      //panControl.shift(-1.0f, 1.0f, audioLength);
      //gainControl.shift(from, to, microseconds)
      //	The Balance control differs from the Pan control in that it controls the relative 
      //	levels of two signals (Left and Right) at their outputs
	  
       clip.start();

       Thread.sleep((audioLength * 2) + 5000); //p8// * 2 to * 4
       ain.close( );
      } catch (Exception e) {
        org.nlogo.util.Exceptions.ignore(e);
      } finally{  
    	  //clip.stop();
    	  while(clip.isRunning()){
    	  		try {
    				Thread.sleep(100);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	  }
          clip.close();
          clip.setMicrosecondPosition(0);
        //clip.stop();//p8// add clip.stop();
      }
    }
  }
// This extension was developed by Eyal Ofeck, Sharona T. Levy & Orly Lahav
// as part of  "Listening to Complexity" ISF-funded research project
// The related learning environment includes sonified models for blind students' exploration of models
// The project is led by Sharona T. Levy (University of Haifa) and Orly Lahav (Tel-Aviv University)