import java.io.Serializable;

public class Tuple implements Serializable{
		/**
	 * 
	 */
	private static final long serialVersionUID = -625977331559375221L;
		String file;
		int	 position;
		
		public Tuple(String filename,int pos){
			file=filename;
			position=pos;
		}
		
		public Tuple(){
		}
		
		
		public String toString(){
			return ("File name: \""+this.file + "\" Location in file: "+ this.position);
		}
		
	}