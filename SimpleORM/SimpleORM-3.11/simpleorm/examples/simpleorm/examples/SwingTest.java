package simpleorm.examples;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import simpleorm.dataset.SQuery;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;

/**
 * Demo of SimpleORM in a Swing Environment. Currently mainly just plays with
 * Swing classes, but will be a generalized interface for editing any tables
 * data. The tricky part is foreign keys and lookups.
 * <p>
 * 
 * Also note that the nasty way that Swing handles threads complicates the
 * common case of a single threaded program. In the normal pattern, the main
 * thread sets up the forms, and then exits. A second thread then does the work.
 * A single threaded application using two threads! This application uses
 * <code>invokeAndWait()</code> to do all the work in the Swing thread, which
 * is much cleaner.
 * <p>
 * 
 */

public class SwingTest { //implements SUIConstants {

	static JFrame frame = null;

	static ThreadLocal tlocal = new ThreadLocal();

	public static void main(String[] argv) throws Exception {
		tlocal.set("MainLocal");
		Runnable swing = new Runnable() {
			public void run() {
				try {
					doMain();
				} catch (Exception ex) {
					throw new SException.Test(ex);
				}
			}
		};
		SwingUtilities.invokeAndWait(swing);
		// Makes all the SimpleORM work happen in the Swing thread.
		System.out.println("Exiting main thread..." + Thread.currentThread()
				+ tlocal.get());
	}

    static SSessionJdbc session;
	/** This executes in the Swing thread, so SOrm connections work nicely. */
	static void doMain() throws Exception {
		tlocal.set("SwingLocal");
		session = TestUte.initializeTest(SwingTest.class);
		// #### TestUte.createDeptEmp();
		session.begin();
		editDept();
		session.commit();
		session.begin();
		System.out.println("Exiting runnable..." + Thread.currentThread()
				+ tlocal.get());
	}

