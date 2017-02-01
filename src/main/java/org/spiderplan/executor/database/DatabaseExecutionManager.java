package org.spiderplan.executor.database;

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.database.DatabaseExecutionExpression;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseExecutionManager extends ExecutionManager {
	
	Connection conn = null;


	public DatabaseExecutionManager(String name) {
		super(name);
		
        try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			
			conn = DriverManager.getConnection("jdbc:mysql://ecaredb.oru.se:3306/executive?useSSL=true&verifyServerCertificate=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT", "ecare-exec", "UVQnodKZt1cqrmA6");
//		
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}      
	}

	@Override
	public void initialize(ConstraintDatabase cdb) {
				
		for ( DatabaseExecutionExpression ec : cdb.get(DatabaseExecutionExpression.class) ) {
			if ( verbose ) { 
				 Logger.msg(this.getName(),"Checking: " + ec, 1);
			} 
				
			for ( Statement s : cdb.get(Statement.class) ) {
				
				Atomic variable = new Atomic(ec.getConstraint().getArg(1).toString()); //TODO: add Atomic constructor from Term
				Term value = ec.getConstraint().getArg(2);
				Substitution subst = variable.match(s.getVariable());
				
				if ( subst != null ) 
					subst.add(value.match(s.getValue()));
				
				if ( subst != null ) {
					if ( verbose ) Logger.msg(this.getName(),"    Creating reactor for " + s, 1);
					if ( hasReactorList.contains(s) ) {
						throw new IllegalStateException("Statement " + s + " has multiple reactors... This cannot be good!");
					}
					if ( !execList.contains(s) ) { 
						execList.add(s);
					}
					hasReactorList.add(s);
			
					DatabaseReactor reactor = new DatabaseReactor(s, conn);
					this.reactors.add(reactor);
				}
			}
		}	
		
		
	}
	
	@Override
	public boolean update( long t, ConstraintDatabase execDB ) {	
		
		for ( DatabaseExecutionExpression ec : execDB.get(DatabaseExecutionExpression.class) ) {
			if ( verbose ) { 
				 Logger.msg(this.getName(),"Checking: " + ec, 1);
			} 
			for ( Statement s : execDB.get(Statement.class) ) {
				if ( !super.hasReactorList.contains(s) ) {
					Atomic variable = new Atomic(ec.getConstraint().getArg(1).toString()); //TODO: add Atomic constructor from Term
					Term value = ec.getConstraint().getArg(2);
					Substitution subst = variable.match(s.getVariable());
					if ( subst != null ) 
						subst.add(value.match(s.getValue()));
					
					if ( subst != null ) {
						if ( verbose ) Logger.msg(this.getName(),"    Creating reactor for " + s, 1);
						if ( !execList.contains(s) ) { 
							execList.add(s);
						}
						hasReactorList.add(s);
				
						DatabaseReactor reactor = new DatabaseReactor(s, conn);
						this.reactors.add(reactor);
					}
				}
			}
		}	
		
		return super.update(t, execDB);
	}
}
