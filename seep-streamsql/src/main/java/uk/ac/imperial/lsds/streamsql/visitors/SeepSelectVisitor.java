package uk.ac.imperial.lsds.streamsql.visitors;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeepSelectVisitor implements SelectVisitor, FromItemVisitor {

	private static Logger LOG = LoggerFactory.getLogger(SeepSelectVisitor.class);

	public void map() {
		
	}
	
	public void generate() {
		
	}
	
	@Override
	public void visit(PlainSelect plainSelect) {
		
		LOG.debug("plain select: {}", plainSelect);
		
//		plainSelect.getFromItem().accept(this);
//
//		_selectItems.addAll(plainSelect.getSelectItems());
//
//		if (plainSelect.getJoins() != null)
//			for (final Iterator joinsIt = plainSelect.getJoins().iterator(); joinsIt.hasNext();) {
//				final Join join = (Join) joinsIt.next();
//				visit(join);
//				join.getRightItem().accept(this);
//			}
//		_whereExpr = plainSelect.getWhere();
//		if (_whereExpr != null)
//			_whereExpr.accept(this);
	}

	public void visit(Select select) {
//		_tableList = new ArrayList<Table>();
//		_joinList = new ArrayList<Join>();
//		_selectItems = new ArrayList<SelectItem>();
//		_whereExpr = null;
		select.getSelectBody().accept(this);
	}
	
	@Override
	public void visit(Table tableName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubJoin subjoin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ValuesList valuesList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SetOperationList setOpList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WithItem withItem) {
		// TODO Auto-generated method stub
		
	}




}