	static void editDept() throws Exception {
		
		frame = new JFrame();
		frame.setTitle("Swing SimpleORM Tester");

		/*
		 * Simple Table Object [][] ddata = readDeptData(); String [] headings =
		 * {"Dept ID", "Name"}; final JTable jtable = new JTable(ddata,
		 * headings); JScrollPane scrollPane = new JScrollPane(jtable);
		 * scrollPane.setVerticalScrollBarPolicy(
		 * ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS); JPanel jpanel = new
		 * JPanel(); jpanel.setLayout(new BorderLayout()); // Necessary for
		 * scrolling jpanel.add(scrollPane, BorderLayout.CENTER);
		 * frame.getContentPane().add(jpanel);
		 */

		frame.setJMenuBar(makeMenu());

		JComponent panel = makeTabs();

		// Add the top panel to the frame and start execution
		frame.getContentPane().add(panel);
		// frame.getContentPane().add(new JLabel("WestLab"), BorderLayout.WEST);

		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
				ses.commit();
				SSessionJdbc.getThreadLocalSession().close(); // Need listener.
				System.exit(0); // #### Ugly, but how else to stop the thread?
								// stop()?
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	/** Menu not currently used. */
	static JMenuBar makeMenu() throws Exception {
		JMenuBar jmb = new JMenuBar();
		JMenu file = new JMenu("UnusedMenu");
		jmb.add(file);
		JMenuItem item = new JMenuItem("SillyItem");
		file.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Menu " + e.getActionCommand());
			}
		});
		return jmb;
	}

	static JComponent makeTabs() throws Exception {
		// Tabs
		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("List Depts", null, deptsTab());

		tabbedPane.addTab("Play", null, playTab());

		JPanel second = new GraphicsTab();
		second.addMouseListener(new MListener());
		tabbedPane.addTab("Graphics", null, second);
		tabbedPane.setSelectedIndex(0);
		return tabbedPane;
	}

	public static JComponent deptsTab() {

		final DeptTableModel tModel = new DeptTableModel();
		final JTable jtable = new JTable(tModel);
		// jtable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		JScrollPane scrollPane = new JScrollPane(jtable);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = jtable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					System.out.println("No Row Selected.");
				} else {
					int selectedRow = lsm.getMinSelectionIndex(); // 0 is
																	// first
					System.out.println("Row " + selectedRow + " Selected ");
					showDeptDialog(tModel.getValueAt(selectedRow, 0));
					// throw new RuntimeException(); // Ignored!
				}
			}
		});
		return scrollPane;
	}

	static class DeptTableModel extends javax.swing.table.AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Object[][] ddata = readDeptData();

		String deptIdLab = "Department Id";//Department.DEPT_ID.getString(SUI_PROMPT);

		String deptNameLab = "Department Label"; //Department.NAME.getString(SUI_PROMPT);

		String[] headings = { deptIdLab, deptNameLab };

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return ddata.length;
		}

		public String getColumnName(int col) {
			return headings[col];
		}

		public Object getValueAt(int row, int col) {
			return ddata[row][col];
		}

		/** Read all the Departments(Id, Name) from the database. */
		public static Object[][] readDeptData() {
			ArrayList ddata = new ArrayList();
			// / Prepare and execute the query
			List<Department> res = session.query(new SQuery<Department>(Department.DEPARTMENT).ascending(Department.NAME));

			// / loop through the results, adding up the budgets.
			for (Department dept : res) {
				Object[] rec = { dept.getString(Department.DEPT_ID),
						dept.getString(Department.NAME) };
				ddata.add(rec);
			}
			Object[][] darray = (Object[][]) ddata.toArray(new Object[0][0]);
			return darray;
		}
	} // DeptTableModel

	/** Called when the user selects a row. Non-Modal, but only one shown. */
	static void showDeptDialog(Object key) {
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		System.out.println("DeptDialog..." + Thread.currentThread()
				+ tlocal.get());
		Department dept = ses.findOrCreate(Department.DEPARTMENT, (String) key);

		if (jdialog == null) {
			jdialog = new JDialog(frame, "My Dialog", false); // modalality
		}
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new GridLayout(0, 2));
		String deptIdLab = "Dept Id";//Department.DEPT_ID.getString(SUI_PROMPT);
		jpanel.add(new JLabel(deptIdLab));
		jpanel.add(new JLabel(dept.getString(dept.DEPT_ID)));
		String missionLab = "Mission";//Department.MISSION.getString(SUI_PROMPT);
		jpanel.add(new JLabel(missionLab));
		JTextField mission = new JTextField(30);
		mission.setText(dept.getString(dept.MISSION));
		jpanel.add(mission);

		jdialog.getContentPane().add(jpanel);
		jdialog.pack();
		jdialog.setLocationRelativeTo(frame);
		jdialog.setVisible(true); // Display
	}

	static JDialog jdialog = null;

	public static JPanel playTab() {
		JPanel panel = new JPanel(); // Needed if more than one item
		panel.add(new JLabel("Sliders Share Model"));
		GridBagLayout gbl = new GridBagLayout();
		panel.setLayout(gbl);

		// / Label, Field and Button
		final JLabel label = new JLabel(
				"<html><font color=red>Hello</font> World</html>", JLabel.RIGHT);
		// </html> must be last -- appending "!" resets.
		label.setAlignmentX(Component.RIGHT_ALIGNMENT); // Don't work.
		GridBagConstraints labelc = new GridBagConstraints();
		labelc.anchor = labelc.WEST; // No good either.
		gbl.setConstraints(label, labelc);
		panel.add(label);

		final JTextField field = new JTextField(20);
		GridBagConstraints fieldc = new GridBagConstraints();
		gbl.setConstraints(label, fieldc);
		panel.add(field);

		final JButton button = new JButton("Add !!");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				label.setText(field.getText() + "!!");
				// JOptionPane.showMessageDialog(frame, "Pressed!");
			}
		});
		GridBagConstraints buttonc = new GridBagConstraints();
		buttonc.gridheight = 2;
		gbl.setConstraints(button, buttonc);
		panel.add(button);
		label.setLabelFor(button); // For ADO

		// / Two sliders, one model.
		BoundedRangeModel brm = new DefaultBoundedRangeModel(3, 2, 0, 10);

		final JSlider slider1 = new JSlider(); // sliderModel);
		slider1.setModel(brm);
		GridBagConstraints slider1c = new GridBagConstraints();
		slider1c.gridy = 1;
		slider1c.gridx = 0;
		slider1c.gridwidth = 2;
		gbl.setConstraints(slider1, slider1c);
		panel.add(slider1);

		final JSlider slider2 = new JSlider(); // sliderModel);
		slider2.setModel(brm);
		GridBagConstraints slider2c = new GridBagConstraints();
		slider2c.gridy = 1;
		slider2c.gridwidth = 1;
		gbl.setConstraints(slider2, slider2c);
		panel.add(slider2);

		// / Set a border for fun
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Named Border"), BorderFactory
				.createEmptyBorder(30, 30, 30, 30))); // top left bottom right

		return panel;
	}

	/**
	 * This second tab is just used to play with graphic objects for now. Later
	 * it will be used to do non-Form database I/O.
	 */
	static class GraphicsTab extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g); // paint background

			g.setColor(Color.yellow); // For the next graphic!
			g.fillRect(5, 5, 200, 20);

			Graphics2D g2 = (Graphics2D) g;
			GradientPaint redtowhite = new GradientPaint(5, 20, Color.red, 200,
					100, Color.white);
			g2.setPaint(redtowhite);
			g2.fill(new Ellipse2D.Double(5, 20, 200, 100));
		}
	}

	static class MListener implements MouseListener {
		public void mousePressed(MouseEvent e) {
			saySomething("Mouse pressed (# of clicks: " + e.getClickCount()
					+ ")", e);
		}

		public void mouseReleased(MouseEvent e) {
			saySomething("Mouse released (# of clicks: " + e.getClickCount()
					+ ")", e);
		}

		public void mouseEntered(MouseEvent e) {
			saySomething("Mouse entered", e);
		}

		public void mouseExited(MouseEvent e) {
			saySomething("Mouse exited", e);
		}

		public void mouseClicked(MouseEvent e) {
			saySomething("Mouse clicked (# of clicks: " + e.getClickCount()
					+ ")", e);
		}

		void saySomething(String eventDescription, MouseEvent e) {
			System.out.println(eventDescription + " detected on "
					+ e.getComponent().getClass().getName());
		}
	}
}
