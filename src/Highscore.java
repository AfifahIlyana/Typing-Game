public class Highscore {
	
	private String name;
	private int score;
	private int totalWords;
	private String longestWord;
	
	public Highscore() {
		name = "";
		score = 0;
		totalWords = 0;
		longestWord = "";
	}
	
	public Highscore(String n, int s, int tW, String lW) {
		name = n;
		score = s;
		totalWords = tW;
		longestWord = lW;
		
	}
	
	public String getName() {
		return name;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getTotalWords() {
		return totalWords;
	}
	
	public String getLongestWord() {
		return longestWord;
	}
}
