   package simpleorm.examples;
 
   /**
 * Just runs all the other tests, one after the other.
 * 
 * This is done as a java class rather than as an ant task so that it will work
 * easily when called from an IDE such as Eclipse.
 */
public class ATestAll {

	public static void main(String[] args) throws Exception {
		TestUte.systemProperties.setProperty("trace.level", "10");
        ADemo.doPlugin = false;
		ADemo.main(args);
		BasicTests.main(args);
		CompareTest.main(args);
        QueryTests.main(args);
        JoinQueryTests.main(args);
        QueryTransientTests.main(args);
		CreateDBTest.main(args);
		ColumnCacheTest.main(args);
		DataTypesTest.main(args);
		System.out.println("WARNING : IdentForeignKeys are not possible anymore");
		//IdentFKeysTest.main(args);
		ReferenceTest.main(args);
		GeneratedKeyTest.main(args);
		ValidationTest.main(args);
		LongTransactionTest.main(args);
        SubTypeTest.main(args);
		Benchmarks.main(args);
		System.err.println("==== All Done ====");
	}
}
