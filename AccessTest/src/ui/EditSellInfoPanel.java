package ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import common.DatabaseManager;

@SuppressWarnings("serial")
public class EditSellInfoPanel extends JPanel implements ActionListener{
	
	private MainFrame mMainFrame;
	private JButton btnSearch, btnModify;
	private JTextField tfSearchWord;
	private JComboBox cbSearch;
	private JTable mTable;
	
	public EditSellInfoPanel(MainFrame mainFrame) {
		mMainFrame = mainFrame;
		init();
		
		setLayout(new BorderLayout());
		setVisible(false);
		
		JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(cbSearch);
		top.add(tfSearchWord);
		top.add(btnSearch);
		top.add(btnModify);
		
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
		btnModify = new JButton("저장");
		btnModify.setFocusable(false);
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
		header.addElement("ID");
		header.addElement("구입일");
		header.addElement("고객명");
		header.addElement("수취인");
		header.addElement("전화");
		header.addElement("주문내역");
		header.addElement("주소");
		header.addElement("우편번호");
		header.addElement("비고");
		
		DefaultTableModel model = new DefaultTableModel(header, 0){
			@Override
			public boolean isCellEditable(int row, int column) {
				return (column==1 || column==3 || column==5 || column==8);
			}
		};

			
		mTable = new JTable(model){
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {   
                Component result = super.prepareRenderer(renderer, row, column);   
                if (result instanceof JComponent) {  
                	((JComponent) result).setAlignmentY(CENTER_ALIGNMENT);
                    if(column==1 || column==3 || column==5 || column==8){
                    	((JComponent) result).setBorder(new MatteBorder(1, 1, 1, 1, Color.MAGENTA));  
                    }
                }   
                return result;   
            }    
		};
		
		mTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		mTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		mTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		mTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		mTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		mTable.getColumnModel().getColumn(5).setPreferredWidth(150);
		mTable.getColumnModel().getColumn(6).setPreferredWidth(200);
		mTable.getColumnModel().getColumn(7).setPreferredWidth(60);
		mTable.getColumnModel().getColumn(8).setPreferredWidth(70);
		
		mTable.setFocusable(false);
		
		btnSearch.addActionListener(this);
		btnModify.addActionListener(this);
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
		}else if(obj == btnModify){
			if(mTable.getRowCount() == 0){
				JOptionPane.showMessageDialog(this, "업데이트 할 항목이 없습니다.");
				return;
			}
			int selectedOption = JOptionPane.showConfirmDialog(this, "수정 된 내용을 저장 하시겠습니까?", null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(selectedOption == JOptionPane.OK_OPTION){
				saveModifiedDatas();
			}
		}
	}

	private int[] saveModifiedDatas(){
		int[] result = null;
		String query = 
					"UPDATE SELL_LIST "
				+ 	"SET "
				+ 	"BUY_DATE = ?, "
				+ 	"RECIPIENT = ?, "
				+ 	"JUMUN = ?, "
				+ 	"ETC = ? "
				+ 	"WHERE "
				+ 	"SEQ = ?";
		try {
			DatabaseManager.initDatabase(false);
			DatabaseManager.beginTransaction();
			result = DatabaseManager.excuteUpdate(query, getUpdateDatas());
			DatabaseManager.commitTransaction();
			successUpdateDatas();
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(this, String.format("입력 중 오류가 발생했습니다.[%s]", e1.getMessage()));
			e1.printStackTrace();
			try {
				DatabaseManager.rollbackTransaction();
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		} finally {
			try {
				DatabaseManager.releaseConnection(DatabaseManager.getConnection());
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return result;
	}
	
	private void successUpdateDatas() {
		mTable.clearSelection();
		JOptionPane.showMessageDialog(this, "데이터를 성공적으로 수정했습니다.");
		
		//수정된 데이터 삭제
		while(mTable.getRowCount() > 0){
			((DefaultTableModel)mTable.getModel()).removeRow(0);
		}
	}
	
	private ArrayList<ArrayList<String>> getUpdateDatas() {
		ArrayList<ArrayList<String>> datas = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowData = null;
		for(int i=0; i<mTable.getRowCount(); i++){
			rowData = new ArrayList<String>();
			rowData.add(mTable.getValueAt(i, 1).toString());
			rowData.add(mTable.getValueAt(i, 3).toString());
			rowData.add(mTable.getValueAt(i, 5).toString());
			rowData.add(mTable.getValueAt(i, 8).toString());
			rowData.add(mTable.getValueAt(i, 0).toString());
			
			datas.add(rowData);
		}
		return datas;
	}

	private ResultSet getSearchedData(int selectedIndex){
		String query;
		ResultSet rs = null;
		try {
			if(selectedIndex == 1){//구입건수
				query = "SELECT "
						+ "A.BUY_DATE,C.NAME,A.RECIPIENT,C.PHONE_NO,A.JUMUN,C.ADDRESS,C.POST_CODE,A.ETC,A.SEQ "
						+ "FROM "
						+ "SELL_LIST A, CUSTOMER C, (SELECT COUNT(CUSTOMER_NO) AS SELL_COUNT,CUSTOMER_NO FROM SELL_LIST WHERE DEL_YN != 'Y' GROUP BY CUSTOMER_NO) B "
						+ "WHERE "
						+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.CUSTOMER_NO = C.CUSTOMER_NO AND SELL_COUNT > ? "
						+ "ORDER BY C.CUSTOMER_NO";
			}else if(selectedIndex == 2){//고객명
				query = "SELECT "
						+ "A.BUY_DATE,B.NAME,A.RECIPIENT,B.PHONE_NO,A.JUMUN,B.ADDRESS,B.POST_CODE,A.ETC,A.SEQ "
						+ "FROM SELL_LIST A, CUSTOMER B "
						+ "WHERE "
						+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.DEL_YN != 'Y' AND B.NAME LIKE ? "
						+ "ORDER BY B.CUSTOMER_NO";
			}else{//전화번호
				query = "SELECT "
						+ "A.BUY_DATE,B.NAME,A.RECIPIENT,B.PHONE_NO,A.JUMUN,B.ADDRESS,B.POST_CODE,A.ETC,A.SEQ "
						+ "FROM SELL_LIST A, CUSTOMER B "
						+ "WHERE "
						+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.DEL_YN != 'Y' AND B.PHONE_NO LIKE ? "
						+ "ORDER BY B.CUSTOMER_NO";
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
						rs.getString(9),
						rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8)
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
	
//	public class MyCellRenderer extends JTextField implements TableCellRenderer {
//	 
//	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//	            boolean hasFocus, int row, int column) {
//	    	if(column==1 || column==3 || column==5 || column==8){
//	    		setEditable(true);
//            	setBorder(new MatteBorder(1, 1, 1, 1, Color.RED));  
//            }else{
//            	setEditable(false);
//            }
//	 
//	        return this;
//	    }
//	}
}
