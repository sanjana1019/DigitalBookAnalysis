import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class MapTextAnalyzer {
	private String filename; 
	private String charSet; 
	private char quotationMark; 
	private Set<String> stopWordSet; 
	private enum LAST_CHAR {
        QUOTATION_MARK, COMMA, PERIOD_QUESTION_EXCLAMATION, ALPHABETIC_CHARACTER
    };
	
	public static void main(String[] args) {
		//TODO parameterize the name and character set 
		String nameOfFile = "pride-prejudice.txt";
		//String nameOfFile = "les-mis.txt";
		//String nameOfFile = "huck-finn.txt";
		//String nameOfFile = "christmas-carol.txt";
		//String nameOfFile = "tom-sawyer.txt";
		String charSet = "ASCII";
		//String charSet = "ISO-8859-1";
		char quotationMark = '"';
		//char quotationMark = '\'';
		
		System.out.println("top 10 letter frequencies:");
		//create MapTextAnalyzer object called text 
		MapTextAnalyzer text = new MapTextAnalyzer(nameOfFile, charSet, quotationMark);
		//Q1: call method to get character frequencies 
		Set<CharacterCounter> charfreqs = text.getCharacterFrequencies();		
		//print the top 10 most frequent characters
		//text.printMostFreqCharacters(charfreqs.iterator(), 10);
		
		//Q2: word frequency
		System.out.println("\ntop 10 word frequencies:");
		//call method to get word frequencies, takes false as param b/c not using stop list 
		Set<WordCounter> wordFreqs = text.getWordFrequencies(false);;		
		//print the top 10 most frequent words
		//text.printMostFreqWords(wordFreqs.iterator(), 10);
	
		//Q3: word frequency with stop list 
		System.out.println("\ntop 10 word frequencies with Stop List:");
		//call method to get word frequencies, takes true as param b/c using stop list
		Set<WordCounter> wordFreqsWithStopList = text.getWordFrequencies(true);	
		//print result
		//text.printMostFreqWords(wordFreqsWithStopList.iterator(), 10);
		
		//Q4: top 10 longest quotations
		System.out.println("\ntop 10 longest quotations:");
		//call method to get the quotations
		NavigableSet<WordCounter> quotationsAndLength = text.getQuotationsAndLength();	
		//print result
		//text.printMostFreqWords(quotationsAndLength.iterator(), 10);
	
		/**
		Q4: top 10 shortest quotations
		System.out.println("\ntop 10 shortest quotations:");
		print result
		text.printMostFreqWords(quotationsAndLength.descendingIterator(), 10);
		*/ 
		
		//Q5 wild card: print any words that are greater than or equal to 3 chars long 
		//and are palindromes 
		System.out.println("\nWild Card - Palindromes:");
		text.checkIfPallindrome();
	}
	
	

	/**
	 * constructor
	 * @param filename
	 */
	public MapTextAnalyzer(String filename, String charSet, char quotationMark){
		this.filename = filename; 
		this.charSet = charSet;	
		this.stopWordSet = getStopListWords();	
		this.quotationMark = quotationMark;
	}
	
	
	/**
	 * read the file, get the letter and the count and sort them
	 * @return
	 */
	public Set<CharacterCounter> getCharacterFrequencies(){
		
		//declare map
		long startTime = System.nanoTime();
		Map<Character,Integer> characterFreq = new HashMap<>();
		long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new map object: " + createObjTime + " ns");
		
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
				
				//*time for checking if element exists in map 
				long startTime1 = System.nanoTime();
				if(characterFreq.containsKey(c)){      
					timeToExist = System.nanoTime() - startTime1; 
			
					//*updating existing element
					long startTime2 = System.nanoTime();
					int countOfCharacter = characterFreq.get(c); 					
					countOfCharacter++; 
					characterFreq.put(c, countOfCharacter); 
					timeToUpdate = System.nanoTime() - startTime2; 
						 
				} else{
					//*get time to add new element 
					long startTime3 = System.nanoTime();
					characterFreq.put(c, 1);
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
	       
		//puts elements of HashMap into a TreeSet
		//objects will now be in descending order 	 
		Set<CharacterCounter> sortedFreq = new TreeSet<>();
		for( Map.Entry<Character, Integer> entry: characterFreq.entrySet()){
			sortedFreq.add(new CharacterCounter(entry.getKey(), entry.getValue()));
		}
		//return the set
		return sortedFreq;
	}
	
	
	/**
	 * print the top 10 most frequent letters
	 * @param sortedFreq
	 * @param max
	 */
	public void printMostFreqCharacters(Iterator<CharacterCounter> sortedFreq, int max){
		for(int i = 0; i<max; i++){
			CharacterCounter cc = sortedFreq.next();
			System.out.printf("%c : %,d\n", cc.getC(), cc.getCount());
		}
	}
	
	////////////////////////////Q2 and Q3: word frequency ///////////////////////////////////////////
	/**
	 * read file, get the word and count and sort it 
	 * @return
	 */
	public Set<WordCounter> getWordFrequencies(boolean useStopList){
		//declare map
		long startTime = System.nanoTime();
		Map<String, Integer> wordFreq = new HashMap<>();
		long createObjTime  = System.nanoTime() - startTime; 
		System.out.println("to create new map object: " + createObjTime  + " ns");
		Scanner input = null;
		
		//to get timings 
		long timeToExist = 0; 
		long timeToAdd = 0; 
		long timeToUpdate = 0; 
		
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
				//*time for checking if element exists in map 
				long startTime1 = System.nanoTime();
				if (wordFreq.containsKey(word)){
					timeToExist = System.nanoTime() - startTime1; 
	
					//*updating existing element
					long startTime2 = System.nanoTime();
					int wordCount = wordFreq.get(word);
					wordCount++;
					wordFreq.put(word, wordCount);
					timeToUpdate = System.nanoTime() - startTime2; 
	
				}else{
					//*get time to add new element 
					long startTime3 = System.nanoTime();
					wordFreq.put(word, 1);
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

		//puts elements of HashMap into a TreeSet
		//objects will now be in descending order 	 
		Set<WordCounter> sortedWordFreq = new TreeSet<>();
		for( Map.Entry<String, Integer> entry: wordFreq.entrySet()){
			sortedWordFreq.add(new WordCounter(entry.getKey(), entry.getValue()));
		}
		return sortedWordFreq;
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
	public NavigableSet<WordCounter> getQuotationsAndLength(){
			
		//create map object
		long startTime = System.nanoTime();
		Map<String,Integer> quotationsMap = new HashMap<>(); 
		long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new map object: " + createObjTime + " ns");
		
		//to get timings 
		long timeToAdd1 = 0; 
		long timeToAdd2 = 0; 

				
		//open a file
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
                //ignoreUntilNextQuote is true if there is a break in the quote (so we want to
                //ignore this portion) and not include it as a part of the quote 
                if (ignoreUntilNextQuote) {
                	//but you come across a " or ' then ignoreUntilNextQuote becomes false 
                    if (c == this.quotationMark) {
                        ignoreUntilNextQuote = false;
                    } else if (".?!".indexOf(c) >= 0){
                    	ignoreUntilNextQuote = false;
                    	if (sb != null){
                    		inQuote = false;
                            //add this string to the map 
                    		//*get time to add new element     
        					long startTime1 = System.nanoTime();
                    		quotationsMap.put(sb.toString(), sb.toString().length());
                    		timeToAdd1 = System.nanoTime() - startTime1; 

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
                            //add this string to the map
                            //time to add new element 
        					long startTime2 = System.nanoTime();
                    		quotationsMap.put(sb.toString(), sb.toString().length());
                    		timeToAdd2 = System.nanoTime() - startTime2; 

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
        }
		catch (IOException e){
			e.printStackTrace();
		}	
        
      //print
  	  System.out.println("to add new element: " + timeToAdd1 + " ns");
  	  //System.out.println("to add new element2: " + timeToAdd2 + " ns");

  	  
  	  //overall time to get top 10 letters
  	  double getFreqTime = createObjTime + timeToAdd1; 
  	  System.out.println("to get top 10: " + getFreqTime + " ns");

                
		
		//puts elements of HashMap into a TreeSet
		//objects will now be in descending order 	 
		NavigableSet<WordCounter> quotationsAndLengths = new TreeSet<>();
		for( Map.Entry<String, Integer> entry: quotationsMap.entrySet()){
			quotationsAndLengths.add(new WordCounter(entry.getKey(), entry.getValue()));
		}
		return quotationsAndLengths; 
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * print the top 10 most frequent words
	 * @param sortedWordFreq
	 * @param max
	 */
	public void printMostFreqWords(Iterator<WordCounter> sortedWordFreq, int max){
		for(int i = 0; i<max; i++){
			WordCounter w = sortedWordFreq.next();
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
		Map<String, Object> mapOfWords = new HashMap<>();
		long createObjTime = System.nanoTime() - startTime; 
		System.out.println("to create new map object: " + createObjTime + " ns");
		
		//to get timings 
		long timeToAdd = 0; 
				
		Scanner input = null;
		
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
					//*get time to add new element 
					long startTime3 = System.nanoTime();
					mapOfWords.put(word,null);		
					timeToAdd = System.nanoTime() - startTime3; 				
		        }				
			}			
			//print the map
			for (String str : mapOfWords.keySet()){
				//System.out.println(str);
			}			    
			
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} finally{
			input.close();
		}
		
		  //print
		  System.out.println("to add new element: " + timeToAdd + " ns");
		  
		  //overall time to get top 10 letters
		  double getFreqTime = createObjTime + timeToAdd; 
		  System.out.println("to get top 10: " + getFreqTime + " ns");		
		
		
	}
	
}

