package simpleorm.sessionjdbc;

import java.io.Serializable;

/** Gathers and displays per session statistics. */
public class SStatistics implements Serializable {
   	private static final long serialVersionUID = 20083L;

    SSessionJdbc dataSet;
    
    long nrTransactions=0;
    long nrFlushRecord=0;
    long nrFindInDatabase=0;
    long nrQueryDatabase=0;   
        

    public SStatistics(SSessionJdbc dataSet) {
        this.dataSet = dataSet;
    }

    /** No need to sychronize, as datasets not multi threaded. */
    public void incrementNrTransactions() {nrTransactions++;}
    public void incrementNrFlushRecord(){nrFlushRecord++;}
    public void incrementNrFindInDatabase(){nrFindInDatabase++;}
    public void incrementNrQueryDatabase(){nrQueryDatabase++;}
    
    public String toString() {
        return "[Statistics Trans: " + nrTransactions + " Flush: " + nrFlushRecord + " Finddb: " 
            + nrFindInDatabase + " Querydb: " + nrQueryDatabase + " CurTim: " + System.currentTimeMillis() + "]";
    }
    
    public SSessionJdbc getDataSet() {
        return dataSet;
    }

    public long getNrFindInDatabase() {
        return nrFindInDatabase;
    }

    public long getNrFlushRecord() {
        return nrFlushRecord;
    }

    public long getNrQueryDatabase() {
        return nrQueryDatabase;
    }

    public long getNrTransactions() {
        return nrTransactions;
    }
    
    
}
