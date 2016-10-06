package ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import common.Constants;
import common.DatabaseManager;


public class MainFrame extends JFrame implements ActionListener{
	
	private JMenuBar mb;
	private JMenuItem miInput, miSearchHistory, miSearchCustomer;
	private InputOrderJPanel mInputOrderPanel;
	private SearchSellHistoryPanel mSellHistorySearchPanel;
	private SearchCustomerPanel mCustomerSearchPanel;
	private EditCustomerInfoPanel mEditCustomerInfoPanel;
	private EditSellInfoPanel mEditSellInfoPanel;
	private JProgressBar mProgress;
	private JButton mDefaultBtn;
	private JMenuItem miEditCustomerInfo;
	private JMenuItem miEditSellInfo;
	private JMenuItem miInitDB;
	private JMenuItem miExportExcel;
	
	public static void main(String[] args) {
		new MainFrame();
	}
	
	public MainFrame() {
		init();
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		setSize(900, 700);
		setVisible(true);
		setResizable(false);
		setLocation( (Constants.SCREEN_SIZE.width-getSize().width)/2, (Constants.SCREEN_SIZE.height-getSize().height)/2 );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void init() {
		mInputOrderPanel = new InputOrderJPanel(this);
		mSellHistorySearchPanel = new SearchSellHistoryPanel(this);
		mCustomerSearchPanel = new SearchCustomerPanel(this);
		mEditCustomerInfoPanel = new EditCustomerInfoPanel(this);
		mEditSellInfoPanel = new EditSellInfoPanel(this);
		
		mDefaultBtn = new JButton();
		mDefaultBtn.setVisible(false);
		
		mb = new JMenuBar();
		JMenu searchMenu = new JMenu("검색");
		searchMenu.add(miSearchHistory = new JMenuItem("구입내역 검색"));
		searchMenu.add(miSearchCustomer = new JMenuItem("고객 검색"));
		
		JMenu inputMenu = new JMenu("입력");
		inputMenu.add(miInput = new JMenuItem("구입내역 입력"));
		
		JMenu editMenu = new JMenu("편집");
		editMenu.add(miEditCustomerInfo = new JMenuItem("고객정보 수정"));
		editMenu.add(miEditSellInfo = new JMenuItem("구매내역 수정"));
		editMenu.add(new JSeparator());
		editMenu.add(miInitDB = new JMenuItem("DB 초기화"));
		editMenu.add(miExportExcel = new JMenuItem("내보내기"));
		
		miInput.addActionListener(this);
		miSearchHistory.addActionListener(this);
		miSearchCustomer.addActionListener(this);
		miEditCustomerInfo.addActionListener(this);
		miEditSellInfo.addActionListener(this);
		miInitDB.addActionListener(this);
		miExportExcel.addActionListener(this);
		
		mb.add(searchMenu);
		mb.add(inputMenu);
		mb.add(editMenu);
		
		add(mSellHistorySearchPanel);
		add(mInputOrderPanel);
		add(mCustomerSearchPanel);
		add(mEditCustomerInfoPanel);
		add(mEditSellInfoPanel);
		
		add(mDefaultBtn);
		
		setJMenuBar(mb);
		
		setTitle(miSearchHistory.getText());
		mSellHistorySearchPanel.setVisible(true);
//		mEditSellInfoPanel.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if(obj==miInitDB){
			int selectedOption = JOptionPane.showConfirmDialog(this, "DB를 초기화 하시겠습니까?", null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(selectedOption == JOptionPane.OK_OPTION){
				try {
					DatabaseManager.initDatabase(true);
					JOptionPane.showMessageDialog(this, "DB를 초기화 했습니다.\n프로그램을 재시작 해 주세요.");
					System.exit(0);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else if(obj==miExportExcel){
			int selectedOption = JOptionPane.showConfirmDialog(this, "DB에 저장된 데이터를 Excel파일로 생성합니다.\n진행하시겠습니까?", null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(selectedOption == JOptionPane.OK_OPTION){
				makeExcel(getSearchedData());
			}
		}else{
			setTitle( ((JMenuItem)obj).getText() );
			mInputOrderPanel.setVisible(obj==miInput);
			mSellHistorySearchPanel.setVisible(obj==miSearchHistory);
			mCustomerSearchPanel.setVisible(obj==miSearchCustomer);
			mEditCustomerInfoPanel.setVisible(obj==miEditCustomerInfo);
			mEditSellInfoPanel.setVisible(obj==miEditSellInfo);
		}
	}
	
	private void makeExcel(ResultSet rs){
		int count = 0;
		try {
			FileOutputStream fileOut = new FileOutputStream("판매이력.xls");
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet worksheet = workbook.createSheet("판매이력");
			createHeader(workbook, worksheet.createRow(0));
			
			HSSFRow row = null;
			while(rs.next()){
				row = worksheet.createRow(count+1);
				for(int i=0; i<10; i++){
					row.createCell(i).setCellValue(rs.getString(i+1));
				}
				count++;
			}
			if(count==0){
				JOptionPane.showMessageDialog(this, "Excel파일을 생성 할 데이터가 없습니다.");
			}else{
				workbook.write(fileOut);
				fileOut.flush();
				fileOut.close();
				JOptionPane.showMessageDialog(this, "Excel파일을 생성했습니다.");
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "사용중인 Excel파일을 닫고 다시 실행해 주세요.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	private void createHeader(HSSFWorkbook workbook, HSSFRow header) {
		HSSFCellStyle rowStyle = workbook.createCellStyle();
		rowStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
		header.setRowStyle(rowStyle);
		
		header.createCell(0).setCellValue("고객번호");
		header.createCell(1).setCellValue("구입횟수");
		header.createCell(2).setCellValue("구입일");
		header.createCell(3).setCellValue("주문인");
		header.createCell(4).setCellValue("수취인");
		header.createCell(5).setCellValue("전화");
		header.createCell(6).setCellValue("주문내역");
		header.createCell(7).setCellValue("주소");
		header.createCell(8).setCellValue("우편번호");
		header.createCell(9).setCellValue("비고");
		
	}

	private ResultSet getSearchedData(){
		String query;
		ResultSet rs = null;
		try {
			query = "SELECT "
					+ "C. CUSTOMER_NO, B.SELL_COUNT,A.BUY_DATE,C.NAME,A.RECIPIENT,C.PHONE_NO,A.JUMUN,C.ADDRESS,C.POST_CODE,A.ETC "
					+ "FROM "
					+ "SELL_LIST A, CUSTOMER C, (SELECT COUNT(CUSTOMER_NO) AS SELL_COUNT,CUSTOMER_NO FROM SELL_LIST WHERE DEL_YN != 'Y' GROUP BY CUSTOMER_NO) B "
					+ "WHERE "
					+ "A.CUSTOMER_NO = B.CUSTOMER_NO AND A.CUSTOMER_NO = C.CUSTOMER_NO "
					+ "ORDER BY A.BUY_DATE";
			DatabaseManager.initDatabase(false);
			rs = DatabaseManager.excuteQueryNoParams(query);
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
}
