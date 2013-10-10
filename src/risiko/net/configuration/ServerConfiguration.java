package risiko.net.configuration;

import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

public class ServerConfiguration extends Configure{

	public int min_clients_number;
	
	public ServerConfiguration(String configPath){
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
		if (attribute.equals("min_clients_number") ){
			min_clients_number = par.getInt(); 
			return;
		} 
	} 

}
