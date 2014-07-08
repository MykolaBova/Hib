package simpleorm.quickstart;

/**Used to store information about a parameter.<p>
 * Examples<br>
 * paramName   = empleeID<br>
 * paramType   = int<br>
 * methodParam = int empleeID<br>
 * objectParam = new Integer( employID)<p>
 *
 * paramName   = department<br>
 * paramType   = Department<br>
 * methodParam = Department department<br>
 * objectParam = department<br>
 */
public class AParam {
	public String paramName;
	public String paramType;	
	public String methodParam;
	public String objectParam;

	public AParam(String name, String type, String method, String object) {
		paramName  = name;
		paramType = type;
		methodParam = method;
		objectParam = object;
	}
}