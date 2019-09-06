
public class WordCounter implements Comparable<WordCounter>{

	private String word;
	private Integer count; 
	
	/**
	 * constructor
	 * @param word is the word from the text
	 * @param count is count for the word
	 */
	public WordCounter(String word, int count){
		this.word = word; 
		this.count = count; 
	}
	
	/**
	 * getter
	 */
	public String getWord(){
		return word;
	}
	
	/**
	 * getter for count
	 * @return
	 */
	public Integer getCount(){
		return count;
	}
	
	/**
	 * setter
	 */
	public void setWord(String word){
		this.word = word;
	}
	
	/**
	 * setter for count
	 * @param count
	 */
	public void setCount(int count){
		this.count = count;
	}
	
	/**
	 * compares the count of that to count of this
	 */
	public int compareTo(WordCounter that){
		return that.count.compareTo(this.count); 
	}
	
	
}
