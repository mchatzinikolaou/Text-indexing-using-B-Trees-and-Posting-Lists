import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;






public class Posting implements Serializable{
	
	
	private static final long serialVersionUID = 643491977166456221L;
	String[] text_name;
	int[]	location_in_text;
	int ID;
	String filename;
	int PAGE_SIZE;
	int MAX_POSTS;
	int current_size;
	int ovf_page;
	
	public Posting(String filename,int ID,int page_size,int max_posts) throws IOException{ 
		
		this.filename=filename; this.ID=ID;
		MAX_POSTS=max_posts;	PAGE_SIZE=page_size;
		
		text_name= new String[MAX_POSTS];
		location_in_text = new int[MAX_POSTS];
		for(int i=0;i<MAX_POSTS;i++){
			char[] chars = new char[12];	//max text name size is still 12 chars.
			text_name[i]=new String(chars);
			location_in_text[i]=0;
		}
		current_size=0;
		
		ovf_page=0;
		
		
		this.writeToFile();
	}
	
	public Posting() {}


		//Adds a pair of index+ file location.
		public int insertKey(Tuple toBeInserted,int current_id) throws ClassNotFoundException, IOException{
			//If the page is not full , enter the entry and return the id.
			if(!this.isFull()){
				text_name[current_size]=toBeInserted.file;
				location_in_text[current_size]=toBeInserted.position;
				current_size++;
				this.writeToFile();
			}else{
				//If there is no overflow page, create one.
				if(ovf_page==0){
					ovf_page=current_id;
					Posting overflow=new Posting(filename,current_id++, PAGE_SIZE, MAX_POSTS);
					current_id= overflow.insertKey(toBeInserted, current_id);
					this.writeToFile();
					
				//If there already exists an overflow page.
				}else{
					Posting overflow=this.readFromFile(ovf_page);
					current_id= overflow.insertKey(toBeInserted, current_id);
					this.writeToFile();
				}
			}

			return current_id; 
		}
	
		
		private boolean isFull() {
			return (current_size==MAX_POSTS);
		}

		
		
		
		public Tuple[] returnLocations() throws ClassNotFoundException, IOException{
			
			Posting retrieve=this;
			ArrayList<Tuple> results= new ArrayList<Tuple>();
			
			do{
				Tuple[] result= new Tuple[retrieve.current_size];
				for(int i=0;i<retrieve.current_size;i++){
					result[i]=new Tuple();
				}
				
					for(int i=0;i<retrieve.current_size;i++){
						result[i].position=retrieve.location_in_text[i];
						result[i].file=retrieve.text_name[i];
					}

				
				results.ensureCapacity(results.size()+result.length);
				results.addAll(Arrays.asList(result));
				
				//If there isn't an overflow page.
				if(retrieve.ovf_page==0){
					break;
				}else{
					retrieve=retrieve.readFromFile(retrieve.ovf_page);
				}
			}while(true);
			
		
			return results.toArray(new Tuple[results.size()]);
		}
		
		
	//Could be changed to generic or object, since its being used so frequently.
	public Posting readFromFile(int nodeID)throws IOException, ClassNotFoundException{
		//Read disk file.   
		RandomAccessFile MyFile = new RandomAccessFile (filename, "r");
	       byte[] buf = new byte[PAGE_SIZE];
	       MyFile.seek(nodeID*PAGE_SIZE);
	       MyFile.read(buf);
	       // Deserialize Object
	       ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	       ObjectInputStream ois = new ObjectInputStream(bis);
	       Posting deserializedObject = (Posting)ois.readObject(); // deserialize data
	       
	       
	       ois.close();
	       MyFile.close();
	       main.PL_DiskAccesses++;
	       return  deserializedObject;
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
        main.PL_DiskAccesses++;
	}
	
}
