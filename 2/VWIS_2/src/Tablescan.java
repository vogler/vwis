import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Tablescan extends DBIterator {

	private String filename;
	private DataInputStream in;
	private BufferedReader br;
	private String[] names;
	private String[] types;
	private int linecount = 2;

	public Tablescan(String filename) throws Exception {
		this.filename = filename;
	}

	@Override
	String[] open() throws Exception {
		FileInputStream fstream = new FileInputStream(filename);
		in = new DataInputStream(fstream);
		br = new BufferedReader(new InputStreamReader(in));
		String snames = br.readLine();
		String stypes = br.readLine();
		if(snames == null || stypes == null) throw new Exception("Header has the wrong format!");
		names = snames.split("\t");
		types = stypes.split("\t");
		if(names.length != types.length) throw new Exception("#colums have to match!");
		
		return names;
	}

	@Override
	Object[] next() throws Exception {
		if(br == null) throw new Exception("open() as to be called before next()!");
		String line =  br.readLine();
		if(line == null) return null;
		linecount++;
		String[] values = line.split("\t");
		if(values.length != names.length) throw new Exception("Error in line "+linecount+": #colums have to match!");
		return parse(values);
	}

	private Object[] parse(String[] values) throws Exception {
		List<Object> ret = new ArrayList<Object>();
		for(int i=0; i<types.length; i++){
			// strip quotes
			// TODO regex
			if(types[i].equals("String")) values[i] = values[i].substring(1, values[i].length()-1);
			// TODO generic solution
//			ret.add(Class.forName("java.lang."+types[i]).cast(values[i]));
			// java7 in eclipse?? -> no switch on strings
			if(types[i].equals("String")){
				ret.add(values[i]);
			}else if(types[i].equals("Integer")){
				ret.add(Integer.parseInt(values[i]));
			}else if(types[i].equals("Double")){
				ret.add(Double.parseDouble(values[i]));
			}else if(types[i].equals("Float")){
				ret.add(Float.parseFloat(values[i]));
			}else{
				throw new Exception("Unkown attribute type! Allowed: String, Integer, Double, Float");
			}
		}
		return ret.toArray();
	}

	@Override
	void close() throws Exception {
		in.close();
	}
}
