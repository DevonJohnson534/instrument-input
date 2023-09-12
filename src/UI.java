import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UI extends JFrame {

	private JPanel contentPane;
	
	private JButton newKeybindBttn;
	private JTextPane newKbInfoPane;
	
	private JPanel lineSettings;
	private JPanel midiSettings;
	private JPanel noSettings;
	
	private JTextPane recentNotesPane;
	
	private boolean ignoreUnpitched; //only for making keybinds
	private ArrayList<String> playedNotes;
	
	private JList<String> kbList;
	private String kbListSelected;
	
	private JComboBox<String> deviceSelector; //for midi
	private JTextPane midiDevicesInfoPane;
	
	private JSlider mouseXspeedSldr;
	private JSlider mouseYspeedSldr;
	private JSlider scrollAmountSldr;
	private JLabel xSpeedLbl;
	private JLabel ySpeedLbl;
	private JLabel scrollAmountLbl;

	//INPUT METHODS =======================================================================
	private void onUnpitchedToggle(boolean selected) {
		ignoreUnpitched = selected;
	}
	//private void onBitdepthToggle(boolean selected) {
	//	LineMain.setBitdepth(selected);
	//}
	private void onInputTypeSelected(String type) {
        if (type.equals("MIDI")) {
        	if (Main.getLineOpen())
        		Main.closeLine();
        	Thread midiThread = new Thread(() -> {
                System.out.println("MIDI input selected");
                Main.startMidi();
        	});
        	midiThread.start();
        	showInputControls("MIDI");

        } else if (type.equals("Line-In/Mic")) {
        	if (Main.getMidiOpen())
        		Main.closeMidi();
        	Thread lineThread = new Thread(() -> {
                System.out.println("Line-In input selected");
            	Main.startLine();
        	});
        	lineThread.start();
        	showInputControls("Line");
        	
        } else if (type.equals("None")) {
	    	if (Main.getMidiOpen())
	    		Main.closeMidi();
        	if (Main.getLineOpen())
        		Main.closeLine();
        	showInputControls("None");
	    }
	}
	private void onMidiDeviceSelected(String deviceName) {
		ArrayList<MidiDevice.Info> devices = MidiMain.getGoodDevices();
		ArrayList<String> names = MidiMain.getGoodDevicesNames();
		
		MidiDevice.Info device = devices.get(names.indexOf(deviceName));
		MidiMain.midiSetupB(device);
	}
	private void onVolumeChange(int volume) {
		System.out.println(volume);
		LineMain.setVolumeThreshold(volume);
	}
	private void onPitchChange(double pitch) {
		System.out.println(pitch);
		LineMain.setPitchThreshold(pitch);
	}
	private void onNewKeybind(int keycode) { //activates when the key is pressed
        if (playedNotes.size()>=1) {
        	String lastNote = playedNotes.get(playedNotes.size()-1);
        	Main.slHandler.setKeybind(keycode, lastNote);
        	
        	kbList.setListData(Main.slHandler.getKeybindsAsArray());
        	if (keycode < 65500)
        		newKbInfoPane.setText(lastNote + " mapped to " + KeyEvent.getKeyText(keycode));
        	else
        		newKbInfoPane.setText(lastNote + " mapped to " + Main.keyPresser.getMouseActionName(keycode));
        }else
        	newKbInfoPane.setText("Play a note before trying to map something to it.");
	}
	private void onListSelection(String selectedElement){
		System.out.println(selectedElement + " selected from keybind list");
		kbListSelected = selectedElement;
	}
	private void onDeleteKb() {
		Main.slHandler.removeKeybind(kbListSelected.split("=")[0]);
    	kbList.setListData(Main.slHandler.getKeybindsAsArray());
		newKbInfoPane.setText("Keybind deleted");
	}
	//OUTPUT METHODS =======================================================================
	private void showInputControls(String type) {
		midiSettings.setVisible(false);
		lineSettings.setVisible(false);
		noSettings.setVisible(false);
		if (type.equals("MIDI")){
			midiSettings.setVisible(true);
		}
		else if(type.equals("Line")){
			lineSettings.setVisible(true);
		}
		else{
			noSettings.setVisible(true);
		}
	}
	public void noMidiDevicesAvaliable() {
		midiDevicesInfoPane.setText("No MIDI devices avaliable.");
	}
	public void setMidiDeviceOptions(String[] devices) {
		midiDevicesInfoPane.setText("");
		deviceSelector.setModel(new DefaultComboBoxModel<>(devices));
		deviceSelector.repaint();
	}
	public void updateNoteTextField(String note) {
        SwingUtilities.invokeLater(() -> {
        	if (ignoreUnpitched && note.equals("UNPITCHED"))
        		return;
        	
        	if (playedNotes.size()>8)
        		playedNotes.remove(0);
        	playedNotes.add(note);
        	recentNotesPane.setText(playedNotes.toString());
        });
	}
	
	//SCARY UI STUFF ==============================================================
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI frame = new UI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public UI() {
		playedNotes = new ArrayList<>();
		
//		try {
//			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//		} catch (Exception e) {
//			System.out.println("look and feel didn't work. using default");
//		}
		URL iconURL = getClass().getResource("icon9.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());
		
		setResizable(false);
		setTitle("Instrument Controller");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 710, 410);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel inputLabel = new JLabel("Input type:");
		inputLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		inputLabel.setBounds(12, 8, 60, 16);
		contentPane.add(inputLabel);
		
		JComboBox<String> inputTypeDropdown = new JComboBox<String>(new String[]{"None","Line-In/Mic", "MIDI"});
		inputTypeDropdown.setBounds(78, 6, 100, 21);
		contentPane.add(inputTypeDropdown);
		
		JSeparator inputTypeSeparator = new JSeparator();
		inputTypeSeparator.setBounds(12, 32, 366, 4);
		contentPane.add(inputTypeSeparator);
		
		lineSettings = new JPanel();
		lineSettings.setBounds(6, 32, 372, 102);
		contentPane.add(lineSettings);
		lineSettings.setLayout(null);
		
		JLabel volumeLabel = new JLabel("Volume Sensitivity:");
		volumeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		volumeLabel.setBounds(6, 29, 127, 16);
		lineSettings.add(volumeLabel);
		
		JSlider volumeSlider = new JSlider();
		volumeSlider.setMinimum(1); // Set minimum value
		volumeSlider.setMaximum(200); // Set maximum value
		volumeSlider.setValue(100); // Set initial value
		volumeSlider.setBounds(134, 29, 172, 21);
		lineSettings.add(volumeSlider);
		
		JLabel pitchLabel = new JLabel("Pitch Sensitivity:");
		pitchLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
		pitchLabel.setBounds(6, 51, 127, 16);
		lineSettings.add(pitchLabel);
		
		JCheckBox unpitchedCheckBox = new JCheckBox("Ignore unpitched sounds");
		unpitchedCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
		unpitchedCheckBox.setBounds(6, 73, 167, 18);
		lineSettings.add(unpitchedCheckBox);
		
		JSlider pitchSlider = new JSlider();
		pitchSlider.setMinimum(0); // Set minimum value
		pitchSlider.setMaximum(80); // Set maximum value
		pitchSlider.setValue(20); // Set initial value
		pitchSlider.setBounds(134, 51, 172, 21);
		lineSettings.add(pitchSlider);
		
		JLabel lineSettingsLabel = new JLabel("Line/Mic Settings:");
		lineSettingsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		lineSettingsLabel.setBounds(6, 6, 116, 16);
		lineSettings.add(lineSettingsLabel);
		
		JSeparator lineSettingsSeparator = new JSeparator();
		lineSettingsSeparator.setBounds(6, 95, 366, 4);
		lineSettings.add(lineSettingsSeparator);
		
		//JCheckBox bitdepthCheckbox = new JCheckBox("Higher accuracy volume");
		//bitdepthCheckbox.setFont(new Font("Dialog", Font.PLAIN, 12));
		//bitdepthCheckbox.setBounds(177, 70, 172, 24);
		//lineSettings.add(bitdepthCheckbox);
		
		midiSettings = new JPanel();
		midiSettings.setLayout(null);
		midiSettings.setBounds(6, 32, 372, 102);
		contentPane.add(midiSettings);
		
		JLabel deviceSelectorLb = new JLabel("Device:");
		deviceSelectorLb.setFont(new Font("SansSerif", Font.PLAIN, 12));
		deviceSelectorLb.setBounds(6, 31, 107, 16);
		midiSettings.add(deviceSelectorLb);
		
		JLabel midiSettingsLabel = new JLabel("MIDI Settings:");
		midiSettingsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		midiSettingsLabel.setBounds(6, 6, 116, 16);
		midiSettings.add(midiSettingsLabel);
		
		JSeparator midiSettingsSeparator = new JSeparator();
		midiSettingsSeparator.setBounds(6, 95, 366, 4);
		midiSettings.add(midiSettingsSeparator);
		
		deviceSelector = new JComboBox<String>();
		deviceSelector.setBounds(53, 29, 146, 21);
		midiSettings.add(deviceSelector);
		
		midiDevicesInfoPane = new JTextPane();
		midiDevicesInfoPane.setOpaque(false);
		midiDevicesInfoPane.setForeground(Color.BLACK);
		midiDevicesInfoPane.setEditable(false);
		midiDevicesInfoPane.setBounds(3, 59, 311, 24);
		midiSettings.add(midiDevicesInfoPane);
		
		noSettings = new JPanel();
		noSettings.setLayout(null);
		noSettings.setBounds(6, 32, 372, 102);
		contentPane.add(noSettings);
		
		JLabel lblNoSettings = new JLabel("Select an input type to start processing audio.");
		lblNoSettings.setFont(new Font("SansSerif", Font.PLAIN, 12));
		lblNoSettings.setBounds(6, 0, 318, 32);
		noSettings.add(lblNoSettings);
		
		JSeparator noSettingsSeparator = new JSeparator();
		noSettingsSeparator.setBounds(6, 95, 366, 4);
		noSettings.add(noSettingsSeparator);
		
		JLabel keybindSetsLabel = new JLabel("Keybinds:");
		keybindSetsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		keybindSetsLabel.setBounds(13, 138, 100, 16);
		contentPane.add(keybindSetsLabel);
		
//		JTabbedPane kbPresetPane = new JTabbedPane(JTabbedPane.TOP);
//		kbPresetPane.setBounds(12, 157, 219, 148);
//		contentPane.add(kbPresetPane);
		
		JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setBounds(10, 157, 219, 148);
		contentPane.add(scrollPane1);
		//kbPresetPane.addTab("New tab", null, scrollPane1, null);
		
		kbList = new JList<String>();
    	kbList.setListData(Main.slHandler.getKeybindsAsArray()); //initialize keybind list
		scrollPane1.setViewportView(kbList);
		//kbPresetPane.setEnabledAt(0, true);
		
		JLabel recentNotesLabel = new JLabel("Recent Notes:");
		recentNotesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		recentNotesLabel.setBounds(239, 137, 100, 16);
		contentPane.add(recentNotesLabel);
		
		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setBounds(236, 157, 142, 148);
		contentPane.add(scrollPane2);
		
		recentNotesPane = new JTextPane();
		recentNotesPane.setBounds(243, 157, 135, 148);
		scrollPane2.setViewportView(recentNotesPane);
		//contentPane.add(recentNotesPane);
		recentNotesPane.setEditable(false);
		
		newKeybindBttn = new JButton("New Keybind");
		newKeybindBttn.setToolTipText("");
		newKeybindBttn.setBounds(12, 311, 110, 24);
		contentPane.add(newKeybindBttn);
		
		newKbInfoPane = new JTextPane();
		newKbInfoPane.setText("Play a note then press a key on your keyboard to set a keybind.");
		newKbInfoPane.setEditable(false);
		newKbInfoPane.setForeground(new Color(0, 0, 0));
		newKbInfoPane.setOpaque(false);
		newKbInfoPane.setBounds(12, 337, 366, 24);
		contentPane.add(newKbInfoPane);
		
		JButton deleteKbBtn = new JButton("Delete Selected Keybind");
		deleteKbBtn.setBounds(121, 311, 181, 24);
		contentPane.add(deleteKbBtn);
		
		JPanel mouseActionsPane = new JPanel();
		mouseActionsPane.setBounds(389, 8, 295, 351);
		contentPane.add(mouseActionsPane);
		mouseActionsPane.setLayout(null);
		
		JSeparator vertSeparator = new JSeparator();
		vertSeparator.setBounds(0, 0, 5, 351);
		vertSeparator.setOrientation(SwingConstants.VERTICAL);
		mouseActionsPane.add(vertSeparator);
		
		JButton mouseUpBttn = new JButton("Up");
		mouseUpBttn.setBounds(100, 73, 99, 24);
		mouseActionsPane.add(mouseUpBttn);
		
		JButton mouseLeftBttn = new JButton("Left");
		mouseLeftBttn.setBounds(17, 101, 99, 24);
		mouseActionsPane.add(mouseLeftBttn);
		
		JButton mouseRightBttn = new JButton("Right");
		mouseRightBttn.setBounds(173, 101, 99, 24);
		mouseActionsPane.add(mouseRightBttn);
		
		JButton mouseDownBttn = new JButton("Down");
		mouseDownBttn.setBounds(100, 129, 99, 24);
		mouseActionsPane.add(mouseDownBttn);
		
		JButton leftClickBttn = new JButton("Left Click");
		leftClickBttn.setBounds(17, 302, 99, 24);
		mouseActionsPane.add(leftClickBttn);
		
		JButton rightClickBttn = new JButton("Right Click");
		rightClickBttn.setBounds(184, 302, 99, 24);
		mouseActionsPane.add(rightClickBttn);
		
		JButton scrollDwnBttn = new JButton("Scroll Down");
		scrollDwnBttn.setBounds(157, 266, 110, 24);
		mouseActionsPane.add(scrollDwnBttn);
		
		JButton scrollUpBttn = new JButton("Scroll Up");
		scrollUpBttn.setBounds(35, 266, 110, 24);
		mouseActionsPane.add(scrollUpBttn);
		
		mouseXspeedSldr = new JSlider();
		mouseXspeedSldr.setMinimum(1); // Set minimum value
		mouseXspeedSldr.setMaximum(80); // Set maximum value
		mouseXspeedSldr.setValue(1); // Set initial value
		mouseXspeedSldr.setBounds(131, 172, 152, 16);
		mouseActionsPane.add(mouseXspeedSldr);
		
		mouseYspeedSldr = new JSlider();
		mouseYspeedSldr.setMinimum(1); // Set minimum value
		mouseYspeedSldr.setMaximum(80); // Set maximum value
		mouseYspeedSldr.setValue(1); // Set initial value
		mouseYspeedSldr.setBounds(131, 201, 152, 16);
		mouseActionsPane.add(mouseYspeedSldr);
		
		scrollAmountSldr = new JSlider();
		scrollAmountSldr.setMinimum(1); // Set minimum value
		scrollAmountSldr.setMaximum(20); // Set maximum value
		scrollAmountSldr.setValue(1); // Set initial value
		scrollAmountSldr.setBounds(131, 229, 152, 16);
		mouseActionsPane.add(scrollAmountSldr);
		
		xSpeedLbl = new JLabel("Mouse X Speed: ");
		xSpeedLbl.setFont(new Font("Dialog", Font.PLAIN, 12));
		xSpeedLbl.setBounds(16, 172, 134, 16);
		mouseActionsPane.add(xSpeedLbl);
		
		ySpeedLbl = new JLabel("Mouse Y Speed:");
		ySpeedLbl.setFont(new Font("Dialog", Font.PLAIN, 12));
		ySpeedLbl.setBounds(16, 201, 134, 16);
		mouseActionsPane.add(ySpeedLbl);
		
		scrollAmountLbl = new JLabel("Scroll Amount:");
		scrollAmountLbl.setFont(new Font("Dialog", Font.PLAIN, 12));
		scrollAmountLbl.setBounds(16, 229, 132, 16);
		mouseActionsPane.add(scrollAmountLbl);
		
		JLabel lblNewLabel_2 = new JLabel("Mouse:");
		lblNewLabel_2.setBounds(10, 0, 115, 16);
		mouseActionsPane.add(lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel("Click one of these buttons after playing a note");
		lblNewLabel_3.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblNewLabel_3.setBounds(10, 18, 271, 16);
		mouseActionsPane.add(lblNewLabel_3);
		
		JLabel lblNewLabel_4 = new JLabel(" to set a mouse \"keybind.\"");
		lblNewLabel_4.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblNewLabel_4.setBounds(8, 33, 142, 16);
		mouseActionsPane.add(lblNewLabel_4);
		
		JButton middleClickBttn = new JButton("Mid");
		middleClickBttn.setBounds(117, 301, 66, 26);
		mouseActionsPane.add(middleClickBttn);
		
		JSeparator horzSeparator = new JSeparator();
		horzSeparator.setBounds(6, 57, 266, 4);
		mouseActionsPane.add(horzSeparator);
		showInputControls("None");
		
		
		//Create listeners
		mouseLeftBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65502);
			}
		});
		mouseUpBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65500);
			}
		});
		mouseRightBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65503);
			}
		});
		mouseDownBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65501);
			}
		});
		scrollDwnBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65508);
			}
		});
		scrollUpBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65507);
			}
		});
		leftClickBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65504);
			}
		});
		middleClickBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65505);
			}
		});
		rightClickBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNewKeybind(65506);
			}
		});
		
		mouseXspeedSldr.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int speed = mouseXspeedSldr.getValue();
                xSpeedLbl.setText("Mouse X Speed: " + speed);
                Main.keyPresser.setXspeed(speed);
            }
        });
        
		mouseYspeedSldr.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int speed = mouseYspeedSldr.getValue();
                ySpeedLbl.setText("Mouse Y Speed: " + speed);
                Main.keyPresser.setYspeed(speed);
            }
        });
		
		scrollAmountSldr.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int amount = scrollAmountSldr.getValue();
                scrollAmountLbl.setText("Scroll Amount: " + amount);
                Main.keyPresser.setScrollAmount(amount);
            }
        });
		
		newKeybindBttn.addActionListener(new KeybindListener());
		
		deleteKbBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onDeleteKb();
			}
		});
		
		//bitdepthCheckbox.addItemListener(e -> {
		//	onBitdepthToggle(e.getStateChange() == ItemEvent.SELECTED);
		//	});
		
		unpitchedCheckBox.addItemListener(e -> {
			onUnpitchedToggle(e.getStateChange() == ItemEvent.SELECTED);
			});
		
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int volume = volumeSlider.getValue();
                volumeLabel.setText("Volume Sensitivity: " + volume);
                onVolumeChange(volume);
            }
        });
		        
        pitchSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double pitch = pitchSlider.getValue()/100.0;
                pitchLabel.setText("Pitch Sensitivity: " + pitch);
                onPitchChange(pitch);
            }
        });
		
		kbList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Check if the event is not in an intermediate state
                    JList<String> source = (JList<String>) e.getSource();
                    String selectedElement = source.getSelectedValue();
    				onListSelection(selectedElement);
                }
			}
		});
        
		inputTypeDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedInput = (String) e.getItem();
                onInputTypeSelected(selectedInput);
                }
            });
		
		deviceSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedInput = (String) e.getItem();
                onMidiDeviceSelected(selectedInput);
                }
            });
        
        //unused
