
import org.junit.Before;
import org.junit.Test;


public class UserDAOTest {
	
	UserVO vo = null;
	UserDAO dao = null;
	
	@Before
	public void init() {
		dao = new UserDAO();
		vo = new UserVO();
		vo.setId("whatdoesthefoxsay");
		vo.setPw("wtf");
		vo.setName("Flowkater");
	}
	
	@Test
	public void createTest() throws Exception{
		dao.create(vo);
	}
	
	@Test
	public void readTest() throws Exception{
		dao.read(vo.getId());
	}
	
	@Test
	public void updateTest() throws Exception{
		dao.update(vo);
	}
	
	@Test
	public void deleteTest() throws Exception{
		dao.delete(vo.getId());
	}
	
	@Test
	public void listAllTest() throws Exception{
		dao.listAll();
	}

}
