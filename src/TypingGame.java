import net.miginfocom.swing.MigLayout;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Random;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

public class TypingGame extends JFrame implements ActionListener{
	private static final int FRAME_HEIGHT = 600;
	private static final int FRAME_WIDTH = 800;
	
	private static final String EMPTY_STRING = "";
	
	private JFrame mainFrame = new JFrame();
	
	private List<JButton> titleScreen;
	private List<JButton> confirmButton;
	
	private JButton start, highscore, option, help, exit, ok, cancel, yes, no, back;
	private JLabel gameTitle, score, totalWords, word, timerS, separator, timerMS;
	private JLabel confirm1 = new JLabel("");
	private JLabel confirm2 = new JLabel("");
	private JPanel cards, titlePage, gamePage, highscorePage, optionPage, endPage, exitPage, confirmationPage;
	private CardLayout c;
	private JTextField enterName, enterWord;
	
	private int currentScore = 0;
	private int currentWord = 0;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
					TypingGame tg = new TypingGame();
					
					try {
						tg.loadWords();
						tg.readHighscore();
						
					} catch(FileNotFoundException e) {
						e.printStackTrace();
						
					}
					
					tg.setTitleScreenComponents();
					tg.setGUI();
					tg.start();
			}
		});
	}
	
	private void start() {
		mainFrame.setVisible(true);
		
	}
	
	/* ----------- HIGHSCORE ----------- */
	
	private List<Highscore> highscoreList = new ArrayList<Highscore>();
	private List<String> correctWords = new ArrayList<String>();
	private Highscore h;
	
	private String playerName, playerLongestWord;
	private int playerScore, playerTotalWords;
	
	private int totalPlayer;
	
	
	private void readHighscore() throws FileNotFoundException {
		
		try {
			File inFile = new File("highscore.dat");
			FileInputStream fis = new FileInputStream(inFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			
			totalPlayer = dis.read();
			
			if(totalPlayer > 0) {
				for(int i = 0; i < totalPlayer; i++) {
					playerName = dis.readUTF();
					playerScore = dis.read();
					playerTotalWords = dis.read();
					playerLongestWord = dis.readUTF();
					
					h = new Highscore(playerName, playerScore, playerTotalWords, playerLongestWord);
					highscoreList.add(h);
					
				}
			}
			
			dis.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeHighscore(String playerN, int playerS, int playerTW, String playerLW) {
		try {
			File outFile = new File("highscore.dat");
			FileOutputStream fos = new FileOutputStream(outFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(bos);
			
			h = new Highscore(playerN, playerS, playerTW, playerLW);
			highscoreList.add(h);
			
			totalPlayer = highscoreList.size();
			
			dos.write(totalPlayer);
			
			for(Highscore hh: highscoreList) {
				dos.writeUTF(hh.getName());
				dos.write(hh.getScore());
				dos.write(hh.getTotalWords());
				dos.writeUTF(hh.getLongestWord());
			}
			
			dos.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private List<JLabel> jLabelHighscoreList = new ArrayList<JLabel>();
	
	private void printTop10() {
		
	}
	
	
	/* ------------ WORDS DB -------------*/
	
	private int wordIndex = 0;
	private String theWord = "";
	
	private List<String> wordsList;
	
	private Random r = new Random();
	
	private void loadWords() throws FileNotFoundException {
		wordsList = new ArrayList<String>();
		
		File inFile = new File("list.dat");
		Scanner read = new Scanner(inFile);
		
		while(read.hasNext()) wordsList.add(read.next());
		
		read.close();
		
	}
	
	/* ------------- TIMER --------------- */
	
	private volatile boolean running = true;
	private volatile boolean paused = false;
	private final Object pauseLock = new Object();
	
	private boolean check = true;
	private boolean sCheck = true;
	private int s = 4;
	private int ms = 60;
	private int counter = 3;
	
	private Thread sT = new Thread(new Runnable() {
		public void run() {
			while(running) {
				synchronized(pauseLock) {
					if(!running) break;
					if(paused) {
						try {
							pauseLock.wait();
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
						
						if(!running) break;
					}
				}
				
				for(counter = 3; counter >= 1; counter--) {
					word.setText(counter + "");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					
					if(counter >= 0) {
						wordIndex = r.nextInt(58110);
						theWord = wordsList.get(wordIndex);
						word.setText(theWord);
					}
				}
				
				for(s = 4; s >= 0; s--) {
					timerS.setText("0" + s);
					
					for(ms = 60; ms >= 0; ms--) {
						if(ms<10) timerMS.setText("0"+ ms);
						else timerMS.setText(ms + "");
						
						try {
							Thread.sleep(20);
						} catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				pause();
				
				c.show(cards, "end");
				System.out.println(theWord);
				theWord = null;
				word.setText("Type 'START' to play");
				timerS.setText("05");
				timerMS.setText("00");
				
			}
		}
		
	});
	
	private void pause() {
		paused = true;
	}
	
	private void resume() {
		synchronized(pauseLock) {
			paused = false;
			pauseLock.notifyAll();
		}
	}
	
	private void executeTimer() {
		timerS.setText("05");
		timerMS.setText("00");
		
		if(check) {
			sT.start();
		}
			
		check = false;
		resume();
	}
	
	/* ---------- SETTING GUI ------------ */
	
	private void setTitleScreenComponents() {
		gameTitle = new JLabel("Typing Game");
		gameTitle.setFont(new Font("KBLuckyClover", Font.PLAIN, 100));
		
		start = new JButton("Start");
		highscore = new JButton("Highscore");
		option = new JButton("Option");
		help = new JButton("Help");
		exit = new JButton("Exit");
		
		titleScreen = new ArrayList<JButton>();
		titleScreen.add(start);
		titleScreen.add(highscore);
		titleScreen.add(option);
		titleScreen.add(help);
		titleScreen.add(exit);
		
		
		
	}
	
	private void setGUI() {
		mainFrame.setTitle("Typing Game");
		mainFrame.setSize(FRAME_WIDTH,FRAME_HEIGHT);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setResizable(false);
		
		Color pastelPink = new Color(244,175,161);
		Color alabaster = new Color(237,245,230);
		Color lightMossGreen = new Color(190,219,164);
		Color mayGreen = new Color(95,145,74);
		Color teaRose = new Color(248,204,195);
		Color seashells = new Color(255,245,239);
		Font kbPlanetEarth = new Font("KBPlanetEarth", Font.PLAIN, 50);
		
		Container cp = mainFrame.getContentPane();
		
		titlePage = new JPanel(new MigLayout("align center","[center]","70[]60[][][][][]"));
		titlePage.setBackground(alabaster);
		
		gameTitle.setForeground(pastelPink);
		titlePage.add(gameTitle, "cell 1 0 3 1");

		for(JButton b: titleScreen) {
			b.setOpaque(false);
			b.setContentAreaFilled(false);
			b.setBorderPainted(false);
			b.setFocusPainted(false);
			
			b.setFont(kbPlanetEarth);
			b.setForeground(lightMossGreen);
			
			b.addMouseListener(new MouseAdapter(){
				public void mouseEntered(MouseEvent event) { b.setForeground(mayGreen);}
				public void mouseExited(MouseEvent event) { b.setForeground(lightMossGreen);}
				public void mousePressed(MouseEvent event) { b.setForeground(teaRose);}
				public void mouseReleased(MouseEvent event) { b.setForeground(mayGreen);}
			});
			
			b.addActionListener(this);
			
			if(b.getText().equals("Start")) titlePage.add(b, "cell 1 1");
			if(b.getText().equals("Highscore")) titlePage.add(b, "cell 1 2");
			if(b.getText().equals("Option")) titlePage.add(b, "cell 1 3");
			if(b.getText().equals("Exit")) titlePage.add(b, "cell 1 4");
			
		}
		
		/* -------- GAME PAGE -------- */
		
		Font sansSerif = new Font("SansSerif", Font.BOLD, 50);
		
		gamePage = new JPanel(new MigLayout("align center,filly", "[center]", "20[]20[]60[]60[]20[]"));
		gamePage.setBackground(alabaster);
		
		JPanel info = new JPanel(new MigLayout("fillx", "[center]270[center]", "[][]"));
		info.setBackground(alabaster);
		
		score = new JLabel(currentScore + "");
		word = new JLabel("Type 'START' to play");
		totalWords = new JLabel(currentWord + "");
		
		score.setFont(sansSerif);
		word.setFont(kbPlanetEarth);
		totalWords.setFont(sansSerif);
		
		score.setForeground(pastelPink);
		word.setForeground(lightMossGreen);
		totalWords.setForeground(pastelPink);
		
		JLabel labelScore = new JLabel("Score");
		JLabel labelTotalWords = new JLabel("Words");
		
		labelScore.setForeground(teaRose);
		labelScore.setFont(kbPlanetEarth);
		labelTotalWords.setForeground(teaRose);
		labelTotalWords.setFont(kbPlanetEarth);
		
		info.add(labelScore, "cell 0 0");
		info.add(score, "cell 0 1");
		info.add(totalWords, "cell 1 1");
		info.add(labelTotalWords, "cell 1 0");
		
		enterWord = new JTextField(15);
		
		enterWord.setFont(kbPlanetEarth);
		enterWord.setHorizontalAlignment(JTextField.CENTER);
		enterWord.setBackground(seashells);
		enterWord.setForeground(mayGreen);
		enterWord.setBorder(BorderFactory.createLineBorder(pastelPink));
		enterWord.setCaretColor(mayGreen);
		enterWord.addActionListener(this);
		
		timerS = new JLabel("05");
		separator = new JLabel(" : ");
		timerMS =new JLabel("00");
		
		timerS.setFont(sansSerif);
		timerS.setForeground(pastelPink);
		separator.setFont(sansSerif);
		separator.setForeground(pastelPink);
		timerMS.setFont(sansSerif);
		timerMS.setForeground(pastelPink);
		
		gamePage.add(info, "cell 0 0, span");
		gamePage.add(word, "span, align center");
		gamePage.add(enterWord, "cell 0 2 3 1, grow");
		
		gamePage.add(timerS, "cell 0 3");
		gamePage.add(separator, "cell 0 3");
		gamePage.add(timerMS, "cell 0 3");
		
		/* ------- END PAGE -------- */
		
		endPage = new JPanel(new MigLayout("align center, filly", "[center]", "60[]-20[]50[]50[][][]"));
		endPage.setBackground(alabaster);
		
		JLabel end1 = new JLabel("You ran out of time!");
		JLabel end2 = new JLabel("Enter name to save highscore");
		
		end1.setFont(kbPlanetEarth);
		end1.setForeground(mayGreen);
		end2.setFont(kbPlanetEarth);
		end2.setForeground(lightMossGreen);
		
		enterName = new JTextField(15);
		
		enterName.setFont(kbPlanetEarth);
		enterName.setHorizontalAlignment(JTextField.CENTER);
		enterName.setBackground(seashells);
		enterName.setForeground(mayGreen);
		enterName.setBorder(BorderFactory.createLineBorder(pastelPink));
		enterName.setCaretColor(mayGreen);
		enterName.addActionListener(this);
		
		endPage.add(end1, "cell 0 0");
		endPage.add(end2, "cell 0 1");
		endPage.add(enterName, "cell 0 2, grow");
		
		ok = new JButton("OK");
		cancel = new JButton("CANCEL");
		
		List<JButton> endButton = new ArrayList<JButton>();
		endButton.add(ok);
		endButton.add(cancel);
		
		for(JButton b: endButton) {
			b.setFont(kbPlanetEarth);
			b.setForeground(teaRose);
			
			b.setBorderPainted(false);
			b.setOpaque(false);
			b.setFocusPainted(false);
			b.setContentAreaFilled(false);
			
			b.addMouseListener(new MouseAdapter(){
				public void mouseEntered(MouseEvent event) { b.setForeground(pastelPink);}
				public void mouseExited(MouseEvent event) { b.setForeground(teaRose);}
				public void mousePressed(MouseEvent event) { b.setForeground(mayGreen);}
				public void mouseReleased(MouseEvent event) { b.setForeground(pastelPink);}
			});
			
			if(b.getText().equals("OK")) endPage.add(b, "cell 0 3");
			if(b.getText().equals("CANCEL")) endPage.add(b, "cell 0 3");
			
			b.addActionListener(this);
			
		}
		
		/* ------ CONFIRMATION PAGE ------ */
		
		confirmationPage = new JPanel(new MigLayout("align center", "[center]", "100[][]100[]"));
		confirmationPage.setBackground(alabaster);
		
		confirm1.setForeground(mayGreen);
		confirm1.setFont(kbPlanetEarth);
		confirm2.setForeground(lightMossGreen);
		confirm2.setFont(kbPlanetEarth);
		
		confirmationPage.add(confirm1, "cell 0 0");
		confirmationPage.add(confirm2, "cell 0 1");
		
		yes = new JButton("YES");
		no = new JButton("NO");
		
		confirmButton = new ArrayList<JButton>();
		confirmButton.add(yes);
		confirmButton.add(no);
		
		for(JButton b: confirmButton) {
			b.setFont(kbPlanetEarth);
			b.setForeground(teaRose);
			
			b.setBorderPainted(false);
			b.setOpaque(false);
			b.setFocusPainted(false);
			b.setContentAreaFilled(false);
			
			b.addMouseListener(new MouseAdapter(){
				public void mouseEntered(MouseEvent event) { b.setForeground(pastelPink);}
				public void mouseExited(MouseEvent event) { b.setForeground(teaRose);}
				public void mousePressed(MouseEvent event) { b.setForeground(mayGreen);}
				public void mouseReleased(MouseEvent event) { b.setForeground(pastelPink);}
			});
			
			if(b.getText().equals("YES")) confirmationPage.add(b, "cell 0 2");
			if(b.getText().equals("NO")) confirmationPage.add(b, "cell 0 2");
			
			b.addActionListener(this);
			
		}
		
		/* ---------- HIGHSCORE PAGE ----------- */
		
		highscorePage = new JPanel(new MigLayout("fillx", "[center]", "[][]"));
		highscorePage.setBackground(alabaster);
		
		/* JTABLE IMPLEMENTATION
		
		String column[] = {"Name","Score","Total Words", "Longest Word"};
		
		DefaultTableModel tableModel = new DefaultTableModel(column, 0);
		JTable table = new JTable(tableModel);
		
		for(Highscore hh: highscoreList) {
			String n = hh.getName();
			int s = hh.getScore();
			int tw = hh.getTotalWords();
			String lw = hh.getLongestWord();
			
			Object[] data = {n, s, tw, lw};
			
			tableModel.addRow(data);
			
		}
		
		table.setShowGrid(false);
		table.setFont(kbPlanetEarth);
		
		table.getTableHeader().setFont(kbPlanetEarth);
		
		JScrollPane sp = new JScrollPane(table);
		
		highscorePage.add(sp, "cell 0 0");
		
		*/
		
		back = new JButton("BACK");
		
		back.setFont(kbPlanetEarth);
		back.setForeground(teaRose);
		
		back.setBorderPainted(false);
		back.setOpaque(false);
		back.setFocusPainted(false);
		back.setContentAreaFilled(false);
		back.addActionListener(this);
		
		back.addMouseListener(new MouseAdapter(){
			public void mouseEntered(MouseEvent event) { back.setForeground(pastelPink);}
			public void mouseExited(MouseEvent event) { back.setForeground(teaRose);}
			public void mousePressed(MouseEvent event) { back.setForeground(mayGreen);}
			public void mouseReleased(MouseEvent event) { back.setForeground(pastelPink);}
		});
		
		highscorePage.add(back, "cell 0 1");
		
		
		/* ------ ADDING CARDS ------ */
		
		cards = new JPanel(new CardLayout());
		
		cards.add(titlePage, "title");
		cards.add(gamePage, "start");
		cards.add(endPage, "end");
		cards.add(confirmationPage, "confirm");
		cards.add(highscorePage, "highscore");
		
		cp.add(cards);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JTextField) {
			String typedWord = enterWord.getText();
			if(typedWord.equals("START")) {
				executeTimer();
				
				currentScore = Integer.parseInt(score.getText());
				currentWord = Integer.parseInt(totalWords.getText());
				
			} if(typedWord.equals(theWord)) {
				correctWords.add(theWord);
				int add = theWord.length();
				
				currentScore = Integer.parseInt(score.getText());
				
				currentWord++;
				currentScore = add + currentScore;
				
				score.setText(currentScore + "");
				totalWords.setText(currentWord + "");
				
				timerS.setText("04");
				
				s = 4;
				ms = 60;
				
				wordIndex = r.nextInt(58110);
				theWord = wordsList.get(wordIndex);
				word.setText(theWord);
			}
			
			enterWord.setText(EMPTY_STRING);
			
		} else if(e.getSource() instanceof JButton) {
			JButton click = (JButton)e.getSource();
			c = (CardLayout)(cards.getLayout());
			
			if(click.getText().equals("Start")) c.show(cards, "start");
			else if(click.getText().equals("Highscore")) c.show(cards,"highscore"); 
			else if(click.getText().equals("Option")) c.show(cards, "option");
			else if(click.getText().equals("Exit")) {
				confirm1.setText("Do you really want to quit?");
				confirm2.setText("");
				
				for(JButton b: confirmButton) {
					if(b.getText().equals("NO")) b.setText("<html><center>NO TAKE<br>ME BACK</center></html>");
					else if(b.getText().equals("YES")) b.setText("<html><center>YES<br>PLEASE</center></html>");
				}
				
				c.show(cards, "confirm");
				
			}
			
			else if(click.getText().equals("<html><center>NO TAKE<br>ME BACK</center></html>")) {
				c.show(cards, "title");
				
				for(JButton b: confirmButton) {
					if(b.getText().equals("<html><center>NO TAKE<br>ME BACK</center></html>")) b.setText("NO");
					else if(b.getText().equals("<html><center>YES<br>PLEASE</center></html>")) b.setText("YES");
				}
			}
			
			else if(click.getText().equals("<html><center>YES<br>PLEASE</center></html>")) System.exit(0);
			
			else if(click.getText().equals("OK")) {
				playerName = enterName.getText();
				playerScore = Integer.parseInt(score.getText());
				playerTotalWords = correctWords.size();
				
				Comparator<String> stringLengthComparator = new Comparator<String>() {
					public int compare(String s1, String s2) {
						return Integer.compare(s1.length(), s2.length());
						
					}
					
				};
				
				Collections.sort(correctWords, stringLengthComparator);
				
				playerLongestWord = correctWords.get(correctWords.size() - 1);
				
				writeHighscore(playerName, playerScore, playerTotalWords, playerLongestWord);
				
				correctWords.clear();
				
				confirm1.setText("HIGHSCORE SAVED");
				confirm2.setText("Do you want to play again?");
				
				c.show(cards, "confirm");
				
			} else if(click.getText().equals("CANCEL")) {
				confirm1.setText("oh well");
				confirm2.setText("Do you want to play again?");
				
				c.show(cards, "confirm");
			} 
			
			else if(click.getText().equals("YES")) {
				c.show(cards, "start");
				score.setText("0");
				totalWords.setText("0");
			}
			
			else if(click.getText().equals("NO")) {
				c.show(cards, "title");
				enterWord.setText(EMPTY_STRING);
				score.setText("0");
				totalWords.setText("0");
				
				
			} else if(click.getText().equals("BACK")) c.show(cards, "title");
		}
		
	}
	
}
