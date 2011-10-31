
import java.io.Closeable;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Load a table from a file.
 */
public class Tablescan implements DBIterator {

    /**
     * Loads a table from the given file.
     * 
     * @param filename the file to load
     */
    public Tablescan(final String filename) {
        this(FileSystems.getDefault().getPath(filename));
    }

    /**
     * Loads a table from the given file.
     * 
     * @param filepath the file to load
     */
    public Tablescan(final Path filepath) {
        this.filepath = filepath;
        this.source = null;
    }

    /**
     * Parses the header.
     * 
     * The header consists of two lines.  The first line provides the names of
     * the attributes of the table separated by TAB characters.  The second line
     * defines the types of each row, again separated by TAB characters.
     * 
     * @throws IOException if reading the file failed
     * @throws TableFormatException if the header has an invalid format
     */
    private void parseHeader() throws IOException, TableFormatException {
        final String namesline = this.source.readLine();
        final String typesline = this.source.readLine();
        if (namesline == null || typesline == null) {
            throw new TableFormatException("Missing header");
        }
        this.names = namesline.split("\t");
        this.types = typesline.split("\t");
        if (this.names.length != this.types.length) {
            throw new TableFormatException("Mismatching header length");
        }
    }

    /**
     * Parses the next line into a typed tuple.
     * 
     * A line contains values for each column of the relation separated by TAB
     * characters.  Each value is parsed according to the type defined for the
     * column in the header.
     * 
     * @return the parsed tuple, or null, if there are no further lines
     * @throws IOException if reading of the line failed
     * @throws TableFormatException if the line has an invalid format
     */
    private Object[] parseNextLine() throws TableFormatException, IOException {
        final String line = this.source.readLine();
        if (line == null) {
            return null;
        }

        final String[] values = line.split("\t");
        if (values.length != this.names.length) {
            throw new TableFormatException(
                    String.format("Length mismatch in line %s",
                    this.source.getLineNumber()));

        }

        final List<Object> parsedValues = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; ++i) {
            final String typename = this.types[i];
            final String value = values[i];
            switch (typename) {
                case "String":
                    parsedValues.add(value.replaceAll("^\"|\"$", ""));
                    break;
                case "Integer":
                    parsedValues.add(Integer.valueOf(value));
                    break;
                case "Double":
                    parsedValues.add(Double.valueOf(value));
                    break;
                case "Float":
                    parsedValues.add(Float.valueOf(value));
                    break;
                default:
                    throw new TableFormatException(
                            String.format("Unknown type: %1", typename));
            }
        }
        return parsedValues.toArray();
    }

    /**
     * Opens the relation.
     * 
     * This method opens the underlying file, and parses its header.
     * 
     * @return the names of the attributes of this table
     * @throws IOException if opening or reading the file failed
     * @throws TableFormatException if the header has an invalid format
     */
    public String[] open() throws IOException, TableFormatException {
        this.close();
        this.source = new LineNumberReader(Files.newBufferedReader(
                this.filepath, Charset.forName("UTF-8")));
        this.parseHeader();
        return this.names;
    }

    /**
     * Gets the next tuple in this relation.
     * 
     * @return the next tuple, or null, if there is no further tuple in the 
     *         relation
     * @throws IOException if reading of the file failed
     * @throws TableFormatException if the line has an invalid format
     */
    @Override
    public Object[] next() throws IOException, TableFormatException {
        if (this.source == null) {
            throw new IOException("Relation is closed");
        }
        return this.parseNextLine();
    }

    /**
     * Closes this relation.
     * 
     * @throws IOException if closing failed
     */
    @Override
    public void close() throws IOException {
        if (this.source != null) {
            this.source.close();
        }
        this.source = null;
    }
    
    /**
     * The path of the underlying file.
     */
    private Path filepath;
    
    /**
     * A reader for the underlying file, while the relation is opened.
     */
    private LineNumberReader source;
    
    /**
     * The types of the relation.
     */
    private String[] types;
    
    /**
     * The attributes of the relation.
     */
    private String[] names;
}
