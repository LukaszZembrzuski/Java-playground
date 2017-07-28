package wm.projects.midi;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.sound.midi.*;
import java.util.ArrayList;
import java.io.*;

public class DrumSequenceCreator {
/*in short, sets up GUI with grid layout instruments and checkboxes
 * checkboxes added to array list and gui
 * then, looping for each instrument, a track is created for each time instance (checking if the checkbox was selected)
 * thi strack is created by supplying array of 0-quiet or drumNumber-checkbox selected to track making method
 */
	JPanel mainPanel;
	JFrame frame;
	JFrame aboutFrame;
	Sequencer sequencer;
	Sequence seq;
	Track track;
	int[] differentDrums = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	ArrayList<JCheckBox> checkBoxList = new ArrayList<JCheckBox>();
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snara", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle (annoying)", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
	
	
	public void setupGUI(){
		frame = new JFrame("Drum Sequence Creator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		JPanel background = new JPanel();
		background.setLayout(new BorderLayout());
		background.setBackground(Color.lightGray);
		background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));	//new
		
		//menu
		JMenuBar menu = new JMenuBar();
		JMenu saveMenu = new JMenu("Save sequence");
		JMenu openMenu = new JMenu("Open sequence");
		JMenu aboutMenu = new JMenu("About/Instructions");
		JMenu exitMenu = new JMenu("Exit");
		JMenuItem saveMenuItem = new JMenuItem("Save as...");
		saveMenuItem.addActionListener(new SaveMenuItemListener());
		JMenuItem openMenuItem = new JMenuItem("Open...");
		openMenuItem.addActionListener(new OpenMenuItemListener());		
		JMenuItem aboutMenuItem = new JMenuItem("About/Instructions");
		aboutMenuItem.addActionListener(new AboutMenuItemListener());
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ExitMenuItemListener());
		saveMenu.add(saveMenuItem);
		openMenu.add(openMenuItem);
		aboutMenu.add(aboutMenuItem);
		exitMenu.add(exitMenuItem);
		menu.add(saveMenu);
		menu.add(openMenu);
		menu.add(aboutMenu);
		menu.add(exitMenu);
		frame.setJMenuBar(menu);
		
		//create instrument names
		JPanel names = new JPanel(new GridLayout(16,1));
		
		for(int i = 0; i < 16; i++){
			JLabel l = new JLabel();
			l.setText(instrumentNames[i]);
			names.add(l);
		}

		
		JPanel buttonPanel = new JPanel();
		
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new StartButtonListener());
		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(new StopButtonListener());
		JButton clearButton = new JButton("Clear all");
		clearButton.addActionListener(new ClearButtonListener());
		JButton fasterButton = new JButton("Faster");
		fasterButton.addActionListener(new FasterButtonListener());
		JButton slowerButton = new JButton("Slower");
		slowerButton.addActionListener(new SlowerButtonListener());
		
		buttonPanel.add(startButton);
		buttonPanel.add(fasterButton);
		buttonPanel.add(slowerButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(stopButton);		
		
		background.add(BorderLayout.WEST, names);
		background.add(BorderLayout.SOUTH, buttonPanel);
		
		GridLayout grid = new GridLayout(16,16);	//new
		grid.setVgap(1);
		grid.setHgap(1);
		mainPanel = new JPanel(grid);
		
		for(int i = 0; i < 256; i++){
			JCheckBox c = new  JCheckBox();
			c.setSelected(false);
			mainPanel.add(c);
			checkBoxList.add(c);		//list used for seeing which notes were selected (only references to checkBox objects here)
		}
		
		background.add(BorderLayout.CENTER, mainPanel);
		frame.getContentPane().add(BorderLayout.CENTER, background);
		
		frame.pack();
		frame.setBounds(50,50,500,500);
		frame.setVisible(true);
		
		setupMIDI();
	}//setupGUI
	
	
	
	
	
	
	
	

	
	public void setupMIDI(){
		try{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			seq = new Sequence(Sequence.PPQ,4);
			track = seq.createTrack();
			sequencer.setTempoInBPM(120);
		}catch(MidiUnavailableException mue){
			System.out.println("Midi unavaliable exception");
		}catch(InvalidMidiDataException imde){
			System.out.println("InvalidMidiDataException");
		}
	}
	
	public MidiEvent createMidiEvent(int command, int instrument, int key, int tone, int time){
		try{
			ShortMessage sm = new ShortMessage(command, instrument, key, tone);
			MidiEvent event = new MidiEvent(sm, time);
			return event;
		}catch(InvalidMidiDataException imde){
			System.out.println("InvalidMidiDataException");
			return null;
		}
	}
	
	public void createTrack(int[] list){//fills track for each instrument using createMidiEvent()
		for(int i = 0; i < 16; i++){
			if(list[i] != 0){
				track.add(createMidiEvent(144,9,list[i],100,i));//144-note on, 9-drums as instrument, list[i] - which drum in particular, 100 - how loud, i - time
				track.add(createMidiEvent(128,9,list[i],100,i+1));//128 - note off
			}
		}
	}
	
	public void play(){
		seq.deleteTrack(track);
		track = seq.createTrack();
		
		int[] trackNotes; //array with events indicating which notes are playedthat will be used to create track /////has to be initialised
		
		for(int i = 0; i < 16; i++){	//loop for rows (instruments)
			int k = differentDrums[i];
			trackNotes = new int[16];
			
			for(int j = 0; j < 16; j++){	//loop for columns
				JCheckBox c = checkBoxList.get(j + (16*i));
				if(c.isSelected()){
					trackNotes[j] = k;
				}else{
					trackNotes[j] = 0;
				}
			}	
			createTrack(trackNotes);
		}
		track.add(createMidiEvent(192,9,1,0,15)); //apparently we need to make sure there is some beat at the end
		
		try{
			sequencer.setSequence(seq);
			sequencer.setTempoInBPM(120);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
		}catch(InvalidMidiDataException imde){
			System.out.println("Invalid Midi Data Exception");
		}
	}

	//listener inner classes
	
	//saves sequence as boolean array
	class SaveMenuItemListener implements ActionListener{
		
		public void actionPerformed(ActionEvent a){
			boolean[] sequenceToSave = new boolean[256];
			for(JCheckBox b:checkBoxList){
				int i = checkBoxList.indexOf(b);
				if(b.isSelected()){
					sequenceToSave[i]=true;
				}else{
					sequenceToSave[i]=false;
				}
			}
		
			JFileChooser file = new JFileChooser();
			file.showSaveDialog(frame);
			
			try{
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file.getSelectedFile()));
				os.writeObject(sequenceToSave);
				os.close();
			}catch(IOException ioe){
				System.out.println("IOException");
			}
		}	
	}
	
	//opens a sequence
	class OpenMenuItemListener implements ActionListener{

		public void actionPerformed(ActionEvent a){
			
			JFileChooser file = new JFileChooser();
			file.showOpenDialog(frame);
			boolean[] loadedSequence = null;
			
			try{
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(file.getSelectedFile()));
				try{
					loadedSequence = (boolean[]) is.readObject();
				}catch(ClassNotFoundException ce){
					System.out.println("This shouldn't have happened");
				}
			
				is.close();
				
			}catch(IOException ieo){
				System.out.println("IOException");
			}
			
			
			for(int i = 0; i < loadedSequence.length; i++){		//array.length as in variable, not a method
				JCheckBox c = checkBoxList.get(i);
				//System.out.println(loadedSequence[i]);
				if(loadedSequence[i]==true){
					c.setSelected(true);
				}else{
					c.setSelected(false);
				}

			}
			
			sequencer.stop();
			play();
		}
	}
	
	class AboutMenuItemListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			aboutFrame = new JFrame("About");
			aboutFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			JPanel aboutPanel = new JPanel();
			aboutPanel.setBackground(Color.white);
			
			JTextArea aboutArea = new JTextArea(12,30);
			aboutArea.setEditable(false);
			aboutArea.setLineWrap(true);
			aboutArea.setWrapStyleWord(true);
			
			aboutArea.setText("\nInstructions:\nJust pick which drum/instrument (vertical axis with names) should play at what time (horizontal axis). After creating a sequence, hit the Start button. Sequence can be saved and loaded.\n-----   -----   -----   -----\nBased on program from Java Head First book.\nMichał Wiśniewski");
			
			JButton aboutCloseButton = new JButton("Close");
			aboutCloseButton.addActionListener(new AboutCloseButtonListener());
			
			aboutPanel.add(aboutArea);
			aboutPanel.add(aboutCloseButton);
			aboutFrame.getContentPane().add(BorderLayout.CENTER, aboutPanel);
			
			aboutFrame.setVisible(true);
			aboutFrame.setBounds(200,220,380,290);
		}
	}
	
	class ExitMenuItemListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			System.exit(0);
		}
	}

	class StartButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			play();
		}
	}
	
	class StopButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			sequencer.stop();
		}
	}
	
	class ClearButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			for(int i = 0; i < 256; i++){
				checkBoxList.get(i).setSelected(false);
			}
		}
	}
	
	class FasterButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			float tempo = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempo*1.03));
		}
	}
	
	class SlowerButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			float tempo = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempo*0.97));
		}
	}
	
	class AboutCloseButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent a){
			aboutFrame.dispose();
		}
	}
}
