import java.io.File;
import java.io.IOException;

public class PostingList {
	String filename;
	int currentID;
	int PAGE_SIZE;
	int max_posts;
	Posting holder;
	
	public PostingList(int PAGE_SIZE,String Filename) throws IOException, ClassNotFoundException{
		
		//Initiate the file by adding the first node in it.
			filename=Filename;
			File delet_this= new File(filename);
			delet_this.delete();
			currentID=0;
			this.PAGE_SIZE=PAGE_SIZE;
			max_posts=calculatePageSize(PAGE_SIZE);
			holder=new Posting(filename,currentID,PAGE_SIZE,max_posts);
			System.out.println();
	}
	
	/*
	 * Inserts the tuple at the said index. 
	 * 
	 * 	This method is mainly called by a b-tree function.
	 * If a match is not found , it's inserted onto a new page.
	 * 
	 * Returns the ID of the page.
	 */
	public int insert(Tuple Inserted,int index/*used only when newPage is false, a variable which Could be ommited with the use of negative values.*/,boolean newPage) throws ClassNotFoundException, IOException{
		
		int result;
		//Restrict the string length to 12 chars.
		if(Inserted.file.length()>12) Inserted.file=Inserted.file.substring(0, 12);
		
		//If this is a new Page, create it and insert the key there.
		if(newPage)
		{
			result=currentID;
			Posting toInsertAt=new Posting(filename,currentID++,PAGE_SIZE,max_posts);
			currentID=toInsertAt.insertKey(Inserted, currentID);
		}else{
			//Else, insert the new location in the previously allocated page.
			result=index;
			Posting toInsertAt=holder.readFromFile(index);
			currentID=toInsertAt.insertKey(Inserted, currentID);
		}
		return result;
	}
	
	public Tuple[] retrieveWords(int index) throws ClassNotFoundException, IOException{
		Posting fetch=holder.readFromFile(index);
		return fetch.returnLocations();
	}
	
	public int calculatePageSize(int PAGE_SIZE) throws IOException, ClassNotFoundException{
		
		int size=1;
		while(true){ 
			//Create mock pages and store them on the disk until there is a page overflow.
			try{
				new Posting("test.temp", 0, PAGE_SIZE, size);
				size++;
			}catch (ArrayIndexOutOfBoundsException e){
				break;
			}
		}	
		size=size-1;	//CHECK if size-2 is needed.
		//Delete the temporary file.
		File delet_this= new File("test.temp");
		delet_this.delete();
		return size;
	}
}
