package vwis.server;

public class Send extends SimpleServerProxy {

    public Send(String tableName) {
        super(tableName);
    }

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    @Override
    protected void handleCommand(Command i) throws Exception {
        switch (i) {
        case OPEN:
            this.out.writeObject(this.tablescan.open());
            break;
        case CLOSE:
            this.tablescan.close();
            break;
        case NEXT:
            int number = this.in.readInt();
            for (int j = 0; j < number; j++) {
                Object[] t = this.tablescan.next();
                this.out.writeObject(t);
                // stop sending once no more tuples are available
                if (t == null) {
                    return;
                }
            }
            break;
        }
    }

}
