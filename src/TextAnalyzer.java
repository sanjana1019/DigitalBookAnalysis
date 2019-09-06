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
import java.util.Map;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class TextAnalyzer {
	
	private String filename; 
	private String charSet; 
	private char quotationMark; 
	private Set<String> stopWordSet; 
	private enum LAST_CHAR {
        QUOTATION_MARK, COMMA, PERIOD_QUESTION_EXCLAMATION, ALPHABETIC_CHARACTER
    };
	
	public static void main(String[] args) {
		//TODO parameterize the name, character set, and quotation mark
		//String nameOfFile = "pride-prejudice.txt";
		String nameOfFile = "christmas-carol.txt";
		//String nameOfFile = "alice-in-wonderland.txt";
		//String nameOfFile = "les-mis.txt";
		//String nameOfFile = "metamorphosis.txt";
		//String nameOfFile = "my-man-jeeves.txt";
		//String nameOfFile = "tale-of-two-cities.txt";
		//String nameOfFile = "tom-sawyer.txt";
		String charSet = "ASCII";
		//String charSet = "ISO-8859-1";
		//char quotationMark = '\'';
		char quotationMark = '"';
		
		System.out.println("top 10 letter frequencies:");
		//create TextAnalyzer object called text 
		TextAnalyzer text = new TextAnalyzer(nameOfFile, charSet, quotationMark);
		//Q1: call method to get character frequencies 
		HashMap <Character, Integer> charfreqs = text.getCharacterFrequencies();
		//print the top 10 most frequent characters
		text.printMostFreqCharacters(charfreqs, 10);
		
		//Q2: word frequency
		System.out.println("\ntop 10 word frequencies:");
		//call method to get word frequencies, takes false as param b/c not using stop list 
		HashMap<String, Integer> wordFreqs = text.getWordFrequencies(false);
		//print the top 10 most frequent words
		text.printMostFreqWords(wordFreqs, 10, true);
		
		//Q3: word frequency with stop list 
		System.out.println("\ntop 10 word frequencies with Stop List:");
		//call method to get word frequencies, takes true as param b/c using stop list
		HashMap<String, Integer> wordFreqsWithStopList = text.getWordFrequencies(true);
		//print result
		text.printMostFreqWords(wordFreqsWithStopList, 10, true);
		
		//Q4: top 10 longest quotations
		System.out.println("\ntop 10 longest quotations:");
		//call method to get the quotations
		HashMap<String, Integer> quotationsAndLength = text.getQuotationsAndLength();
		//print result
		text.printMostFreqWords(quotationsAndLength, 10, true);
	
		//Q4: top 10 shortest quotations
		System.out.println("\ntop 10 shortest quotations:");
		//print result
		text.printMostFreqWords(quotationsAndLength, 10, false);
		
		//Q5 wild card: print any words that are greater than or equal to 3 chars long 
		//and are palindromes 
		System.out.println("\nWild Card - Palindromes:");
		text.checkIfPallindrome();

	}
	
	

	/**
	 * constructor
	 * @param filename
	 */
	public TextAnalyzer(String filename, String charSet, char quotationMark){
		this.filename = filename; 
		this.charSet = charSet;	
		this.stopWordSet = getStopListWords();	
		this.quotationMark = quotationMark;
	}
	
	
	/**
	 * read the file, get the letter and the count and sort them
	 * @return
	 */
	public HashMap<Character, Integer> getCharacterFrequencies(){
		
		//declare map
		HashMap<Character,Integer> characterFreq = new HashMap<>();
		
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
				//build a map one character at a time, counting the characters 
				if(characterFreq.containsKey(c)){     //if map has the char
					int countOfCharacter = characterFreq.get(c); //un-boxes
					countOfCharacter++;  //get the count and increase it 
					characterFreq.put(c, countOfCharacter);  //boxes
				} else{
					//else seeing char for first time 
					characterFreq.put(c, 1);
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

	
		//return the map
		return characterFreq;
	}
	

	
	/**
	 * print the top 10 most frequent letters
	 * @param sortedFreq
	 * @param max
	 */
	
	public void printMostFreqCharacters(HashMap<Character,Integer> sortedFreq, int max){
		ArrayList<CharacterCounter> sortedCharList = new ArrayList<>(); 
		for (Map.Entry<Character, Integer> entry : sortedFreq.entrySet()){
			Character c = entry.getKey(); 
			int charLength = entry.getValue(); 
			CharacterCounter cc = new CharacterCounter(c, charLength); 
			sortedCharList.add(cc); 
		}
		//sort the list in natural sorted order 
		Collections.sort(sortedCharList);
		//print out letter and associated count
		for(int i = 0; i<max; i++){
			CharacterCounter cc = sortedCharList.get(i);
			System.out.printf("%c : %,d\n", cc.getC(), cc.getCount());
		}
	}
	
	//////////////////////////// Q2 and Q3: word frequency ///////////////////////////////////////////
	/**
	 * read file, get the word and count and sort it 
	 * @return hashmap containing word and its frequency 
	 */
	public HashMap<String, Integer> getWordFrequencies(boolean useStopList){
		//declare map
		HashMap<String, Integer> wordFreq = new HashMap<>();
		
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
						//goes to top of while loop
						continue;    
					}
				}
				//build a map one word at a time, put in the HashMap
				if (wordFreq.containsKey(word)){
					int wordCount = wordFreq.get(word);
					wordCount++;
					wordFreq.put(word, wordCount);
				}else{
					wordFreq.put(word, 1);
				}
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} finally{
			input.close();
		}
		
		
		return wordFreq;
	}
	
	/**
	 * method returns the word without any non-alphabetic characters 
	 * @param word
	 * @return word without any non-alphabetic characters 
	 */	
	public String stripNonAlphaCharacters(String word){
		StringBuilder newWord = new StringBuilder(); 
		for (int i=0; i< word.length(); i++){
			char c = word.charAt(i);
			 //if c is alphabetic 
			if (Character.isAlphabetic(c) || c=='\''){  
				newWord.append(c);
			} 
		}
		return newWord.toString(); 
	}
	
	///////////////////////////////////////// Q4: quotes //////////////////////////////////////////////////
	
	/**
	 * Question 4: find the 10 longest and shortest quotes 
	 * @return hashMap with the quotes and their corresponding lengths 
	 */	
	public HashMap<String, Integer> getQuotationsAndLength() {
        HashMap<String, Integer> quotations = new HashMap<>();

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
                            quotations.put(sb.toString(), sb.toString().length());
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
                            quotations.put(sb.toString(), sb.toString().length());
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

        return quotations;
    }
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * print the top 10 most frequent words (Q2,Q3)
	 * also used to print longest/shortest quotes (Q4)
	 * @param sortedWordFreq
	 * @param max
	 */
	public void printMostFreqWords(HashMap<String, Integer> wordFreq, int max, boolean descending){
		ArrayList<WordCounter> sortedWordList = new ArrayList<>(); 
		for (Map.Entry<String, Integer> entry : wordFreq.entrySet()){
			String quotation = entry.getKey(); 
			int quotationLength = entry.getValue(); 
			WordCounter wc = new WordCounter(quotation, quotationLength); 
			sortedWordList.add(wc); 
		}
		
		if (descending){
			//sorts the list in descending order (natural sort order of WordCounter objects)
			Collections.sort(sortedWordList);
		} else {
			//sort in ascending order 
			Collections.sort(sortedWordList, new Comparator<WordCounter>(){
				public int compare(WordCounter w1, WordCounter w2){
					return w1.getCount().compareTo(w2.getCount()); 
				}
			});
			
		}
		//print out the word/quote and the associated count
		for(int i = 0; i<max; i++){
			WordCounter w = sortedWordList.get(i);
			System.out.printf("%d. %s : %,d\n", i+1, w.getWord(), w.getCount());
		}
	}
	
	
	/**
	 * reads the "stop-lists.txt" file and puts the words in a Set
	 * @return a set containing the stop words 
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
	 * @return void
	 */
	public void checkIfPallindrome(){
		Set<String> setOfWords = new HashSet<String>();
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
					setOfWords.add(word);			
					//System.out.println(word ); 
		        }				
			}			
			//print the set
			for (String str : setOfWords){
				System.out.println(str);
			}			    
			
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} finally{
			input.close();
		}
				
	}	
	
}

