package risiko.net.configuration;

import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

public class ClientConfiguration extends Configure{

	public String server_address = null;
	
	public ClientConfiguration(String configPath){
		loadFile(configPath);
	}
	
	public void parseLine(String line){
		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  
			attribute=line.substring(0,index).trim(); 
			par=new Parser(line,index+1);  
		}
		else {  
			attribute=line; 
			par=new Parser("");  
		}

		// Parsing the attributes
		if (attribute.equals("server_address") ){
			server_address = par.getString(); 
			return;
		} 
	} 

}
