import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:3306/orm";
	String db_id = "root";
	String db_pw = "";

	public void create(UserVO vo) throws Exception {
		Class.forName(driver);

		Connection c = DriverManager.getConnection(url, db_id, db_pw);

		PreparedStatement pstmt = null;

		// String sql = "insert into member(id, pw, name) values ('" +
		// vo.getId()
		// + "','" + vo.getPw() + "','" + vo.getName() + "')"; // 쿼리문 끝에
		// 세미콜론을 넣으면
		// 안된다.

		String sql = "insert into member(id, pw, name) values (?,?,?)";

		pstmt = c.prepareStatement(sql);

		pstmt.setString(1, vo.getId());
		pstmt.setString(2, vo.getPw());
		pstmt.setString(3, vo.getName());

		pstmt.execute();

		// st = c.createStatement();
		//
		// st.executeUpdate(sql);
		//
		// st.close();
		pstmt.close();
		c.close();

	}

	public UserVO read(String id) throws Exception {
		UserVO result = null;

		Class.forName(driver);

		Connection c = DriverManager.getConnection(url, db_id, db_pw);

		// Statement st = null;
		PreparedStatement pstmt = null;

		String query = "SELECT ID, PW, NAME FROM MEMBER WHERE ID = ?";

		pstmt = c.prepareStatement(query);

		pstmt.setString(1, id);

		ResultSet rs = pstmt.executeQuery();

		rs.next();

		if (rs.getRow() == 0) {
			return null;
		}

		result = new UserVO();
		result.setId(rs.getString(1));
		result.setPw(rs.getString(2));
		result.setName(rs.getString(3));
		System.out.println(result.toString());

		rs.close();
		pstmt.close();
		c.close();

		return result;
	}

	public void update(UserVO vo) throws Exception {
		Class.forName(driver);

		Connection c = DriverManager.getConnection(url, db_id, db_pw);

		PreparedStatement pstmt = null;

		String query = "UPDATE MEMBER SET PW=?,NAME=? WHERE ID = ?";
		pstmt = c.prepareStatement(query);

		pstmt.setString(1, vo.getPw());
		pstmt.setString(2, vo.getName());
		pstmt.setString(3, vo.getId());

		pstmt.execute();

		pstmt.close();
		c.close();
	}

	public void delete(String id) throws Exception {
		Class.forName(driver);

		Connection c = DriverManager.getConnection(url, db_id, db_pw);

		// Statement st = null;
		PreparedStatement pstmt = null;

		String query = "DELETE FROM MEMBER WHERE ID = ?";

		pstmt = c.prepareStatement(query);

		pstmt.setString(1, id);

		pstmt.execute();

		// st = c.createStatement();

		// st.execute(query);

		pstmt.close();
		c.close();
	}

	public List<UserVO> listAll() throws Exception {
		List<UserVO> result = new ArrayList<UserVO>();

		UserVO vo = null;

		Class.forName(driver);

		Connection c = DriverManager.getConnection(url, db_id, db_pw);

		PreparedStatement pstmt = null;

		String query = "SELECT ID, PW, NAME FROM MEMBER";

		pstmt = c.prepareStatement(query);

		ResultSet rs = pstmt.executeQuery();

		while (rs.next()) {
			vo = new UserVO();
			vo.setId(rs.getString(1));
			vo.setPw(rs.getString(2));
			vo.setName(rs.getString(3));

			result.add(vo);
		}

		rs.close();
		pstmt.close();
		c.close();

		return result;
	}
}
