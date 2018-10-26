package com.contec.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * ini 文件接口类  wh add
 * 
 */

public class IniFileIO {
	
	private String filename; 
	protected HashMap<String, Properties> sections = new HashMap<String, Properties>();
	private transient String section;     
	private transient Properties properties;
	
	public IniFileIO(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename)); 
		read(reader);         
		reader.close();         
		this.filename = filename;     
		} 
	protected void read(BufferedReader reader) throws IOException {
		String line;         
		while ((line = reader.readLine()) != null) {             
			parseLine(line);         
		}
	}
	private void parseLine(String line) { 
		line = line.trim(); 
		if(line.startsWith(";")){ 
			   return;        
		} 
		if (line.matches("\\[.*\\]") == true) {
			section = line.replaceFirst("\\[(.*)\\]", "$1");
			properties = new Properties();
			sections.put(section, properties);
		} else if (line.matches(".*=.*") == true) {
			if (properties != null) {
				int i = line.indexOf('=');
				String name = line.substring(0, i);
				String value = line.substring(i + 1);
				properties.setProperty(name, value);
			}
		}

	}
	
	public String getValue(String section, String name) {
		Properties p = sections.get(section);

		if (p == null) {
			return "";
		}

		String value = p.getProperty(name);
		return value;
	}
	
	public boolean putValue(String section, String name, String value){ 
		Properties p = (Properties) sections.get(section); 
		if (p == null){             
            p = new Properties();            
            sections.put(section, p);         
        } 
		String val = p.getProperty(name);         
		if(val == null){             
        }         
		p.setProperty(name, value);         
		return true; 
	}
	
	 public boolean commit() throws IOException{
		 FileWriter fw = null;         
		 BufferedWriter bw = null;         
		 fw = new FileWriter(filename);         
		 bw = new BufferedWriter(fw); 
		 if(sections == null || sections.isEmpty()){             
            bw.flush();             
            bw.close();             
            return true;         
          } 
		 Set<Entry<String, Properties>> entryset = sections.entrySet(); 
		 for(Entry<String, Properties> entry: entryset){ 
			 String strSection = (String) entry.getKey(); 
			 Properties p = (Properties) sections.get(strSection);             
			 bw.write("[" +strSection +"]");             
			 bw.newLine(); 
			 
			 if(p == null || p.isEmpty()){               
                    continue;            
             } 
			 for(Object obj: p.keySet()){                
				 String key = (String)obj;                 
				 bw.write(key +"=");                 
				 String value = p.getProperty(key);                 
				 bw.write(value);                 
				 bw.newLine();             
			} 
		 }
		 bw.flush();         
		 bw.close();         
		 return true; 
	 }
}
