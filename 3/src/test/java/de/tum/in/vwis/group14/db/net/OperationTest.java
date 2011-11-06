package de.tum.in.vwis.group14.db.net;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class OperationTest {

    private static final byte[] ORDINALS;

    static {
        ORDINALS = new byte[Operation.values().length];
        for (int i = 0; i < ORDINALS.length; ++i) {
            ORDINALS[i] = (byte) Operation.values()[i].ordinal();
        }
    }

    public OperationTest() {
    }

    @Test
    public void testWriteTo() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (final Operation op : Operation.values()) {
            op.writeTo(out);
        }
        assertArrayEquals(ORDINALS, out.toByteArray());
    }

    @Test
    public void testReadFrom() throws IOException, UnknownOperationException {
        final ByteArrayInputStream in = new ByteArrayInputStream(ORDINALS);
        final List<Operation> readOperations = new ArrayList<>();
        Operation op;
        while ((op = Operation.readFrom(in)) != null) {
            readOperations.add(op);
        }
        assertArrayEquals(Operation.values(), readOperations.toArray());
    }

    @Test(expected = UnknownOperationException.class)
    public void testReadFromUnknown() throws IOException,
            UnknownOperationException {
        final Operation[] ops = Operation.values();
        final byte unknownOrdinal = (byte) (ops[ops.length - 1].ordinal() + 10);
        final ByteArrayInputStream in = new ByteArrayInputStream(
                new byte[] { unknownOrdinal });
        Operation.readFrom(in);
    }

    @Test
    public void testReadEndOfStream() throws IOException,
            UnknownOperationException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {});
        assertNull(Operation.readFrom(in));
    }

    @Test
    public void testWriteReadRoundtrip() throws IOException,
            UnknownOperationException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (final Operation op : Operation.values()) {
            op.writeTo(out);
        }
        final ByteArrayInputStream in = new ByteArrayInputStream(
                out.toByteArray());
        final List<Operation> readOperations = new ArrayList<>();
        Operation op;
        while ((op = Operation.readFrom(in)) != null) {
            readOperations.add(op);
        }
        assertArrayEquals(Operation.values(), readOperations.toArray());
    }
}
