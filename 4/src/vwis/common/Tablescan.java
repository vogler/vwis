package vwis.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Tablescan implements DBIterator {

    String filename;
    BufferedReader inputStream;
    String[] types;

    public Tablescan(String filename) throws Exception {
        this.filename = filename;
    }

    // open file, split first line (attribute names) and give back string array
    @Override
    public String[] open() throws Exception {
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File file = new File(this.filename);
        this.inputStream = new BufferedReader(new FileReader(file));
        String[] attributes = this.inputStream.readLine().split("\t");
        this.types = this.inputStream.readLine().split("\t");
        return attributes;
    }

    // read next line as string, split it, convert tuple elements
    // back to original attribute type and give back object array
    @Override
    public Object[] next() throws Exception {
        String line = this.inputStream.readLine();
        if (line == null) {
            return null;
        }
        String[] tuple = line.split("\t");
        Object[] result = new Object[tuple.length];
        for (int i = 0; i < tuple.length; i++) {
            result[i] = this.convert(tuple[i], this.types[i]);
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        this.inputStream.close();
    }

    // convert given string into type and give it back
    private Object convert(String str, String type) throws Exception {
        if (type.equals("String")) {
            return str.substring(1, str.length() - 1);
        }
        if (type.equals("Integer")) {
            return Integer.parseInt(str);
        }
        if (type.equals("Double")) {
            return Double.parseDouble(str);
        }
        if (type.equals("Float")) {
            return Float.parseFloat(str);
        }
        throw new Exception("Unknown Type!");
    }

}
