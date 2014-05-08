package uk.ac.imperial.lsds.streamsql.operator;

import uk.ac.imperial.lsds.streamsql.visitors.SeepSQLVisitor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;


public class App {
    
	public static void main(String[] args) {
		System.out.println( "Test runner..." );
       
        try {
        	
			Statement stmt = CCJSqlParserUtil.parse("SELECT * FROM tab1");
			stmt.accept(new SeepSQLVisitor());
			
			int i = 0;
//			stmt.accept(statementVisitor)
			
			
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
