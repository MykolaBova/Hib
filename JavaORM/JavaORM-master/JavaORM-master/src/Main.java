import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Main {
	public static void main(String[] args) {
		try {
			
			// mysql jdbc library 를 다운받아야 된다.
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://172.16.101.160:3306/orm","root", "gkqazx");
			
			Statement stmt = c.createStatement();
			
//			String sql = "insert into member(id, pw, uname) values ('JaewooId','WTF','Flowkater')"; // 쿼리문 끝에 세미콜론을 넣으면 안된다.
			
//			stmt.executeUpdate(sql); // 쿼리문에 insert
			
			String sql = "SELECT * FROM MEMBER WHERE ID = 'JaewooId'";
			
			ResultSet rs = stmt.executeQuery(sql);
			
			rs.next(); // 처음에 커서가 BOF 를 가리킨다. 
			
			System.out.println(rs.getString(1)); 
			System.out.println(rs.getString(2));
			System.out.println(rs.getString(3));
			
			rs.close();
			stmt.close();
			c.close();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
