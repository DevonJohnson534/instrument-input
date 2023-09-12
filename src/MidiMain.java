import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

public class MidiMain {
	private static Transmitter transmitter;
	private static MidiDevice device;
	private static KeyPresser keyPresser;
	private static ArrayList<MidiDevice.Info> goodDevices;
	private static ArrayList<String> goodDevicesNames;

	public static final int midiStartNote = 12; //this is the note where they start to line up with the noteNames
	
    public static void main(String[] args) {
    	midiSetupA();
		keyPresser = Main.keyPresser;
    }
    
    //UTIL ==========================================================================
    public static void midiSetupA() { //this makes a list of devices for the user to choose from
        try {        	
        	goodDevicesNames = new ArrayList<String>();
        	goodDevices = new ArrayList<MidiDevice.Info>();
        	
            MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
            for (MidiDevice.Info info : midiDeviceInfo) {
            	MidiDevice device = MidiSystem.getMidiDevice(info);
            	if (device instanceof Synthesizer | device instanceof Sequencer) {
            		continue;
            	}
            	if (device.getMaxTransmitters() != 0) {
            		goodDevicesNames.add(info.getName());
            		goodDevices.add(info);
            	}
            }
            if (goodDevices.size()==0) {
            	System.out.println("no midi devices avaliable.");
            	Main.ui.noMidiDevicesAvaliable();
            	return;
            }
            Main.ui.setMidiDeviceOptions(goodDevicesNames.toArray(new String[goodDevicesNames.size()]));
            midiSetupB(goodDevices.get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void midiSetupB(MidiDevice.Info dInfo) { //this is called once the user selects a device and can be called many times
    	System.out.println("setupB");
    	if (device!=null)
    		device.close();
        try {
            device = MidiSystem.getMidiDevice(dInfo);   //4(reciever) or 5(transmitter), for my keyboard and computer only

            device.open();
            transmitter = device.getTransmitter();
            //Create a new receiver to process the received MIDI messages
            transmitter.setReceiver(new MidiReceiver());

            System.out.println("MIDI is ready.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void closeMidi() {
    	if (device==null)
    		return;
    	device.close();
    }
    //GET SET ========================================================================
    public static ArrayList<MidiDevice.Info> getGoodDevices(){
    	return goodDevices;
    }
    public static ArrayList<String> getGoodDevicesNames(){
    	return goodDevicesNames;
    }
    
    //MIDI RECEIVER ==================================================================
    private static class MidiReceiver implements Receiver {
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int cmd = sm.getCommand();
                int noteNum = sm.getData1();
                int velocity = sm.getData2();

                if (cmd == ShortMessage.NOTE_ON) {
                	String noteName = Main.noteNames[noteNum-midiStartNote];
                	if(velocity == 0) {
                		keyPresser.releaseKey(noteName);
                	}else {
                		keyPresser.pressKey(noteName);
                		Main.ui.updateNoteTextField(noteName);
                	}
                }
            }
        }

        @Override
        public void close() { //even though there's nothing in here it still implicitly closes
            // Empty implementation (no resources to release)
        }
    }
}
