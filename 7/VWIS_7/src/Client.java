import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.derby.jdbc.EmbeddedXADataSource;

public class Client {

	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	String createMietspiegel = "CREATE TABLE mietspiegel  "
			+ "(ort VARCHAR(50) NOT NULL," + " miete INTEGER NOT NULL) ";
	String createKindergarten = "CREATE TABLE kindergarten  "
			+ "(ort VARCHAR(50) NOT NULL," + " beitrag INTEGER NOT NULL) ";

	private String dbName;
	public Connection conn;
	public EmbeddedXADataSource ds;
	public XAResource res;

	public Client(String dbName) {
		this.dbName = dbName;
	}

	private void createMietspiegel() {
		try {
			conn.createStatement().executeUpdate("DROP TABLE mietspiegel");
			conn.createStatement().executeUpdate(createMietspiegel);
			conn.createStatement()
					.executeUpdate(
							"insert into mietspiegel (ort,miete) values ('Berlin', 1200)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createKindergarten() {
		try {
			conn.createStatement().executeUpdate("DROP TABLE kindergarten");
			conn.createStatement().executeUpdate(createKindergarten);
			conn.createStatement()
					.executeUpdate(
							"insert into kindergarten (ort,beitrag) values ('Berlin', 100)");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void connect() {
		try {
			ds = new EmbeddedXADataSource();
			ds.setCreateDatabase("create");
			ds.setDatabaseName(dbName);
			res = ds.getXAConnection().getXAResource();
			conn = ds.getXAConnection().getConnection();
			if (dbName.equals("mietspiegel")) {
				createMietspiegel();
			} else {
				createKindergarten();
			}
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void print() throws SQLException {
		System.out.println("Printing " + dbName + " table");
		Statement statement = conn.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM "
				+ dbName);
		int columnCnt = resultSet.getMetaData().getColumnCount();
		boolean shouldCreateTable = true;
		while (resultSet.next() && shouldCreateTable) {
			for (int i = 1; i <= columnCnt; i++) {
				System.out.print(resultSet.getString(i) + " ");
			}
			System.out.println();
		}
		resultSet.close();
		statement.close();
	}

	public static void main(String[] args) throws XAException, SQLException {
		Client c1 = new Client("mietspiegel");
		c1.connect();
		Client c2 = new Client("kindergarten");
		c2.connect();

		MyXid xid1 = new MyXid(new byte[] { 0x11 }, 1, new byte[] { 0x12 });
		MyXid xid2 = new MyXid(new byte[] { 0x21 }, 2, new byte[] { 0x22 });

		c1.res.start(xid1, XAResource.TMNOFLAGS);
		c2.res.start(xid2, XAResource.TMNOFLAGS);
		int i1 = c1.conn
				.createStatement()
				.executeUpdate(
						"insert into mietspiegel (ort,miete) values ('Wallenfels', 300)");
		int i2 = c2.conn
				.createStatement()
				.executeUpdate(
						"insert into kindergarten (ort,beitrag) values ('Wallenfels', 20)");
		c1.res.end(xid1, i1 > 0 ? XAResource.TMSUCCESS : XAResource.TMFAIL);
		c2.res.end(xid2, i2 > 0 ? XAResource.TMSUCCESS : XAResource.TMFAIL);
		try {
			System.out.println("prepare 1");
			int r1 = c1.res.prepare(xid1);
			System.out.println("prepare 2");
			int r2 = c2.res.prepare(xid2);
			if (r1 == XAResource.XA_OK && r2 == XAResource.XA_OK) {
				System.out.println("commit both");
				c1.res.commit(xid1, false);
				c2.res.commit(xid2, false);
			}
		} catch (XAException e) {
			System.out.println("rollback both");
			c1.res.rollback(xid1);
			c2.res.rollback(xid2);
		}
		c1.print();
		c2.print();
	}

	static class MyXid implements Xid {

		private byte[] branch;
		private int format;
		private byte[] trans;

		public MyXid(byte[] branch, int format, byte[] trans) {
			this.branch = branch;
			this.format = format;
			this.trans = trans;
		}

		@Override
		public byte[] getBranchQualifier() {
			return branch;
		}

		@Override
		public int getFormatId() {
			return format;
		}

		@Override
		public byte[] getGlobalTransactionId() {
			return trans;
		}

	}

}
