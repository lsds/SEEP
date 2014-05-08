package uk.ac.imperial.lsds.streamsql.conversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.imperial.lsds.streamsql.visitors.SeepSQLVisitor;

public class DateConversion implements TypeConversion<Date> {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(SeepSQLVisitor.class);

	private static final String STRING_DATE_FORMAT = "yyyy-MM-dd";

	// this cannot be static, because a static field with a constructor cannot
	// be serialized
	private final SimpleDateFormat string_format = new SimpleDateFormat(STRING_DATE_FORMAT);

	private static final String INT_DATE_FORMAT = "yyyyMMdd";

	// this cannot be static, because a static field with a constructor cannot
	// be serialized
	private final SimpleDateFormat int_format = new SimpleDateFormat(INT_DATE_FORMAT);
	
	@Override
	public Date fromString(String str) {
		return fromString(str, string_format);
	}
	
	public Date fromLong(Long dateLong) {
		return fromString(String.valueOf(dateLong), int_format);
	}
	
	private Date fromString(String str, SimpleDateFormat format){
		Date date = null;
		try {
			date = format.parse(str);
		} catch (final ParseException pe) {
			LOG.error(pe.getMessage());
			throw new RuntimeException("Invalid Date Format for " + str);
		}
		return date;
	}	

	@Override
	public double getDistance(Date bigger, Date smaller) {
		/*
		final DateTime smallerDT = new DateTime(smaller);
		final DateTime biggerDT = new DateTime(bigger);
		return Days.daysBetween(smallerDT, biggerDT).getDays();
		*/
		long diff = bigger.getTime() - smaller.getTime();
		return (diff / (24* 1000 * 60 * 60));
	}

	@Override
	public Date getInitialValue() {
		return new Date();
	}

	// for printing(debugging) purposes
	@Override
	public String toString() {
		return "DATE";
	}

	@Override
	public String toString(Date obj) {
		return string_format.format(obj);
	}
	
	public Long toLong(Date obj){
		return Long.valueOf(int_format.format(obj));
	}
}