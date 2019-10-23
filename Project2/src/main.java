import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class main {

	/*Minimum page size=512.
	 * Page sizes smaller than this will cause a crash. 
	 * 
	 * Recommended page size : As large as possible.
	*/
	final static int PAGE_SIZE=512;
	
	public static int BT_DiskAccesses=0;
	public static int PL_DiskAccesses=0;
	
	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		

		//Dictionary and Index files.
		String DictName="Dictionary.ser";
		String IndName ="Index.ser";
		B_Tree myDict= new B_Tree(PAGE_SIZE,DictName);
		PostingList Index= new PostingList(PAGE_SIZE, IndName);
		
		//Text file names.
		String[] deaultNames={"Kennedy.txt","MartinLutherKing.txt","Obama.txt"}; 
		
		
		
		Scanner input= new Scanner(System.in);
		int selection=0;
		do{

				System.out.println("================================================");
				System.out.println("\nMenu:\n");
				
				System.out.print("1. Scan default files :"); for(int i=0;i<deaultNames.length;i++) System.out.print(" \""+deaultNames[i]+"\"");
				System.out.println("\n2. Search a word's locations.");
				System.out.println("3. Scan a new file.");
				System.out.println("4. Default program (Project).");
				System.out.println("0. Exit.");
				System.out.print("Please select your menu option: ");
				selection=input.nextInt();
				switch(selection){
								case 1:			
												scanFiles(deaultNames,myDict,Index);
												break;
										
								case 2: 		System.out.print("Enter search keyword: ");
												PrintLocations(input.next(),myDict,Index);
												break;
										
								case 3: 		
												String[] newFile=new String[1];
												try{
													System.out.print("Enter file name: ");
													newFile[0]=input.next();
													scanFiles(newFile,myDict,Index);
												}catch(FileNotFoundException f){
													System.out.println("Name missmatch. Retrying.");
													newFile[0]=newFile[0]+".txt";
													scanFiles(newFile,myDict,Index);
												}
												break;
							
								case 4: //Scan and count the number of disk accesses.
										scanFiles(deaultNames,myDict,Index);
										//Search for words in the files.

										//Valid.
											String[] validWords = new String[100];
											Tuple[] holder=new ReadFromAsciiFile("Excerpts.txt").getResults();
											for(int i=0;i<100;i++){
												validWords[i]=holder[i].file;
											}
											countAccesses(validWords,myDict, Index);
											
										//Random.
											String[] randomWords= new String[100];
											holder=new ReadFromAsciiFile("Trump.txt").getResults();
											for(int i=0;i<100;i++){
												randomWords[i]=holder[i].file;
											}
											countAccesses(randomWords,myDict, Index);
											break;
									
								case 0: break;
								default:System.out.println("\nInvalid selection!"); 
										break;
								
				}
		}while(selection!=0);
		
		System.out.println("\nProgram terminated!\n\n");
		
		System.out.println("Dictionary file: \""+DictName+"\"");
		System.out.println("Index file: \""+IndName+"\"");
	
	}
	
	/*
	 * Scans the input text files and completes the index file and dictionary.
	 */
	static void scanFiles(String[] fileNames,B_Tree myDict,PostingList index) throws ClassNotFoundException, IOException{
		System.out.println("Starting scan:");
		int TotalWords=0;
		//Scan each file in the list.
		for(int i=0;i<fileNames.length;i++){
	
				
				System.out.println("\nScanning file \""+fileNames[i]+"\".");
				System.out.print("Scanning ");
				Tuple[] words= new ReadFromAsciiFile(fileNames[i]).getResults();
				TotalWords+=words.length;
				for(int k=0;k<words.length;k++){
					if(k%100==0 && k>0){
						System.out.print(".");
						if(k%500==0){ 
							System.out.println("\nRegistered first "+k+" words.");
							System.out.print("Scanning ");
						}
					}
					
					//Get next word and location from file.
					String scannedWord = words[k].file; 	
					int byte_loc = words[k].position;			
					
					//Search if the word is saved in the b-tree dictionary. 
				    B_Node searchResult=myDict.searchKey(scannedWord);
				    //If the search didn't find a word, we have a new entry.
				    boolean newWord=(searchResult==null);	
				   
				    //Enter the Word in the index file.
				    Tuple newEntry=new Tuple(fileNames[i],byte_loc);
				   
				    //If we found a new word, insert it in the dictionary.
				    if(newWord){
				    	int index_page=index.insert(newEntry, 0 /*Doesn't matter since newWord=true */, newWord); 	//Returns the new page location (former currentID/file pointer).
				    	myDict.insertKey(scannedWord,index_page);
				    }else{
				    	//Search the index position in the B_Node and retrieve numkey. Enter the result in the corresponding post of the index.
				    	index.insert(newEntry, getIndexPage(searchResult,scannedWord),false);
				    }
				}				
		}
		System.out.println("\n\nScan complete!");
		System.out.println("Median Posting List cost: "+((float)(main.PL_DiskAccesses))/TotalWords);
		System.out.println("Median B-Tree cost: "+((float)(main.BT_DiskAccesses))/TotalWords);
		System.out.println("Median scanning cost: "+((float)(main.BT_DiskAccesses+main.PL_DiskAccesses))/TotalWords);
		//reset the counters.
		main.BT_DiskAccesses=0;
		main.PL_DiskAccesses=0;
		
	}
	
	/*
	 * Prints all found locations of a given "word".
	 */
	static void PrintLocations(String word,B_Tree myDict,PostingList index) throws ClassNotFoundException, IOException{
		//Get all the words.
		B_Node search=myDict.searchKey(word);
		if(search==null) return;
				
		int IndexPage=getIndexPage(search,word);
		Tuple[] locations=index.retrieveWords(IndexPage);
		
		//Print them.
		for(int i=0;i<locations.length;i++){
//			System.out.println("Text \""+locations[i].file+"\" includes word \""+word+"\" on location "+locations[i].position);
		}
		
/*		System.out.println("\n\nSearch complete!");
		System.out.println("B-Tree Disk access cost:"+main.BT_DiskAccesses);
		System.out.println("Posting List Disk access cost: "+main.PL_DiskAccesses);				
		System.out.println("Total scanning cost: "+(main.BT_DiskAccesses+main.PL_DiskAccesses));*/
		
	}
	
	//Counts the accesses required for a complete search of all words in an Array.
	static void countAccesses(String[] words,B_Tree myDict,PostingList index) throws ClassNotFoundException, IOException{
		for(int i=0;i<words.length;i++){
			PrintLocations(words[i],myDict,index);
		}
		//print and reset.
		System.out.println("Median B-Tree Disk access cost:"+(float)main.BT_DiskAccesses/words.length); 
		System.out.println("Median Posting List Disk access cost: "+(float)main.PL_DiskAccesses/words.length);	 
		main.BT_DiskAccesses=0;
		main.PL_DiskAccesses=0;
	}
	
	
	
	
	
	/*
	 * Returns the position ,in a node, of a known reference in the index file.
	 */
	static int getIndexPage(B_Node node,String string){
		int i=0;
		while(!node.keyword[i].equals(string) /*&& i<node.current_size*/){
			i++;
		}
		return node.num_key[i];
	}	
}