
public class CharacterCounter implements Comparable<CharacterCounter>{

	private Character c;
	private Integer count; 
	
	/**
	 * constructor
	 * @param c
	 * @param count
	 */
	public CharacterCounter(char c, int count){
		this.c = c; 
		this.count = count; 
	}
	
	/**
	 * getter for char
	 */
	public Character getC(){
		return c;
	}
	
	/**
	 * getter for count
	 * @return
	 */
	public Integer getCount(){
		return count;
	}
	
	/**
	 * setter for char
	 */
	public void setC(char c){
		this.c = c;
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
	public int compareTo(CharacterCounter that){
		return that.count.compareTo(this.count); 
	}
	
}
