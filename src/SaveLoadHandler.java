import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

public class SaveLoadHandler {

	private HashMap<String, Integer> keybinds;
	private File file = new File("keybinds.ser");
	
	public SaveLoadHandler() {
		loadKeybindsFromFile();
	}
	
	//GET SET ========================================================================================
	public HashMap<String, Integer> getKeybinds(){
		return keybinds;
	}
	
	public String[] getKeybindsAsArray() {
		String[] temp = new String[keybinds.size()+1];
		int i= 0;
		for(Entry<String,Integer> k :keybinds.entrySet()) {
			i++;
			if (k.getValue()<65500) //keybinds
				temp[i]=(k.getKey()+"="+KeyEvent.getKeyText(k.getValue()));
			else { //mousebinds
				temp[i]=(k.getKey()+"="+Main.keyPresser.getMouseActionName(k.getValue()));
			}
		}
		return temp;
	}
	
	public void setKeybind(int keycode, String note) {
		keybinds.put(note, keycode);
		saveKeybindsToFile();
	}
	
	public void removeKeybind(String note) {
		if (note == null)
			return;
		keybinds.remove(note);
		saveKeybindsToFile();
	}
	
	//SAVE LOAD =======================================================================================
	public void saveKeybindsToFile() {
		if (!file.exists()) {
		    try {
		    	file.createNewFile();
		        System.out.println("keybinds file created successfully.");
		    } catch (IOException e) {
		        System.out.println("An error occurred while creating the keybinds file.");
		        e.printStackTrace();
		    }
		}
		
		//write the hashmap to the file
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outputStream.writeObject(keybinds);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	public void loadKeybindsFromFile() {
		if (!file.exists()) {
		    System.out.println("creating keybinds file");
		    keybinds = new HashMap<String, Integer>();
		    saveKeybindsToFile();
		    return;
		}
		else {
			//load hashmap from file
	        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
	            keybinds = (HashMap<String, Integer>) inputStream.readObject();
	            if (keybinds == null) {
	            	keybinds = new HashMap<String, Integer>();
	            	System.out.println("loaded keybinds file, but it was empty");
	            }
	            else {
		            System.out.println("loaded keybinds from file");
	            }
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
		}
	}

}