//		kbPresetPane.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//		        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
//		        int index = sourceTabbedPane.getSelectedIndex();
//		        //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index));
//			}
//		});
	}
	
    private class KeybindListener implements ActionListener {
        private FocusListener focusListener;
        private KeyListener keyAdapter;
        @Override
        public void actionPerformed(ActionEvent e) {
        	newKeybindBttn.setText("Listening...");
        	newKeybindBttn.setEnabled(false);
        	newKbInfoPane.setText("Press a key to map the note, or click a mouse action to the right.");
        	focusListener = new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    //do nothing
                }

                @Override
                public void focusLost(FocusEvent e) {
                    // Handle focus lost event
                    System.out.println("key mapping has lost focus, aborting");
                    newKbInfoPane.setText("");
                	newKeybindBttn.setText("New Keybind");
                	newKeybindBttn.setEnabled(true);
					removeKeyListener(keyAdapter);
                    removeFocusListener(this);
                }
            };
            keyAdapter = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                	onNewKeybind(e.getKeyCode());
                	newKeybindBttn.setText("New Keybind");
                	newKeybindBttn.setEnabled(true);
                	removeFocusListener(focusListener);
                    removeKeyListener(this);
                }
            };
            addFocusListener(focusListener);
            addKeyListener(keyAdapter);
        	requestFocusInWindow(); // Set focus on the window
        }
    }
}
