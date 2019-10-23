import java.io.*;
import java.util.*;

/*
 * Code downloaded from the course's online assistance and edited .
 * 
 * 
 * Reads from file and returns an Array (or collection) of words and their location.
 * This will be used as the input for the 
 */
class ReadFromAsciiFile {
	
	ArrayList<Tuple> results ;
	
	
	public ReadFromAsciiFile(String filename) throws IOException
	{
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		int line_count=0;
		int byte_count;
		int total_byte_count=0;
		int fromIndex;
		results = new ArrayList<Tuple>(); //results (?)
		
		
		while( (line = br.readLine())!= null ){
			fromIndex=0;
        		// \\s+ means any number of whitespaces between tokens
//    			String [] tokens = line.split("\\s+");
    			String [] tokens = line.split(",\\s+|\\s*\\\"\\s*|\\s+|\\.\\s*|\\s*\\:\\s*");
			String line_rest=line;
    			for (int i=1; i <= tokens.length; i++) {
				byte_count = line_rest.indexOf(tokens[i-1]);
				//fromIndex = fromIndex + byte_count + 1 + tokens[i-1].length();
				if ( tokens[i-1].length() != 0){
//       				  System.out.println("( word:" + i + ", start_byte:" + (total_byte_count + fromIndex + byte_count) + "' word_length:" + tokens[i-1].length() + ") = " + tokens[i-1]);
				  results.add(new Tuple(tokens[i-1], total_byte_count + fromIndex + byte_count)); //Tuple = word+location
				}
				fromIndex = fromIndex + byte_count + tokens[i-1].length();
				if (fromIndex < line.length())
				  line_rest = line.substring(fromIndex);
    			}
			total_byte_count += fromIndex + 2; // 2 bytes for CR
		}
		br.close();
	
}
	
	
	
	public Tuple[] getResults(){
		return results.toArray(new Tuple[results.size()]);
	}
}
