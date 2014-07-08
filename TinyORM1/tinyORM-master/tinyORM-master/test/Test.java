import com.tinyorm.Result;


import com.tinyorm.annotation.DBField;
import com.tinyorm.annotation.DBTable;
import com.tinyorm.ResultSet;

@DBTable("t1")
class T1 extends Result {
    
    @DBField private Integer field1;
    @DBField private Integer field2;


}


class T1Set extends ResultSet {

    public T1Set() throws Exception { super(T1.class); }

}

public class Test {
    public static void main (String[] args) {
        Object obj = null;
        try { 
            T1Set t1Set = new T1Set();
            obj = t1Set.getById(1); 
        }
        catch (Exception e) {
            //System.out.println(e);
            e.printStackTrace();
        }
        System.out.println((T1)obj);
    }
}
