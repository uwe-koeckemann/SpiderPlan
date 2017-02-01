/*******************************************************************************
 * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.spiderplan.executor.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.spiderplan.executor.Reactor;
import org.spiderplan.executor.Reactor.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;

/**
 * Request execution through a database.
 * 
 *  Works by adding execution requests in a database.
 *  Some other program takes care of handling the request and updates 
 *  the execution status in the database. 
 *  
 * @author Uwe Köckemann
 */
public class DatabaseReactor extends Reactor {
	
	/**
	 * Initial state: The request has been sent.
	 */
	public static final int REQUESTED = 0;  
	/**
	 * Execution is in progress.
	 * (this is set by program in charge of execution)
	 */
	public static final int HAS_STARTED = 1;
	/**
	 * Execution was finished successfully.
	 * (this is set by program in charge of execution)
	 */
	public static final int HAS_FINISHED = 2;
	/**
	 * A problem occurred during execution.
	 */
	public static final int ERROR = -1;
	
	Connection conn;
	Substitution goalResultSub;
	
	private int ID;
	
	/**
	 * 
	 * @param target statement to be executed
	 */
	public DatabaseReactor( Statement target, Connection conn ) {
		super(target);
		
		this.conn = conn;
	}
	
	@Override
	public Collection<Expression> update(long t, long EST, long LST, long EET, long LET, ConstraintDatabase execDB) {
		 super.update(t, EST, LST, EET, LET, execDB);		 
		 return super.activeConstraints;
	}
	
	@Override
	public void initStart( ) {
		// Insert new request and get request ID for easy checking later.
		java.sql.Statement sqlStatement = null;
		ResultSet sqlResultSet = null;
		try {			
			String sqlInsert = "INSERT INTO execute_string_command (command) VALUES(\""+target.toString()+"\")";
		    sqlStatement = conn.createStatement();
		    sqlStatement.executeUpdate(sqlInsert, java.sql.Statement.RETURN_GENERATED_KEYS);
		    		    
//		    sqlResultSet = sqlStatement.executeQuery("SELECT LAST_INSERT_ID()");
		    sqlResultSet = sqlStatement.getGeneratedKeys();
		    sqlResultSet.next();
		    this.ID = sqlResultSet.getInt(1);
		}
		catch (SQLException ex){
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		    System.exit(1);
		}
		finally {
		    if (sqlResultSet != null) {
		        try {
		            sqlResultSet.close();
		        } catch (SQLException sqlEx) { }

		        sqlResultSet = null;
		    }
		    if (sqlStatement != null) {
		        try {
		            sqlStatement.close();
		        } catch (SQLException sqlEx) { }
		        sqlStatement = null;
		    }
		}
	}
	
	@Override
	public boolean hasStarted( long EST, long LST ) {
		int state = 0;
		
		java.sql.Statement sqlStatement = null;
		ResultSet sqlResultSet = null;
		try {
		    sqlStatement = conn.createStatement();
		    sqlResultSet = sqlStatement.executeQuery("SELECT * FROM execute_string_command WHERE simple_request_id=" + this.ID);
		    sqlResultSet.next();
		    
		    state = sqlResultSet.getInt("state");
		    
//		    System.out.println("Request: " + this.getTarget() + " ID: " + this.ID + " has execution state: " + state);
		    Logger.msg(this.name, "Request: " + this.getTarget() + " ID: " + this.ID + " has execution state: " + state, 2);
		}
		catch (SQLException ex){
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		    System.exit(1);
		}
		finally {
		    if (sqlResultSet != null) {
		        try {
		            sqlResultSet.close();
		        } catch (SQLException sqlEx) { }

		        sqlResultSet = null;
		    }
		    if (sqlStatement != null) {
		        try {
		            sqlStatement.close();
		        } catch (SQLException sqlEx) { }
		        sqlStatement = null;
		    }
		}
		return state >= HAS_STARTED;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET ) {
		int state = 0;
		
		java.sql.Statement sqlStatement = null;
		ResultSet sqlResultSet = null;
		try {
		    sqlStatement = conn.createStatement();
		    sqlResultSet = sqlStatement.executeQuery("SELECT * FROM execute_string_command WHERE simple_request_id=" + this.ID);
		    sqlResultSet.next();
		    
		    state = sqlResultSet.getInt("state");
		    
		    Logger.msg(this.name, "Request: " + this.getTarget() + " ID: " + this.ID + " has execution state: " + state, 2);
		}
		catch (SQLException ex){
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		finally {
		    if (sqlResultSet != null) {
		        try {
		            sqlResultSet.close();
		        } catch (SQLException sqlEx) { }

		        sqlResultSet = null;
		    }
		    if (sqlStatement != null) {
		        try {
		            sqlStatement.close();
		        } catch (SQLException sqlEx) { }
		        sqlStatement = null;
		    }
		}
		return state >= HAS_FINISHED;
	}
	
	
}
