import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class ALTextAnalyzer {
	
	private String filename; 
	private String charSet; 
	private char quotationMark; 
	private Set<String> stopWordSet; 
	private enum LAST_CHAR {
        QUOTATION_MARK, COMMA, PERIOD_QUESTION_EXCLAMATION, ALPHABETIC_CHARACTER
    };
	
	public static void main(String[] args) {
		//TODO parameterize the name and character set and quotation mark
		String nameOfFile = "pride-prejudice.txt";
		//String nameOfFile = "christmas-carol.txt";
		//String nameOfFile = "test1.txt";
		//String nameOfFile = "alice-in-wonderland.txt";
		//String nameOfFile = "test1.txt";
		//String nameOfFile = "les-mis.txt";
		//String nameOfFile = "huck-finn.txt";
		String charSet = "ASCII";
		//char quotationMark = '\'';
		char quotationMark = '"';
		//String charSet = "ISO-8859-1";
		
		System.out.println("top 10 letter frequencies:");
		//create TextAnalyzer object called text 
		ALTextAnalyzer text = new ALTextAnalyzer(nameOfFile, charSet, quotationMark);
		//Q1: call method to get character frequencies 
		List <CharacterCounter> charfreqs = text.getCharacterFrequencies();	
		//print the top 10 most frequent characters
		//text.printMostFreqCharacters(charfreqs, 10);

		
		//Q2: word frequency
		System.out.println("\ntop 10 word frequencies:");
		//call method to get word frequencies, takes false as param b/c not using stop list 
		List<WordCounter> wordFreqs = text.getWordFrequencies(false);
		//print the top 10 most frequent words
		//text.printMostFreqWords(wordFreqs, 10, true);
		
		//Q3: word frequency with stop list 
		System.out.println("\ntop 10 word frequencies with Stop List:");
		//call method to get word frequencies, takes true as param b/c using stop list
		List<WordCounter> wordFreqsWithStopList = text.getWordFrequencies(true);
		//print result
		//text.printMostFreqWords(wordFreqsWithStopList, 10, true);
		
		//Q4: top 10 longest quotations
		System.out.println("\ntop 10 longest quotations:");
		//call method to get the quotations
		List<WordCounter> quotationsAndLength = text.getQuotationsAndLength();
		//print result
		//text.printMostFreqWords(quotationsAndLength, 10, true);
	
		//Q4: top 10 shortest quotations
		//System.out.println("\ntop 10 shortest quotations:");
		//print result
		//text.printMostFreqWords(quotationsAndLength, 10, false);
		
		//Q5 wild card: print any words that are greater than or equal to 3 chars long 
		//and are palindromes 
		System.out.println("\nWild Card - Palindromes:");
		text.checkIfPallindrome();

	}
	
	

	/**
	 * constructor
	 * @param filename
	 */
	public ALTextAnalyzer(String filename, String charSet, char quotationMark){
		this.filename = filename; 
		this.charSet = charSet;	
		this.stopWordSet = getStopListWords();	
		this.quotationMark = quotationMark;
	}
	
	
	/**
	 * read the file, get the letter and the count and sort them
	 * @return
	 */
	public List<CharacterCounter> getCharacterFrequencies(){
		
		//declare ArrayList, *get time to create new object
		long startTime = System.nanoTime();
		List<CharacterCounter> characterFreq = new ArrayList<>();
		long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new arraylist object: " + createObjTime + " ns");
		
		//to get timings 
		long timeToExist = 0; 
		long timeToAdd = 0; 
		long timeToUpdate = 0; 
		
		//open a file
		InputStreamReader input = null;
		try{

			input = new InputStreamReader(new FileInputStream(new File(this.filename)));
			
			//read a file byte by byte 
			while(input.ready()){
				Character c = new Character((char) input.read());
				//ignore non-alphabetic characters 
				if (!c.isAlphabetic(c)){
					continue;
				} 
				//ignore case for each character 
				c = Character.toUpperCase(c);

				CharacterCounter charCounter = null; 
				//loop through AL
				//*time for checking if element exists in list
				long startTime1 = System.nanoTime();
				for (CharacterCounter cc : characterFreq){ 
					if (c == cc.getC()){
						timeToExist = System.nanoTime() - startTime1; 
		
						//char exists in list, put that char into charCounter variable 
						charCounter = cc; 
						break; 
					}					
				}
				//if variable is empty
				if (charCounter == null){
					//haven't found the char in the list 
					charCounter = new CharacterCounter(c, 0); 
				}
				//*updating existing element
				long startTime2 = System.nanoTime();
				int countOfCharacter = charCounter.getCount(); //un-boxes
				countOfCharacter++;  //get the count and increase it 
				charCounter.setCount(countOfCharacter);
				 timeToUpdate = System.nanoTime() - startTime2; 
	
				//after incrementing if count is 1, it means we have a new char 
				if (charCounter.getCount() == 1){
					//new character 
					//*get time to add new element 
					long startTime3 = System.nanoTime();
					characterFreq.add(charCounter); 
					 timeToAdd = System.nanoTime() - startTime3; 
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		  //print
		  System.out.println("to add new element: " + timeToAdd + " ns");
		  System.out.println("to check if element exists: " + timeToExist + " ns");
		  System.out.println("to update element: " + timeToUpdate + " ns");
		  
		  //overall time to get top 10 letters
		  double getFreqTime = createObjTime + timeToAdd + timeToExist + timeToUpdate; 
		  System.out.println("to get top 10: " + getFreqTime + " ns");

	
		//return the list 
		return characterFreq;
	}
	
	
	/**
	 * print the top 10 most frequent letters
	 * @param sortedFreq
	 * @param max
	 */
	
	public void printMostFreqCharacters(List<CharacterCounter> sortedFreq, int max){
		//sort the list in natural sorted order 
		Collections.sort(sortedFreq);
		for(int i = 0; i<max; i++){
			CharacterCounter cc = sortedFreq.get(i);
			System.out.printf("%c : %,d\n", cc.getC(), cc.getCount());
		}
	}
	
	////////////////////////////Q2 and Q3: word frequency ///////////////////////////////////////////
	/**
	 * read file, get the word and count and sort it 
	 * @return
	 */
	public List<WordCounter> getWordFrequencies(boolean useStopList){
		//declare map
		//*get time to create new object
		long startTime = System.nanoTime();
		List<WordCounter> wordFreq = new ArrayList<>();
		long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new arraylist object: " + createObjTime + " ns");
	
		//to get timings 
		long timeToExist = 0; 
		long timeToAdd = 0; 
		long timeToUpdate = 0; 
		
		Scanner input = null;
		
		try{		
			input = new Scanner(new File(filename), charSet);
			
			while (input.hasNext()){
				String word = input.next();
				//change all words to the same case 
				word = word.toUpperCase();
				//call method to ignore any non-alphabetic characters 
				word = stripNonAlphaCharacters(word);
				//if useStopList = true, then skip the word if it appears in the stop list 
				if (useStopList){
					if(this.stopWordSet.contains(word)){
						continue;    //goes to top of while loop
					}
				}
				
				WordCounter wCounter = null; 
				//loop through AL
				//*time for checking if element exists in list
				long startTime1 = System.nanoTime();
				for (WordCounter wc : wordFreq){     
					//word exists in list 
					if (word.equals(wc.getWord())){
						timeToExist = System.nanoTime() - startTime1; 
						wCounter = wc; 
						break; 
					}					
				}
				if (wCounter == null){
					//haven't found the word in the list 
					wCounter = new WordCounter(word, 0); 
				}
				//*updating existing element
				long startTime2 = System.nanoTime();
				int countOfWord = wCounter.getCount(); //un-boxes
				countOfWord++;  //get the count and increase it 
				wCounter.setCount(countOfWord);
				timeToUpdate = System.nanoTime() - startTime2; 	
				if (wCounter.getCount() == 1){
					//new character 
					//*get time to add new element 
					long startTime3 = System.nanoTime();
					wordFreq.add(wCounter);
					timeToAdd = System.nanoTime() - startTime3; 
				}
				
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} finally{
			input.close();
		}
		
		  //print
		  System.out.println("to add new element: " + timeToAdd + " ns");
		  System.out.println("to check if element exists: " + timeToExist + " ns");
		  System.out.println("to update element: " + timeToUpdate + " ns");
		  
		  //overall time to get top 10 letters
		  double getFreqTime = createObjTime + timeToAdd + timeToExist + timeToUpdate; 
		  System.out.println("to get top 10: " + getFreqTime + " ns");
				
		return wordFreq;
	}
	
	/**
	 * method returns the word without any non-alphabetic characters 
	 * @param word
	 * @return
	 */	
	public String stripNonAlphaCharacters(String word){
		StringBuilder newWord = new StringBuilder(); 
		for (int i=0; i< word.length(); i++){
			char c = word.charAt(i);
			if (Character.isAlphabetic(c) || c=='\''){   //if c is alphabetic 
				newWord.append(c);
			} 
		}
		return newWord.toString(); 
	}
	
	///////////////////////////////////////// Q4: quotes //////////////////////////////////////////////////
	
	/**
	 * Question 4: find the 10 longest and shortest quotes 
	 * @return
	 */	
	public List<WordCounter> getQuotationsAndLength() {
		
		//create AL object and get time 
		long startTime = System.nanoTime();
        List<WordCounter> quotations = new ArrayList<>();
        long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new AL object: " + createObjTime + " ns");
				
		//to get timings 
		long timeToExist = 0; 
		long timeToAdd = 0; 

        InputStreamReader input = null;
        try {
        	//open the file 
            input = new InputStreamReader(new FileInputStream(new File(filename)), charSet);
            boolean inQuote = false;
            boolean ignoreUntilNextQuote = false;
            LAST_CHAR lastChar = null;
            StringBuilder sb = new StringBuilder();
            //read each char
            while (input.ready()) {
                Character c = new Character((char) input.read());
                //ignoreUntilNextQuote is true if there is a break in the quote (so we want
                //ignore this portion) and not include it as a part of the quote 
                if (ignoreUntilNextQuote) {
                	//but you come across a " or ' then ignoreUntilNextQuote becomes false 
                    if (c == this.quotationMark) {
                        ignoreUntilNextQuote = false;
                    } else if (".?!".indexOf(c) >= 0){
                    	ignoreUntilNextQuote = false;
                    	if (sb != null){
                    		inQuote = false;
                    		boolean dupeQuote = false;                 
                    		for (WordCounter wc : quotations){
                    			if (sb.toString().equals(wc.getWord())){                 				
                    				dupeQuote = true; 
                    				break; 
                    			}
                    		}
                    		if (!dupeQuote){
                                //add this string to the list 
                    			//*get time to add new element 
            					long startTime2 = System.nanoTime();
                                quotations.add(new WordCounter(sb.toString(), sb.toString().length()));
                                timeToAdd = System.nanoTime() - startTime2;              					
                    		}
                            sb = null;
                    	}
                    }
                    continue;
                }
                //if you're not inside a quote 
                if (!inQuote) {
                	//but you come across a quotation mark
                    if (c == this.quotationMark) {
                    	//create a new string builder 
                        sb = new StringBuilder();
                        //check the last char (excluding spaces)
                        if (lastChar == LAST_CHAR.PERIOD_QUESTION_EXCLAMATION || lastChar == LAST_CHAR.COMMA || lastChar == LAST_CHAR.QUOTATION_MARK) {
                            //means you must now be inside a quote 
                        	inQuote = true;
                        } else if (lastChar == LAST_CHAR.ALPHABETIC_CHARACTER) {
                            //must be a possessive/contraction and not a quote
                        	//don't do anything 
                        } else {
                            ignoreUntilNextQuote = true;
                        }
                    }
                  //if you are currently inside a quote 
                } else {
                	//and you come across another quotation mark 
                    if (c == this.quotationMark) {
                    	//if last char is an end of the sentence punctuation
                        if (lastChar == LAST_CHAR.PERIOD_QUESTION_EXCLAMATION) {
                        	//means you are now done with the quote 
                            inQuote = false;           
                            boolean dupeQuote = false; 
                          //*time for checking if element exists in list
            				long startTime1 = System.nanoTime();
                    		for (WordCounter wc : quotations){
                    			if (sb.toString().equals(wc.getWord())){
                    				timeToExist = System.nanoTime() - startTime1; 
                    				dupeQuote = true; 
                    				break; 
                    			}
                    		}
                    		if (!dupeQuote){
                                //add this string to the list 
                                quotations.add(new WordCounter(sb.toString(), sb.toString().length()));
                    		}                           
                            sb = null;
                        } else if (lastChar == LAST_CHAR.ALPHABETIC_CHARACTER) {
                        	//false positive 
                            inQuote = false;
                            ignoreUntilNextQuote = true;
                            sb = null;
                            //if previous char is a comma then means it's a broken quote 
                        } else if (lastChar == LAST_CHAR.COMMA) {
                        	ignoreUntilNextQuote = true;
                        	continue; 
                        } else if (lastChar == LAST_CHAR.QUOTATION_MARK){
                        	inQuote = true; 
                        	ignoreUntilNextQuote = false;
                        	sb = new StringBuilder(); 
                        }
                    } else {
                    	//if newline or carriage return 
                        if (c == 13 || c == 10) {
                            // then convert to space
                            sb.append(' ');
                        } else {
                        	//keep adding char to the string builder 
                            sb.append(c);
                        }
                    }
                }
                //see which character corresponds to each enum 
                if (!inQuote) {
                    if (c == ',' || c == ';' || c == ':') {
                        lastChar = LAST_CHAR.COMMA;
                    } else if (".?!".indexOf(c) >= 0) {
                        lastChar = LAST_CHAR.PERIOD_QUESTION_EXCLAMATION;
                    } else if (c == this.quotationMark) {
                        lastChar = LAST_CHAR.QUOTATION_MARK;
                    } else if (Character.isAlphabetic(c)) {
                        lastChar = LAST_CHAR.ALPHABETIC_CHARACTER;
                    }
                    //are in a quote 
                } else {  
                    if (c == ',' || c == ';' || c == ':') {
                        lastChar = LAST_CHAR.COMMA;
                    } else if (".?!".indexOf(c) >= 0) {
                        lastChar = LAST_CHAR.PERIOD_QUESTION_EXCLAMATION;
                    } else if (c == this.quotationMark) {
                        lastChar = LAST_CHAR.QUOTATION_MARK;
                    } else {
                        lastChar = LAST_CHAR.ALPHABETIC_CHARACTER;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        	      
	      //print
	      System.out.println("to add new element: " + timeToAdd + " ns");
	      System.out.println("to check if element exists: " + timeToExist + " ns");
	      
	      //overall time to get top 10 letters
	      //double getFreqTime = createObjTime + timeToAdd + timeToExist; 
	      double getFreqTime = createObjTime + timeToAdd; 
	      System.out.println("to get top 10: " + getFreqTime + " ns");
	      

        return quotations;
    }
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * print the top 10 most frequent words
	 * @param sortedWordFreq
	 * @param max
	 */
	public void printMostFreqWords(List<WordCounter> wordFreq, int max, boolean descending){
		
		if (descending){
			//sorts the list in descending order (natural sort order of WordCounter objects)
			Collections.sort(wordFreq);
		} else {
			//sort in ascending order 
			Collections.sort(wordFreq, new Comparator<WordCounter>(){
				public int compare(WordCounter w1, WordCounter w2){
					return w1.getCount().compareTo(w2.getCount()); 
				}
			});		
		}
		for(int i = 0; i<max; i++){
			WordCounter w = wordFreq.get(i);
			System.out.printf("%d. %s : %,d\n", i+1, w.getWord(), w.getCount());
		}
	}
	
	
	/**
	 * reads the "stop-lists.txt" file and puts it in a Set
	 * @return
	 */
	public Set<String> getStopListWords(){
		
		Set<String> stopListWords = new HashSet<>();
		Scanner input = null; 
		
		try{
			input = new Scanner(new File("stop-list.txt"));
			while(input.hasNextLine()){
				String stopWord = input.nextLine(); 
				stopWord = stopWord.toUpperCase(); 
				stopListWords.add(stopWord);
			}		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			input.close();
		}		
		return stopListWords; 		
	}
	
	////////////////////////////Q5: Wild Card ///////////////////////////////////////////
	/**
	 * read file, get the word and see if it's a palindrome and it must be 
	 * 3 or more chars long (print it if it is)
	 * @return
	 */
	public void checkIfPallindrome(){
		
		long startTime = System.nanoTime();
		List<String> listOfWords = new ArrayList<String>();
		long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new AL object: " + createObjTime + " ns");
		
		Scanner input = null;
		
		//to get timings 
		long timeToExist = 0; 
		long timeToAdd = 0; 
		
		try{		
			input = new Scanner(new File(filename), charSet);
			
			while (input.hasNext()){
				String word = input.next();
				//change all words to the same case 
				word = word.toUpperCase();
				//ignore any non-alphabetic chars
				word = stripNonAlphaCharacters(word);
				//if word is a palindrome 
				if(word.length() >= 3 && word.equals(new StringBuilder(word).reverse().toString())){	
					//*time for checking if element exists or not in list
					long startTime1 = System.nanoTime();
					if(!listOfWords.contains(word)){
						timeToExist = System.nanoTime() - startTime1; 			
						
						//*get time to add new element 
						long startTime3 = System.nanoTime();
						listOfWords.add(word);
						timeToAdd = System.nanoTime() - startTime3; 
					}
		        }				
			}			
			//print the set
			for (String str : listOfWords){
				//System.out.println(str);
			}			    
			
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} finally{
			input.close();
		}
		
		  //print
		  System.out.println("to add new element: " + timeToAdd + " ns");
		  System.out.println("to check if element exists: " + timeToExist + " ns");
		  
		  //overall time to get top 10 letters
		  //double getFreqTime = createObjTime + timeToAdd + timeToExist; 
		  double getFreqTime = createObjTime + timeToAdd; 
		  System.out.println("to get top 10: " + getFreqTime + " ns");		
		
	}
	
}






