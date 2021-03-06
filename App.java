import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class App {
	
	public final String databaseName = "filesInFolder.db";
	Connection connection = null;
	public Statement statement = null;
	
	public boolean connectWithDb() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:file:db/"+databaseName);
			System.out.println("Connection with database established!");
			return true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean disconnectFromDb() {
		try {
			connection.close();
			return true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean checkIfTableExists(String tabName){
		try{
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tabName, null);
			if (tables.next()) 
				return true;	
			else 
				return false;
		}catch(SQLException e){
			return false;
		}
	}
	public void createTable(String tableName) {
		String sqlCommand = "CREATE TABLE IF NOT EXISTS "+tableName + " ("
				+ "id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) NOT NULL PRIMARY KEY, "
				+ "filename varchar(255) NOT NULL, size double NOT NULL);";
		if(!updateDb(sqlCommand))
			System.out.println("Table was not created!");
		
	}
	public void deleteTable(String tableName) {
		String sqlCommand = "DROP TABLE "+tableName+";";
		updateDb(sqlCommand);
	}
	public boolean updateDb(String sqlCommand) {
		try {
			statement = (Statement) connection.createStatement();
			statement.executeUpdate(sqlCommand);
			statement.close();
			return true;
		}catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	public ResultSet queryDb(String sqlCommand) {
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sqlCommand);
			statement.close();
			return rs;
		}catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addFilenameToDb(String path, String filename, double sizeInKb) {
		updateDb("INSERT INTO "+path+" VALUES (NULL,'"+filename+"',"+sizeInKb+");");
	}
	
	public boolean addFilesInFolderToDb(String folderName) {
		File folder = new File(folderName);
		if(!folder.exists()) {
			return false;
		}
		if(!connectWithDb()) {
			System.out.println("Nie udalo sie polaczyc z baza danych!");
			return false;
		}
		
		//pozdbywamy sie znakow niebezpiecznych dla sql
		String tFolderName = folderName;
		tFolderName = tFolderName.toUpperCase();
		tFolderName = tFolderName.substring(3);
		tFolderName = tFolderName.replace('\\', '_');
		
		
		if(!checkIfTableExists(tFolderName)) {
			createTable(tFolderName);
		}else {
			deleteTable(tFolderName);
			createTable(tFolderName);
		}

		System.out.println("Path: "+tFolderName);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getName();
				double sizeInKb = (listOfFiles[i].length() * 0.001);
				System.out.println(fileName+"\t"+sizeInKb);
				
				addFilenameToDb(tFolderName, fileName, sizeInKb);
			}
		}
		    
		 
		    
		if(!disconnectFromDb()) {
			System.out.println("Wystapil problem podczas rozlaczania z baza danych!");
			return false;
		}else {
			System.out.println("Poprawnie rozlaczono z baza danych!");
		}
		    
		return true;
	}
	
	
	
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.out.println("Nieprawidlowa ilosc argumentow w wywolaniu programu!");
			return;	
		}
		
		App app = new App();	
		
		if(app.addFilesInFolderToDb(args[0])) {
			System.out.println("Wykonano poprawnie funkcje!");
		}else {
			System.out.println("XD");
		}
		
		
		
		
		
		
		
		
	}
}
