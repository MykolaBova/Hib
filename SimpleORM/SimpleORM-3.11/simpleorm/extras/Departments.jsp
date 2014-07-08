<%@page import="simpleorm.dataset.*, simpleorm.sessionjdbc.*, simpleorm.dataset.SFieldFlags.*" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%!
/** This test class defines the Department table */
static public class Department extends SRecordInstance {

//	public Department(boolean attached){
//		super(attached);
//	}
	private static final long serialVersionUID = 1L;

	public static final SRecordMeta<Department> DEPARTMENT 
        = new SRecordMeta(Department.class, "XX_DEPARTMENT");

	// ie. SRecord objects describe SRecordInstances

	public static final SFieldString DEPT_ID 
        = new SFieldString(DEPARTMENT, "DEPT_ID", 10, SFieldFlags.SPRIMARY_KEY);

	public static final SFieldString NAME 
        = new SFieldString(DEPARTMENT, "DNAME", 40, SFieldFlags.SDESCRIPTIVE);

	public static final SFieldString MISSION 
        = new SFieldString(DEPARTMENT, "MISSION", 40);

	public static final SFieldDouble BUDGET = new SFieldDouble(DEPARTMENT, "BUDGET");

	public static final SFieldDouble MAX_SALARY 
        = new SFieldDouble(DEPARTMENT, "MAX_SALARY").setInitialValue(new Double(8999));

	public @Override() SRecordMeta<Department> getMeta() {
		return DEPARTMENT;
	};
}
%>

<html>
<body>
  <h1>JSPs directly accessing SimpleORM records</h1>
  <%
      	Class.forName("org.hsqldb.jdbcDriver");
    	java.sql.Connection con = java.sql.DriverManager.getConnection("jdbc:hsqldb:hsqlTempFiles;shutdown=true;", "sa", "");
		SSessionJdbc ses = SSessionJdbc.open(con, "Tiny");
		
		ses.begin();
		ses.getDriver().dropTableNoError("XX_DEPARTMENT");
		ses.commit();
		
		ses.begin();
        ses.rawUpdateDB(ses.getDriver().createTableSQL(Department.DEPARTMENT));
		ses.commit();
		
		ses.begin();
		SDataLoader deptDL = new SDataLoader(ses, Department.DEPARTMENT);
		deptDL.insertRecords(new Object[][] {
				{ "100", "One00", "Count Pennies", 10000, 200000 },
				{ "200", "Two00", "Be Happy", 20000, 300000 },
				{ "300", "Three00", "Enjoy Life", 30000, 150000 } });
		ses.commit();

		ses.begin();
		SQueryResult<Department> res  = ses.query(new SQuery(Department.DEPARTMENT));
		//SLog.getSessionlessLogger().message("Departments: " + res);		

		pageContext.setAttribute("Depts", res);
		
  %>
  
  Scriptlet:<br>
  <%= res %>
  
  <h2>Departments</h2>	

  As JSTL tags (\${dept.DEPT_ID} etc.)
  <p>
  <c:forEach items="${Depts}" var="dept">
    Dept: ${dept.DEPT_ID}: ${dept["DNAME"]}<br>
  </c:forEach>
  
  
  <%
		ses.commit();
		ses.close();
  %>
  
</body>
</html>