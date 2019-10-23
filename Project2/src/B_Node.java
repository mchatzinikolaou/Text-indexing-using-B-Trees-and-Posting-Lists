import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class B_Node implements Serializable{

	/**
	 * IF this crashes, consider pre-allocating the strings to 12 char length.
	 */
	private static final long serialVersionUID = 575166238048545334L;
	
	
	int grade;
	//The id of the node. This can deduce its location in the file.
	int ID;	
	//The name of the file in which the node is stored.
	String filename;
	int[] children_IDs;
	int[]	num_key;
	String[] keyword;
	int PAGE_SIZE;
	boolean isLeaf;
	int current_size;
	
	
	
	/* Creates new node and saves it on the file.
	 * Mind that the current node id must be incremented on the calling class.*/
	public B_Node(int Grade,String filename,boolean leaf,int ID,int page_size) throws IOException{ 
		grade=Grade; this.filename=filename; this.ID=ID;
		
		children_IDs=new int[grade+1];
		keyword= new String[grade];
		num_key = new int[grade];
		for(int i=0;i<grade;i++){
			char[] chars = new char[12];
			keyword[i]=new String(chars);
			children_IDs[i]=0;
			num_key[i]=0;
		}
		isLeaf=leaf;
		PAGE_SIZE=page_size;
		current_size=0;
		writeToFile();
	}
	
	
	
	public long positionInFile(){
		return ID*PAGE_SIZE;
	}
	
	 boolean isFull() {
		return current_size==grade;
	}
	
	
	/*
	 * Looks up each node for a key. Goes down to leaf level. Returns null if a match is not found.
	 */
	public B_Node searchKey(String string) throws ClassNotFoundException, IOException {
		//Look up every registry in the current node.
				int i=0;
				for(;i<current_size && string.compareTo(keyword[i])<=0;i++){
					if (keyword[i].equals(string)){
						return this;
					}
				}
				//if done searching and this is a leaf, return null (not found)
				if(isLeaf){
					return null;
				}
				//If this is not a leaf, search the according child node.
				else{
					B_Node child=readFromFile(children_IDs[i]);
					return child.searchKey(string);
				}
	}
	
	
	public int splitChild(int child_position,int current_id) throws ClassNotFoundException, IOException{
		
		
		
		//Locate and isolate the child to be split.
		
		B_Node child=readFromFile(children_IDs[child_position]);
		
		//Isolate the median.
		String medianWord=child.keyword[child.current_size/2];      
		int medianIndex=child.num_key[child.current_size/2];
		
		//Create the two nodes to be re-mapped.
		B_Node newNode1 = new B_Node(grade, filename, child.isLeaf,child.ID, PAGE_SIZE);
		B_Node newNode2 = new B_Node(grade, filename, child.isLeaf,current_id++, PAGE_SIZE);	//Here (and ONLY here), we create a new id.
		
		
		
		
		int diff=(child.current_size+1)/2;
		
		//Remap children and keys.
			int i=0;
			
			//Map the keys.
				while(i<diff-1){
					newNode1.keyword[i]=child.keyword[i];
					newNode1.num_key[i]=child.num_key[i];
					newNode1.current_size++;
					i++; 
				}
				//Skip the Median.
				i++;
				
				//Continue on the second half.
				while(i<child.current_size){
					newNode2.keyword[i-diff]=child.keyword[i];
					newNode2.num_key[i-diff]=child.num_key[i];
					newNode2.current_size++;
					i++;
				}
			//Map the children. 
																			i=0;
				while(i<=newNode1.current_size){
					newNode1.children_IDs[i]=child.children_IDs[i];
					i++; 
				}
				while(i<=child.current_size){
					newNode2.children_IDs[i-diff]=child.children_IDs[i];
					i++;
				}
					
			//Insert median in upper node.
				//increase the number of elements held in this node by 1.
				current_size++;
				//Insert at the appropriate spot and shift the rest keys.
				for(i=current_size-1;i>child_position;i--){
					keyword[i]=keyword[i-1]; 
					num_key[i]=num_key[i-1]; 
				}
				keyword[child_position]=medianWord;
				num_key[child_position]=medianIndex;
				
			
			//Insert the children in the upper node.
				for(i=current_size;i>child_position+1;i--){
					children_IDs[i]=children_IDs[i-1]; 
					}
				children_IDs[child_position]=newNode1.ID;
				children_IDs[child_position+1]=newNode2.ID;
				
				
				
				
				
				
				
				
				//save them
				newNode1.writeToFile();
				newNode2.writeToFile();
				this.writeToFile();
				return current_id;
		
	}
	
	
	//Adds a pair of index+keyword.
	public int insertKey(String word,int index,int current_id) throws ClassNotFoundException, IOException{
		if(isLeaf){
			//Find the spot.
			int loc_to_be_inserted=0;
			while ( loc_to_be_inserted<current_size && word.compareTo(keyword[loc_to_be_inserted])<=0) loc_to_be_inserted++;
			//increase the number of elements held in this node by 1.
			current_size++;
			//Insert at the appropriate spot and shift the rest of the keys.
			for(int i=current_size-1;i>loc_to_be_inserted;i--){
				keyword[i]=keyword[i-1]; 
				num_key[i]=num_key[i-1];
				}
			
			keyword[loc_to_be_inserted]=word;
			num_key[loc_to_be_inserted]=index;
		}else{
			int child_to_insert_at=0;
			
			while ( child_to_insert_at<current_size && word.compareTo(keyword[child_to_insert_at])<=0)
				child_to_insert_at++;
			
			B_Node child= readFromFile(children_IDs[child_to_insert_at]);
			if (child.isFull()) {
				current_id=this.splitChild(child_to_insert_at, current_id); 
				child=readFromFile(child.ID);
			}
			
			current_id=child.insertKey(word,index,current_id); 
		}
			
			
			for (int i=0;i<current_size;i++){
				}
		this.writeToFile();
		return current_id;
	}
	
	

	
	
	public void writeToFile() throws IOException,ArrayIndexOutOfBoundsException{
		//Serialize Object.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(PAGE_SIZE);	//Calculate page size. 		  buffer capacity of the specified size, in BYTES.
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);
        byte[] DataPage = new byte[PAGE_SIZE];
        byte[] buf = bos.toByteArray();
        
        System.arraycopy( buf, 0, DataPage, 0, buf.length);
        //Write disk file.
		RandomAccessFile myFile = new RandomAccessFile(filename,"rw");
		myFile.seek(ID*PAGE_SIZE);
		myFile.write(DataPage);
		myFile.close();
		out.close();
		bos.close();
        out.close();
		
        main.BT_DiskAccesses++;//This costs one access.
	}
	
	public B_Node readFromFile(int nodeID)throws IOException, ClassNotFoundException{
		//Read disk file.   
		RandomAccessFile MyFile = new RandomAccessFile (filename, "r");
	       byte[] buf = new byte[PAGE_SIZE];
	       MyFile.seek(nodeID*PAGE_SIZE);
	       MyFile.read(buf); 		
	       // Deserialize Object
	       ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	       ObjectInputStream ois = new ObjectInputStream(bis);
	       B_Node deserializedObject = (B_Node)ois.readObject(); // deserialize data
	       
	       
	       ois.close();
	       MyFile.close();
	       main.BT_DiskAccesses++;//This costs one access.
	       return  deserializedObject;
	}
	
	
	
	
}
