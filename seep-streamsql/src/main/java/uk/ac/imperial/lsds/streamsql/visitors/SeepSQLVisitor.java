package uk.ac.imperial.lsds.streamsql.visitors;


import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeepSQLVisitor implements StatementVisitor {
	
	private static Logger LOG = LoggerFactory.getLogger(SeepSQLVisitor.class);

	@Override
	public void visit(Select select) {
		LOG.debug("Visit SELECT statement: {}", select);
		SeepSelectVisitor selectVisitor = new SeepSelectVisitor();
		selectVisitor.visit(select);
		selectVisitor.map();
		selectVisitor.generate();
	}
	
	@Override
	public void visit(Statements stmts) {
		LOG.debug("Visit multiple SELECT statements");
		for (Statement statement : stmts.getStatements())
			statement.accept(this);
	}


	@Override
	public void visit(Delete delete) {
		LOG.error("DELETE statement is not supported");
	}

	@Override
	public void visit(Update update) {
		LOG.error("UPDATE statement is not supported");
	}

	@Override
	public void visit(Insert insert) {
		LOG.error("INSERT statement is not supported");
	}

	@Override
	public void visit(Replace replace) {
		LOG.error("REPLACE statement is not supported");
	}

	@Override
	public void visit(Drop drop) {
		LOG.error("DROP statement is not supported");
	}

	@Override
	public void visit(Truncate truncate) {
		LOG.error("TRUNCATE statement is not supported");
	}

	@Override
	public void visit(CreateIndex createIndex) {
		LOG.error("CREATEINDEX statement is not supported");
	}

	@Override
	public void visit(CreateTable createTable) {
		LOG.error("CREATETABLE statement is not supported");
	}

	@Override
	public void visit(CreateView createView) {
		LOG.error("CREATEVIEW statement is not supported");
	}

	@Override
	public void visit(Alter alter) {
		LOG.error("ALTER statement is not supported");
	}

}
