import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

import org.nlogo.api.*;

public class SoundxExtension extends DefaultClassManager {
		
  public void load(PrimitiveManager primitiveManager) {
    primitiveManager.addPrimitive("current-dir", new getModelDirectory());//gets current working directory
	primitiveManager.addPrimitive("play-soundfile", new XPlay());   //play audio file with gain(db - max 6) and pan(left to right\-1.0 to 1.0)
//	primitiveManager.addPrimitive("play-freq", new xPlayFreqLocal());   //Canceled
	primitiveManager.addPrimitive("get-audio-length", new getAudioLength());   //getAudioLength in ms
//	primitiveManager.addPrimitive("play-from-to", new PlayFromToMs());   //Canceled
  }
  
//  public static class xPlayFreqLocal extends DefaultCommand {
//
//	    public Syntax getSyntax() {
//	    	return Syntax.commandSyntax(new int[]{Syntax.StringType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType(), Syntax.NumberType()});
//	    }
//
//
//		@Override
//		public void perform(Argument[] args, Context arg1)
//				throws ExtensionException, LogoException {
//			File file1 = new File(args[0].getString());
//	    	Thread th = new Thread(new XPlayFreq(file1, (float)(args[1].getDoubleValue()),(float)(args[2].getDoubleValue()), (int)(args[3].getIntValue()),(int)(args[4].getIntValue())));//.start();
//	    	th.start();
//		}
//	  }
  
  
  public static class getAudioLength extends DefaultReporter {

	    public Syntax getSyntax() {
	      return Syntax.reporterSyntax(new int[]{Syntax.StringType()},
	    		  Syntax.NumberType());
	    }

	    public Object report(Argument args[], Context context)
	        throws ExtensionException, LogoException {
	    	File file1 = new File(args[0].getString());
	    	
	    	GetAudioLength audioInstance = new GetAudioLength(file1);
	    	Thread th = new Thread(audioInstance);//.start();
	    	th.start();
	    	while (th.isAlive()){ try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
	      return Double.valueOf(audioInstance.getAudioLength());
	    }
	  }
  
  // getModelDirectory() - Created by Charles Staelin(pathdir extension)
  public static class getModelDirectory extends DefaultReporter {

      @Override
      public Syntax getSyntax() {
          return Syntax.reporterSyntax(Syntax.StringType());
      }

      public Object report(Argument args[], Context context) throws ExtensionException {

          String modelDirName;
          try {
              modelDirName = context.attachModelDir(".");
          } catch (java.net.MalformedURLException ex) {
              throw new ExtensionException(ex);
          }
          File f = new File(modelDirName);//gets current working directory
          try {
              return (f.getCanonicalFile()).toString() + "/";
          } catch (Exception ex) {
              ExtensionException eex = new ExtensionException(ex);
              eex.setStackTrace(ex.getStackTrace());
              throw eex;
          }
      }
  }
}

//GetAudioLength in ms
class GetAudioLength implements Runnable{
    private File soundurl;
    private Clip clip;
    private int audioLength;
    private AudioInputStream ain;
    
    GetAudioLength(File soundurl) {
      //super();
      this.soundurl = soundurl;
      //runThis();
    }


    public void run() {
      try {   	  
 	  
    	  ain = AudioSystem.getAudioInputStream(soundurl);

          try {
              DataLine.Info info =
                  new DataLine.Info(Clip.class,ain.getFormat( ));
              clip = (Clip) AudioSystem.getLine(info);
              clip.open(ain);
//              info = null;
          }
          finally { // We're done with the input stream.
              ain.close( );
          }
          // Get the clip length in microseconds and convert to milliseconds
          audioLength = (int)(clip.getMicrosecondLength( ) / 1000);
          clip.close();
          
          Thread.sleep(10);

      	  
      } catch (Exception e) {
        org.nlogo.util.Exceptions.ignore(e);
      } 
    }
    
    public int getAudioLength(){
    	return audioLength;
    }
  }
// This extension was developed by Eyal Ofeck, Sharona T. Levy & Orly Lahav
// as part of  "Listening to Complexity" ISF-funded research project
// The related learning environment includes sonified models for blind students' exploration of models
// The project is led by Sharona T. Levy (University of Haifa) and Orly Lahav (Tel-Aviv University)