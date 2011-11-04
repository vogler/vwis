package de.tum.in.vwis.group14.db;

import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import org.junit.Rule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * Testcase for tablescan
 */
public class TablescanTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    public TablescanTest() {
    }

    @Before
    public void setUp() throws IOException {
        final URL resource = this.getClass().getResource("/test_relation");
        this.relationFileName = FileSystems.getDefault().getPath(
                this.testFolder.getRoot().getPath(), "test_relation");
        try (final InputStream source = resource.openStream()) {
            Files.copy(source, this.relationFileName);
        }
        this.relation = new Tablescan(this.relationFileName);

    }
    
    @After
    public void tearDown() throws IOException {
        this.relation.close();
    }

    @Test
    public void testOpen() throws Exception {
        assertArrayEquals(NAMES, this.relation.open()); 
        // test re-opening
        assertArrayEquals(NAMES, this.relation.open());
    }

    @Test(expected = TableFormatException.class)
    public void openEmptyFile() throws Exception {
        this.relation = new Tablescan(
                this.testFolder.newFile("empty_file").getPath());
        this.relation.open();
    }

    @Test
    public void testEmptyRelation() throws Exception {
        final Charset utf8 = Charset.forName("UTF-8");
        final List<String> lines = Files.readAllLines(
                this.relationFileName, utf8);
        final Path filename = FileSystems.getDefault().getPath(
                this.testFolder.getRoot().getPath(), "empty_relation");
        try (final BufferedWriter sink =
                        Files.newBufferedWriter(filename, utf8)) {
            sink.write(lines.get(0));
            sink.write("\n");
            sink.write(lines.get(1));
            sink.write("\n");
        }
        this.relation = new Tablescan(filename);
        assertArrayEquals(this.relation.open(), NAMES);
        assertNull(this.relation.next());
    }

    @Test
    public void testNext() throws Exception {
        this.relation.open();
        assertArrayEquals(FIRST_ROW, this.relation.next());
        assertArrayEquals(SECOND_ROW, this.relation.next());
        assertNull(this.relation.next());
    }

    @Test(expected = IOException.class)
    public void testNextBeforeOpen() throws Exception {
        this.relation.next();
    }

    @Test
    public void testNextReopen() throws Exception {
        this.relation.open();
        assertArrayEquals(FIRST_ROW, this.relation.next());
        assertArrayEquals(SECOND_ROW, this.relation.next());
        this.relation.open();
        assertArrayEquals(FIRST_ROW, this.relation.next());
    }

    @Test
    public void testTypes() throws Exception {
        this.relation.open();
        final Object[] first = this.relation.next();
        assertThat(first[0], instanceOf(Integer.class));
        assertThat(first[1], instanceOf(String.class));
        assertThat(first[2], instanceOf(Float.class));
        assertThat(first[3], instanceOf(Double.class));
    }

    @Test
    public void testClose() throws Exception {
        this.relation.close();
        try {
            this.relation.next();
            fail("next after close succeeded");
        } catch (IOException error) {
        }
    }
    private Path relationFileName;
    private Tablescan relation;
    private final static String[] NAMES =
            new String[]{"intcol", "stringcol", "floatcol", "doublecol"};
    private final static Object[] FIRST_ROW = new Object[]{
        1, "foo", 1.55f, 10e6d};
    private final static Object[] SECOND_ROW = new Object[]{
        2, "bar", 2.55f, .5d};
}
