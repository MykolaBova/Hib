package EJB;

/**
 * A small example bean method.
 * 
 * (This is an unsupported outline.  But Dan H got it all to work.)
 */
public class EJBTest {

	public static void main(String[] args) {

		public void testMethod() throws Exception
		{
		     DatabaseORM db = baseServiceBean.getDatabaseORM();
		     
		     // We now have an SConnection associated with the current Transaction
		     // which is accessible in the normal way.
		     // eg. we can call findOrCreate etc.
		     
		     CustomerRaw cust = (CustomerRaw) db.create(new CustomerRaw());
		     cust.setFIRSTNAMERaw("Test1");
		     cust.setLASTNAMERaw("Last1");
		     cust.setEMAILRaw("test@yahoo.com");
		}

	}
}
