import java.io.File;
import java.io.IOException;

public class B_Tree {

	int PAGE_SIZE;
	int currentID;
	int grade;
	B_Node root;
	String filename;
	
	public B_Tree(int PAGE_SIZE,String Filename) throws IOException, ClassNotFoundException{
		
		//Initiate the file by adding the first node in it.
			filename=Filename;
			File delet_this= new File(filename);
			delet_this.delete();
			currentID=0;
			this.PAGE_SIZE=PAGE_SIZE;
			grade=calculateGrade(PAGE_SIZE);
			root=new B_Node(grade, filename, true, currentID++, PAGE_SIZE);
	}
	
	
	//REMEMBER: In order to avoid duplicates, while searching for the proper place to allocate a key on insertion , make the condition < instead of <=. This will prevent the entrance on the inner node which actually does the insertion.
			
	public void insertKey(String keyword,int index) throws ClassNotFoundException, IOException{
		if(keyword.length()>12) keyword=keyword.substring(0, 12);
			//Don't enter duplicates.
			if(searchKey(keyword)!=null){
				System.out.println("Word: "+keyword+"Will not be inserted since it's already registered.");
				return;
			}
			//If it is full, split the child.
				if(root.isFull()){
					B_Node newRoot=new B_Node(grade, filename, false,currentID++, PAGE_SIZE);
					
					newRoot.children_IDs[0]=root.ID;
					root=newRoot;
					currentID=root.splitChild(0, currentID);
					root=root.readFromFile(root.ID);
				}
				currentID=root.insertKey(keyword, index, currentID);
	}
		
		
	public B_Node searchKey(String string) throws ClassNotFoundException, IOException{
			root=root.readFromFile(root.ID);
			return root.searchKey(string);
	}
	
	/* This method calculates the maximum grade we can support, given the disk page.
	*	 As the page increases in size, there is more room for keys , so we can 
	*	 increase the size of the node accordingly.*/
	static int calculateGrade(int PAGE_SIZE) throws IOException, ClassNotFoundException{
		
		int grade=1;
		while(true){ 
			//Create mock pages and store them on the disk until there is a page overflow.
			try{
				new B_Node(grade, "test.temp", true, 0, PAGE_SIZE);
				grade++;
			}catch (ArrayIndexOutOfBoundsException e){
				break;
			}
		}	
		grade=grade-1;	//CHECK if grade-2 is needed.
		//Delete the temporary file.
		File delet_this= new File("test.temp");
		delet_this.delete();
		return grade; 

}
	
	
	
}
