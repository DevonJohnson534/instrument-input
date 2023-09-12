import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import be.tarsos.dsp.pitch.FastYin;

public class LineMain {
	
	private static float sampleRate;
	private static int bufferSize;
	
	private static FastYin pitchDetector;
	private static int volumeThreshold;
	private static double pitchThreshold; //aka yinThreshold, parameter that defines which peaks are kept as possible pitch candidates in pitch recognition
	private static TargetDataLine lineIn;
	
	private static KeyPresser keyPresser;
	
	private static boolean readingInput;
	private static String currentNote;
	
	public static void main(String[] args) {
		lineSetup();
		keyPresser = Main.keyPresser;
        
		volumeThreshold = 100;
        pitchThreshold = 0.20; //default is 0.20
        bufferSize = 4096;
        pitchDetector = new FastYin(sampleRate, bufferSize, pitchThreshold);
        
        readingInput = true;
        readLineInput();
	}
	
	private static void readLineInput() {
		// Continuously read input from the line-in
        byte[] buffer = new byte[bufferSize];
        while (readingInput) {
            int bytesRead = lineIn.read(buffer, 0, buffer.length); //params: buffer array to write to, offset from beginning of array, number of bytes to read

            // Calculate the average amplitude (volume) of the audio samples
            long sum = 0;
            for (int i = 0; i < bytesRead; i++) {
            	int sample = buffer[i] & 0xff; //Gets the unsigned value
                sum += Math.abs(sample);
            }
            int averageAmplitude = (int) (sum / (bytesRead / 2));

            if (averageAmplitude > volumeThreshold) {
                //Process the audio samples to detect pitch
                float pitch = detectPitch(buffer);
                if (pitch != -1) {
                    String noteName = getNoteFromPitch(pitch);
                    onNoteDetection(noteName);
                    Main.ui.updateNoteTextField(noteName);
                    //System.out.println("Detected pitch: " + pitch +" ,Detected note: " + note);
                }
                else{
                	onNoteDetection("UNPITCHED");
                    Main.ui.updateNoteTextField("UNPITCHED");
                	//System.out.println("Unpitched sound detected");
                }
            }
            else {
            	onNoNoteDetection();
            }
        }
	}
	//PRESS RELEASE NOTE HANDLING ==========================================================================
	private static void onNoteDetection(String noteName) {
		if (!(noteName.equals(currentNote))) {
			if (currentNote!=null)
				keyPresser.releaseKey(currentNote);
			keyPresser.pressKey(noteName);
		}
		currentNote = noteName;
	}
	private static void onNoNoteDetection() {
    	if (currentNote!=null)
    		keyPresser.releaseKey(currentNote);
    	currentNote = null;
	}
	
	//PITCH DETECTING =======================================================================================
    private static float detectPitch(byte[] audioData) {
        float[] samples = convertBytesToFloats(audioData);
        float pitch = pitchDetector.getPitch(samples).getPitch();
        return pitch;
    }
	
    private static float[] convertBytesToFloats(byte[] audioData) {
        float[] samples = new float[audioData.length];
        for (int i = 0; i < audioData.length; i++) {
        	//Normalize sample to range [-1, 1]
            samples[i] = (float) (audioData[i] / 128.0); //127 inclusive is max value for byte so this works
        }
        return samples;
    }
    
	//NOTE DETECTING =======================================================================================
    private static String getNoteFromPitch(float pitch) {
        int closestPitchIndex = findClosestNote(pitch);
        return Main.noteNames[closestPitchIndex];
    }

    private static int findClosestNote(float pitch)
	{
		int left = 0, right = Main.notePitches.length - 1;
		while (left < right) {
			if (Math.abs(Main.notePitches[left] - pitch) <= Math.abs(Main.notePitches[right] - pitch)) {
				right--;
			} else {
				left++;
			}
		}
		return left;
	}
	
	
    //LINE UTIL================================================================================
    public static void setBitdepth(boolean x) {
    	if (x) { //higher depth
    		//TODO
    	}
    	else { //lower depth
    	}
    }
    
    public static void closeLine() {
    	readingInput=false;
    	lineIn.close();
    }
    
    private static void lineSetup() {
		try {
			// Get the line-in audio input
		    lineIn = getLineIn();
		    if (lineIn == null) {
		        System.err.println("Line-in device not found");
		        return;
		    }
		    
		    //set up audio format
		    AudioFormat customFormat = new AudioFormat(44100, 8, 1, true, false);
		    sampleRate = customFormat.getSampleRate();
		   
		    //open and start line
		    lineIn.open(customFormat);
		    lineIn.start();
		    System.out.println("Line-in or microphone device is ready.");
		    
		    //debug print audio format
		    AudioFormat audioFormat = ((DataLine) lineIn).getFormat();
            System.out.println("AUDIO FORMAT: "+audioFormat); //print info
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private static TargetDataLine getLineIn() throws LineUnavailableException {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        //mixers
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] lineInfos = mixer.getTargetLineInfo();
            
            //lines
            for (Line.Info lineInfo : lineInfos) {
                if (lineInfo.getLineClass() == TargetDataLine.class) {
                    Line line = mixer.getLine(lineInfo);
                    
                    if (line instanceof TargetDataLine) {
                        return (TargetDataLine) line;
                    }
                }
            }
        }
        return null;
    }
    
    //GET/SET FOR UI =======================================================
    public static void setPitchThreshold(double x) {
    	pitchThreshold = x;
    	pitchDetector = new FastYin(sampleRate, bufferSize, pitchThreshold);
    }
    
    public static void setVolumeThreshold(int x) {
    	volumeThreshold = x;
    }

}
