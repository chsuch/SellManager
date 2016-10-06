package ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import common.DatabaseManager;


public class SearchCustomerPanel extends JPanel implements ActionListener{
	
	private MainFrame mMainFrame;
	private JButton btnSearch;
	private JTextField tfSearchWord;
	private JComboBox cbSearch;
	private JTable mTable;
	
	public SearchCustomerPanel(MainFrame mainFrame) {
		mMainFrame = mainFrame;
		init();
		
		setLayout(new BorderLayout());
		setVisible(false);
		
		JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(cbSearch);
		top.add(tfSearchWord);
		top.add(btnSearch);
		
		add(top, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(mTable);
		scroll.setPreferredSize(new Dimension(800, 550));
		add(scroll, BorderLayout.CENTER);
		
		
	}
	
	@Override
	public void setVisible(boolean aFlag) {
		super.setVisible(aFlag);
		if(aFlag){
			tfSearchWord.requestFocus();
		}
	};
	
	private void init() {
		btnSearch = new JButton("검색");
		btnSearch.setFocusable(false);
		
		tfSearchWord = new JTextField(10);
		tfSearchWord.setToolTipText("검색어");
		tfSearchWord.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					btnSearch.doClick();
				}
			}
		});
		
		cbSearch = new JComboBox(new String[]{"--검색조건--","구입건수","고객명","전화번호"});
		cbSearch.setFocusable(false);
		
		Vector<String> header = new Vector<String>();
		header.addElement("고객번호");
		header.addElement("구입횟수");
		header.addElement("고객명");
		header.addElement("전화번호");
		header.addElement("주소");
		header.addElement("우편번호");
		header.addElement("고객등록일");
		
		DefaultTableModel model = new DefaultTableModel(header, 0){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		mTable = new JTable(model);
		mTable.setFocusable(false);
		
		mTable.getColumnModel().getColumn(0).setPreferredWidth(70);//고객번호
		mTable.getColumnModel().getColumn(1).setPreferredWidth(70);//구입횟수
		mTable.getColumnModel().getColumn(2).setPreferredWidth(80);//고객명
		mTable.getColumnModel().getColumn(3).setPreferredWidth(100);//전화번호
		mTable.getColumnModel().getColumn(4).setPreferredWidth(250);//주소
		mTable.getColumnModel().getColumn(5).setPreferredWidth(80);//우편번호
		mTable.getColumnModel().getColumn(6).setPreferredWidth(150);//고객등록일
		
		btnSearch.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj == btnSearch){
			if(cbSearch.getSelectedIndex()==0){
				JOptionPane.showMessageDialog(this, "검색 조건을 선택하세요.");
				return;
			}else if(cbSearch.getSelectedIndex()==1){
				try {
					Integer.parseInt(tfSearchWord.getText().trim());
					setDataList(getSearchedData(cbSearch.getSelectedIndex()));
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(this, "검색어는 숫자로 입력하세요.");
				}
			}else if(tfSearchWord.getText().trim().equals("")){
				JOptionPane.showMessageDialog(this, "검색어를 입력하세요.");
				return;
			}else{//TABLE_SELL_LIST
				setDataList(getSearchedData(cbSearch.getSelectedIndex()));
			}
		}
	}

	private ResultSet getSearchedData(int selectedIndex){
		String query;
		ResultSet rs = null;
		try {
			if(selectedIndex == 1){//구입건수
				query = "SELECT "
						+ "A.CUSTOMER_NO, B.SELL_COUNT, A.NAME, A.PHONE_NO, A.ADDRESS, A.POST_CODE, A.UPDATE_TIME "
						+ "FROM "
						+ "CUSTOMER A, (SELECT COUNT(CUSTOMER_NO) AS SELL_COUNT,CUSTOMER_NO FROM SELL_LIST WHERE DEL_YN != 'Y' GROUP BY CUSTOMER_NO) B "
						+ "WHERE "
						+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.DEL_YN != 'Y' AND B.SELL_COUNT > ? "
						+ "ORDER BY A.CUSTOMER_NO";
			}else if(selectedIndex == 2){//고객명
				query = "SELECT "
						+ "A.CUSTOMER_NO, B.SELL_COUNT, A.NAME, A.PHONE_NO, A.ADDRESS, A.POST_CODE, A.UPDATE_TIME "
						+ "FROM "
						+ "CUSTOMER A, (SELECT COUNT(CUSTOMER_NO) AS SELL_COUNT,CUSTOMER_NO FROM SELL_LIST WHERE DEL_YN != 'Y' GROUP BY CUSTOMER_NO) B "
						+ "WHERE "
						+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.DEL_YN != 'Y' AND A.NAME LIKE ? "
						+ "ORDER BY A.CUSTOMER_NO";
			}else{//전화번호
				query = "SELECT "
						+ "A.CUSTOMER_NO, B.SELL_COUNT, A.NAME, A.PHONE_NO, A.ADDRESS, A.POST_CODE, A.UPDATE_TIME "
						+ "FROM "
						+ "CUSTOMER A, (SELECT COUNT(CUSTOMER_NO) AS SELL_COUNT,CUSTOMER_NO FROM SELL_LIST WHERE DEL_YN != 'Y' GROUP BY CUSTOMER_NO) B "
						+ "WHERE "
						+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.DEL_YN != 'Y' AND A.PHONE_NO LIKE ? "
						+ "ORDER BY A.CUSTOMER_NO";
			}
			DatabaseManager.initDatabase(false);
			rs = DatabaseManager.excuteQuery(query, new String[]{tfSearchWord.getText().trim()});
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				DatabaseManager.releaseConnection(DatabaseManager.getConnection());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rs;
	}
	
	
	private void setDataList(ResultSet rs){
		while(mTable.getRowCount() > 0){
			((DefaultTableModel)mTable.getModel()).removeRow(0);
		}
		int count = 0;
		try {
			while(rs.next()){
				count++;
				((DefaultTableModel)mTable.getModel()).addRow(new String[]
					{
						rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7)
					}
				);
			}
			if(count == 0){
				JOptionPane.showMessageDialog(this, "검색된 데이터가 없습니다.");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void onClickSearchBtn(){
		btnSearch.doClick();
	}
}
