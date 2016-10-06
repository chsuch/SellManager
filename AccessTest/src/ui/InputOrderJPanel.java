package ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import common.Constants;
import common.DatabaseManager;


public class InputOrderJPanel extends JPanel implements ActionListener{
	
	private MainFrame mMainFrame;
	private JButton btnInput, btnAddrow, btnExcel, btnClearAll, btnDelete;
	private JFileChooser jfc;
	/**
	 * 0:구입일
	 * 1:주문인
	 * 2:수취인
	 * 3:전화번호
	 * 4:주문내역
	 * 5:주소
	 * 6:우편번호
	 * 7:비고 
	 */
	private JTable mTable;
	
	public InputOrderJPanel(MainFrame mainFrame) {
		mMainFrame = mainFrame;
		jfc = new JFileChooser();
		init();
		
		setLayout(new BorderLayout());
		setVisible(false);
		
		JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(btnInput);
		top.add(btnAddrow);
		top.add(btnExcel);
		top.add(btnClearAll);
		top.add(btnDelete);
		add(top, BorderLayout.NORTH);
		
		JScrollPane scroll = new JScrollPane(mTable);
		scroll.setPreferredSize(new Dimension(800, 550));
		add(scroll, BorderLayout.CENTER);
	}
	
	private void init() {
		Vector<String> header = new Vector<String>();
		header.addElement("No");
		header.addElement("구입일");
		header.addElement("주문인");
		header.addElement("수취인");
		header.addElement("전화");
		header.addElement("주문내역");
		header.addElement("주소");
		header.addElement("우편번호");
		header.addElement("비고");
		
		DefaultTableModel model = new DefaultTableModel(header, 0);
		
		btnInput = new JButton("저장");
		btnAddrow = new JButton("한줄추가");
		btnExcel = new JButton("Excel");
		btnClearAll = new JButton("초기화");
		btnDelete = new JButton("삭제");
		
		btnInput.setToolTipText("작성된 데이터를 DB에 저장합니다.");
		btnAddrow.setToolTipText("새로 작성 할 행을 추가합니다.");
		btnExcel.setToolTipText("엑셀 파일로 부터 작성 할 데이터를 불러옵니다.");
		btnClearAll.setToolTipText("작성된 데이터를 전부 삭제합니다.");
		btnDelete.setToolTipText("선택된 행을 삭제 합니다.");
		
		mTable = new JTable(model);
		mTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		mTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		mTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		mTable.getColumnModel().getColumn(3).setPreferredWidth(50);
		mTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		mTable.getColumnModel().getColumn(5).setPreferredWidth(150);
		mTable.getColumnModel().getColumn(6).setPreferredWidth(200);
		mTable.getColumnModel().getColumn(7).setPreferredWidth(60);
		mTable.getColumnModel().getColumn(8).setPreferredWidth(70);
		
		btnInput.addActionListener(this);
		btnAddrow.addActionListener(this);
		btnExcel.addActionListener(this);
		btnClearAll.addActionListener(this);
		btnDelete.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj == btnInput){
			int selectedOption = JOptionPane.showConfirmDialog(this, "작성된 데이터를 저장 하시겠습니까?", null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(selectedOption == JOptionPane.OK_OPTION){
				if(checkValidation()){
					ArrayList<HashMap<String, String>> datas = getInputDatas();
					try {
						DatabaseManager.initDatabase(false);
						DatabaseManager.beginTransaction();
						
						addCustomerDatas(datas);
						addSellDatas(datas);
						
						DatabaseManager.commitTransaction();
						successInputData();
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
				}
			}
		}else if(obj == btnAddrow){
			((DefaultTableModel)mTable.getModel()).addRow(new String[]{String.valueOf(mTable.getRowCount()+1),"2016-01-02","최성춘","최성춘","01054611364","상품1","경기도 고양시 덕양구","15000",""});
		}else if(obj == btnExcel){
			jfc.setFileFilter(new FileNameExtensionFilter("excel[xls 형식만 지원]", "xls"));
			jfc.setMultiSelectionEnabled(false);
			int status = jfc.showOpenDialog(this);
			if(status == JFileChooser.APPROVE_OPTION){
				setToTable(jfc.getSelectedFile());
			}
		}else if(obj == btnClearAll){
			int selectedOption = JOptionPane.showConfirmDialog(this, "입력된 데이터를 초기화 하시겠습니까?", null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(selectedOption == JOptionPane.OK_OPTION){
				//입력된 데이터 삭제
				while(mTable.getRowCount() > 0){
					((DefaultTableModel)mTable.getModel()).removeRow(0);
				}
			}
		}else if(obj == btnDelete){
			int[] selectedRows = mTable.getSelectedRows();
			if(selectedRows.length == 0){
				JOptionPane.showMessageDialog(this, "선택된 행이 없습니다.");
			}else{
				int selectedOption = JOptionPane.showConfirmDialog(this, "선택된 행을 삭제 하시겠습니까?", null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(selectedOption == JOptionPane.OK_OPTION){
					for(int i=selectedRows.length-1; i>-1; i--){
						((DefaultTableModel)mTable.getModel()).removeRow(selectedRows[i]);
					}
				}
			}
		}
	}
	
	private void setToTable(File selectedFile) {
		String path = selectedFile.getAbsolutePath();
		if(path.toUpperCase().endsWith(".XLS")){
			DataFormatter formatter = new DataFormatter();
			Workbook workbook = null;
			try {
				workbook = new HSSFWorkbook(new FileInputStream(selectedFile));
				Sheet sheet = workbook.getSheetAt(0);
				for(Row row : sheet){
					if(row.getRowNum()==0){
						continue;
					}
					if(row.getCell(0).getCellTypeEnum()==CellType.BLANK || formatter.formatCellValue(row.getCell(0)).trim().equals("")){
						break;
					}
					
					((DefaultTableModel)mTable.getModel()).addRow(new String[]{
							String.valueOf(mTable.getRowCount()+1),
							new SimpleDateFormat("yyyy-MM-dd").format(row.getCell(0).getDateCellValue()),
							formatter.formatCellValue(row.getCell(1)),
							formatter.formatCellValue(row.getCell(2)),
							formatter.formatCellValue(row.getCell(3)).replaceAll("-", ""),
							formatter.formatCellValue(row.getCell(4)),
							formatter.formatCellValue(row.getCell(5)),
							formatter.formatCellValue(row.getCell(6)),
							formatter.formatCellValue(row.getCell(7))}
					);
				}
					
				
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, String.format("[%s] 파일이 존재하지 않습니다.", selectedFile.getAbsolutePath()));
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, String.format("엑셀파일을 읽는 도중 에러가 발생했습니다.[%s]", e.getMessage()));
			} finally {
				try {
					if(workbook != null) workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			JOptionPane.showMessageDialog(this, "xls 파일이 아닙니다.");
		}
		
	}

	private void successInputData() {
		mTable.clearSelection();
		JOptionPane.showMessageDialog(this, "데이터를 성공적으로 입력했습니다.");
		
		//입력된 데이터 삭제
		while(mTable.getRowCount() > 0){
			((DefaultTableModel)mTable.getModel()).removeRow(0);
		}
	}

	private boolean checkValidation() {
		for(int i=0; i<mTable.getRowCount(); i++){
			String name = mTable.getValueAt(i, 2).toString().trim();//주문인
			String phoneNo = mTable.getValueAt(i, 4).toString().trim();//전화번호
			if(name.equals("") || phoneNo.equals("")){
				JOptionPane.showMessageDialog(this, "[주문인]과 [전화]는 필수 입력 항목입니다.");
				return false;
			}
		}
		return true;
	}

	/**
	 * 0:구입일
	 * 1:주문인
	 * 2:수취인
	 * 3:전화번호
	 * 4:주문내역
	 * 5:주소
	 * 6:우편번호
	 * 7:비고 
	 * @throws SQLException 
	 */
	private Integer[] addCustomerDatas(ArrayList<HashMap<String, String>> sellData) throws SQLException{
		ArrayList<Integer> inputResult = new ArrayList<Integer>();
		ArrayList<String> rowData = null;
		for(int i=0; i<sellData.size(); i++){
			rowData = new ArrayList<String>();
			rowData.add(sellData.get(i).get("NAME"));
			rowData.add(sellData.get(i).get("PHONE_NO"));
			rowData.add(sellData.get(i).get("ADDRESS"));
			rowData.add(sellData.get(i).get("POST_CODE"));
			System.out.println(rowData.toString());
			try {
				inputResult.add(DatabaseManager.excuteUpdate("INSERT INTO " + Constants.TABLE_CUSTOMER + " (NAME, PHONE_NO, ADDRESS, POST_CODE) VALUES (?,?,?,?)", rowData.toArray(new String[rowData.size()])));
			} catch (SQLException e1) {
				if(e1.getSQLState().equals("23505")){
					System.out.println("[" + sellData.get(i).get("NAME") + ", " + sellData.get(i).get("PHONE_NO") + "] 기입력된 고객정보입니다.");
				}else{
					throw e1;
				}
			}
		}
		
		return inputResult.toArray(new Integer[inputResult.size()]);
	}

	private int[] addSellDatas(ArrayList<HashMap<String, String>> sellData) throws SQLException{
		int[] inputResult = null;
		ArrayList<ArrayList<String>> inputDatas = new ArrayList<ArrayList<String>>();
		ArrayList<String> rowData = null;
		for(int i=0; i<sellData.size(); i++){
			rowData = new ArrayList<String>();
			rowData.add(sellData.get(i).get("NAME"));
			rowData.add(sellData.get(i).get("PHONE_NO"));
			rowData.add(sellData.get(i).get("BUY_DATE"));
			rowData.add(sellData.get(i).get("RECIPIENT"));
			rowData.add(sellData.get(i).get("JUMUN"));
			rowData.add(sellData.get(i).get("ETC"));
			
			inputDatas.add(rowData);
		}
		
		inputResult = DatabaseManager.excuteUpdate("INSERT INTO SELL_LIST (CUSTOMER_NO, BUY_DATE, RECIPIENT, JUMUN, ETC) VALUES ((SELECT CUSTOMER_NO FROM CUSTOMER WHERE NAME=? AND PHONE_NO=?),?,?,?,?)", inputDatas);
		
		return inputResult;
	}

	private ArrayList<HashMap<String,String>> getInputDatas() {
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> rowData = null;
		for(int i=0; i<mTable.getRowCount(); i++){
			rowData = new HashMap<String, String>();
			int x = 1;
			rowData.put("BUY_DATE", mTable.getValueAt(i, x++).toString());
			rowData.put("NAME", mTable.getValueAt(i, x++).toString());
			rowData.put("RECIPIENT", mTable.getValueAt(i, x++).toString());
			rowData.put("PHONE_NO", mTable.getValueAt(i, x++).toString());
			rowData.put("JUMUN", mTable.getValueAt(i, x++).toString());
			rowData.put("ADDRESS", mTable.getValueAt(i, x++).toString());
			rowData.put("POST_CODE", mTable.getValueAt(i, x++).toString());
			rowData.put("ETC", mTable.getValueAt(i, x++).toString());
			list.add(rowData);
		}
		return list;
	}

	
}
