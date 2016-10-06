package common;
import java.awt.Dimension;
import java.awt.Toolkit;


public class Constants {
	public static final boolean isDev = true;
	public static final String DB_NAME = "SELL_DB";
	public static final String DB_USER = "admin";
	public static final String DB_USER_PW = "admin";
	
	public static final String TABLE_CUSTOMER = "CUSTOMER";
	public static final String TABLE_SELL_LIST = "SELL_LIST";
	
	public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
}
