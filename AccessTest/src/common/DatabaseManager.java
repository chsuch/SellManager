package common;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.derby.jdbc.EmbeddedDataSource;


public class DatabaseManager {
	private static EmbeddedDataSource ds;
	private static ThreadLocal<Connection> tranConnection = new ThreadLocal<Connection>();
	
	private static void initDataSource(String dbName){
		ds = new EmbeddedDataSource();
		ds.setDatabaseName(dbName);
		ds.setCreateDatabase("create");
	}
	
	public static void logSql() throws SQLException{
		excuteUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(" + "'derby.language.logStatementText', 'true'");
	}
	
	public static synchronized void beginTransaction() throws SQLException {
		System.out.println("beginTransaction()...");
		if(tranConnection.get() != null){
			throw new SQLException("This thread is already in a transaction");
		}
		Connection conn = getConnection();
		conn.setAutoCommit(false);
		tranConnection.set(conn);
	}
	
	public static void commitTransaction() throws SQLException {
		System.out.println("commitTransaction()...");
		if(tranConnection.get() == null){
			throw new SQLException("Can`t commit : This thread isn`t currently in a transaction");
		}
		tranConnection.get().commit();
		tranConnection.set(null);
	}
	
	public static void rollbackTransaction() throws SQLException {
		System.out.println("rollbackTransaction()...");
		if(tranConnection.get() == null){
			throw new SQLException("Can`t rollback : This thread isn`t currently in a transaction");
		}
		tranConnection.get().rollback();
		tranConnection.set(null);
	}
	
	public static Connection getConnection() throws SQLException {
		if(tranConnection.get() != null){
			return tranConnection.get();
		}else{
			return ds.getConnection();
		}
	}
	
	public static void releaseConnection(Connection conn) throws SQLException {
		if(tranConnection.get() == null){
			conn.close();
		}
	}
	
	public static void initDatabase(boolean dropTables) throws SQLException {
		initDataSource(Constants.DB_NAME);
		if(dropTables){
			dropTables();
		}
		if(!tableExists(Constants.TABLE_CUSTOMER) ){
			createTables();
		}
	}
	
	private static boolean tableExists(String tablename) throws SQLException{
		Connection conn = getConnection();
		ResultSet rs;
		boolean exists;
		try {
			DatabaseMetaData md = conn.getMetaData();
			rs = md.getTables(null, null, tablename.toUpperCase(), null);
			exists = rs.next();
		} finally {
			releaseConnection(conn);
		}
		
		return exists;
	}
	
	private static void createTables() throws SQLException {
		String query = "CREATE table " + Constants.TABLE_CUSTOMER + " ("
				+ " CUSTOMER_NO	INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
				+ " NAME				VARCHAR(10) NOT NULL,"
				+ " PHONE_NO			VARCHAR(15) NOT NULL,"
				+ " ADDRESS			VARCHAR(100) ,"
				+ " POST_CODE		CHAR(5),"
				+ " UPDATE_TIME		TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ " INPUT_TIME		TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ " DEL_YN				CHAR(1) DEFAULT 'N',"
				+ " PRIMARY KEY(NAME, PHONE_NO)"
				+ " )";
		
		
		excuteUpdate(query);
		
		query = "CREATE table " + Constants.TABLE_SELL_LIST + " ("
				+ " SEQ									INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
				+ " CUSTOMER_NO					INTEGER NOT NULL,"
				+ " BUY_DATE							DATE,"
				+ " RECIPIENT							VARCHAR(10),"
				+ " JUMUN								VARCHAR(50),"
				+ " ETC									VARCHAR(100),"
				+ " UPDATE_TIME						TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ " INPUT_TIME						TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
				+ " DEL_YN								CHAR(1) DEFAULT 'N'"
				+ " )";
		
		excuteUpdate(query);
	}
	
	public static void dropTables() throws SQLException {
		try {
			excuteUpdate("DROP TABLE " + Constants.TABLE_CUSTOMER);
			excuteUpdate("DROP TABLE " + Constants.TABLE_SELL_LIST);
		} catch (SQLException e) {
			if(!tableDoesntExist(e.getSQLState())){
				throw e;
			}
		}
	}
	
	private static boolean tableDoesntExist(String sqlState){
		return sqlState.equals("42X05") || sqlState.equals("42Y55");
	}
	
	public static void clearTables() throws SQLException {
		Connection conn = getConnection();
		try {
			excuteUpdate("DELETE FROM " + Constants.TABLE_CUSTOMER);
			excuteUpdate("DELETE FROM " + Constants.TABLE_SELL_LIST);
		} finally {
			releaseConnection(conn);
		}
	}
	
	public static int[] excuteUpdate(String statement, ArrayList<ArrayList<String>> paramsList) throws SQLException {
		System.out.println(statement);
		Connection conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(statement);
		ps.clearParameters();
		for(ArrayList<String> params : paramsList){
			for(int i=0; i<params.size(); i++){
				ps.setString(i+1, params.get(i));
			}
			ps.addBatch();
		}
		
		return ps.executeBatch();
	}
	
	public static int excuteUpdate(String statement, String[] params) throws SQLException {
		System.out.println(statement);
		Connection conn = getConnection();
		PreparedStatement ps = conn.prepareStatement(statement);
		if(params != null && params.length > 0){
			ps.clearParameters();
			for(int i=0; i<params.length; i++){
				ps.setString(i+1, params[i]);
			}
		}
		return ps.executeUpdate();
	}
	
	public static int excuteUpdate(String statement) throws SQLException {
		return excuteUpdate(statement, new String[]{});
	}
	
	public static ResultSet excuteQuery(String statement, String[] params) throws SQLException {
		System.out.println(statement);
		Connection conn = getConnection();
//		try {
			PreparedStatement ps = conn.prepareStatement(statement);
			if(params != null){
				ps.clearParameters();
				for(int i=0; i<params.length; i++){
					ps.setString(i+1, params[i]);
				}
			}
			return ps.executeQuery();
//		} 
//		finally {
//			releaseConnection(conn);
//		}
	}
	
	public static ResultSet excuteQueryNoParams(String statement) throws SQLException {
		Connection conn = getConnection();
//		try {
			PreparedStatement ps = conn.prepareStatement(statement);
			return ps.executeQuery();
//		} finally {
//			releaseConnection(conn);
//		}
	}
	
}
