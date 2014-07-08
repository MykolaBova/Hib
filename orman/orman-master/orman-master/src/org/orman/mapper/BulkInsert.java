package org.orman.mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.orman.mapper.exception.NotDeclaredDefaultConstructorException;
import org.orman.sql.util.TypeCastHandler;
import org.orman.util.logging.Log;

/**
 * Bulk record insertion support class for {@link Model} classes.
 * 
 * @author oguz kartal
 */
public class BulkInsert<E extends Model<E>> {
	
	private Class<E> classType; //we should store this for object instance creation.
	private boolean isReady,rowSeperatorDefault,hasAutoInc;
	private String fieldSeperator,rowSeperator,regExp,sourceFile;
	private List<Field> fields;
	private InputStreamReader sourceStream;
	private Map<String,TypeCastHandler> castMap = null;
	
	
	private final int DEFAULT_BUFFER_SIZE = 2 * 1024;
	
	private final static String[] dateFormats = {"dd-MM-yyyy","yyyy-MM-dd","MM/dd/yyyy",
												"dd MMM yyyy", "dd MMMM yyyy","dd-MM-yyyy",
												"dd.MM.yyyy" };
	
	private final static String[] timeFormats = {"HH:mm:ss","HH:mm"};
	
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	
	//CAST HANDLERS BEGIN
	private static final TypeCastHandler stringCast = new TypeCastHandler() {

		@Override
		public String cast(String rawValue) {
			return rawValue;
			
		}

		@Override
		public void normalize() {
		}
	};
	
	private static final TypeCastHandler charCast = new TypeCastHandler() {

		@Override
		public Object cast(String rawValue) {
			return (Character)rawValue.charAt(0);
		}

		@Override
		public void normalize() {
		}
		
	};
	
	private static final TypeCastHandler boolCast = new TypeCastHandler() {

		@Override
		public Object cast(String rawValue) {
			if (rawValue.equalsIgnoreCase("true"))
				return (Boolean)true;
			
			return (Boolean)false;
		}

		@Override
		public void normalize() {
		}
		
	};
	
	private static final TypeCastHandler longCast = new TypeCastHandler() {
		
		@Override
		public Object cast(String rawValue) {
			try {
				return new Long(rawValue);
			}
			catch(Exception e) {}
			
			return (Long)0L;
		}

		@Override
		public void normalize() {
		}
	};
	
	private static final TypeCastHandler intCast = new TypeCastHandler() {

		@Override
		public Object cast(String rawValue) {
			try {
				return new Integer(rawValue);
			}catch (Exception e) {}
			
			return (Integer)0;
		}

		@Override
		public void normalize() {
		}
	};
	
	private static final TypeCastHandler shortCast = new TypeCastHandler() {

		@Override
		public Object cast(String rawValue) {
			try {
				return new Short(rawValue);
			}catch (Exception e) {}
			
			return (Short)(short)0;
		}

		@Override
		public void normalize() {
		}
		
	};
	
	private static final TypeCastHandler doubleCast = new TypeCastHandler() {

		@Override
		public Object cast(String rawValue) {
			try {
				return new Double(rawValue);
			}catch (Exception e){}
			
			return (Double)0.0d;
		}

		@Override
		public void normalize() {
		}
		
	};
	
	private static final TypeCastHandler floatCast = new TypeCastHandler() {

		@Override
		public Object cast(String rawValue) {
			try {
				return new Float(rawValue);
			}catch (Exception e) {}
			
			return (Float)0.0f;
		}

		@Override
		public void normalize() {
			
		}
		
	};
	
	private static final TypeCastHandler dateCast = new TypeCastHandler() {
		private String dateFormat = null;
		
		private Date getDateFromString(String s, String format) {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			
			try {
				return formatter.parse(s);
			}
			catch (ParseException e) {}
			
			return null;
		}
		
		private Date detectDateFormat(String dateInput) {
			Date parsedDate,tempDate;
			
			for (int i=0;i<dateFormats.length;i++) {
				//test only date
				parsedDate = getDateFromString(dateInput,dateFormats[i]);
					
				if (parsedDate != null) {
					for (int j=0;j<timeFormats.length;j++) {
						//test date + time
						tempDate = getDateFromString(dateInput, dateFormats[i] + " " + timeFormats[j]);
						
						if (tempDate != null) {
							dateFormat = dateFormats[i] + " " + timeFormats[j];
							return tempDate;
						}
					}
					
					dateFormat = dateFormats[i];
					return parsedDate;
				}
			}
			
			return null;
		}
		
		
		@Override
		public Object cast(String rawValue) {
			Date parsedDate = null;
			
			if (dateFormat == null) {
				parsedDate = detectDateFormat(rawValue);
				
				if (parsedDate == null) {
					//INVALID DATE!
					return null;
				}
				
				return parsedDate;
			}
			
			return getDateFromString(rawValue, dateFormat);
		}
		
		@Override
		public void normalize() {
			dateFormat = null;
		}
		
	};
	
	
	//CAST HANDLER END
	
	
	public BulkInsert(Class<E> clazz, String sourceFile, String regExp) {
		this.regExp = regExp;
		
		initCommons(clazz,sourceFile);
	}
	
	public BulkInsert(Class<E> clazz, String sourceFile, String fieldSeperator, String rowSeperator) {
		this.fieldSeperator = fieldSeperator;
		this.rowSeperator = rowSeperator;
		
		checkForDefaultRowSeperator();
		
		initCommons(clazz,sourceFile);
	}
	
	private void checkForDefaultRowSeperator() {
		if (rowSeperator.equals(NEW_LINE))
			this.rowSeperatorDefault = true;
	}
	
	private void initCommons(Class<E> clazz, String sourceFile) {
		this.castMap = new HashMap<String,TypeCastHandler>();
		this.isReady = false;
		this.sourceFile = sourceFile;
		this.classType = clazz;
		
		if (!MappingSession.entityExists(clazz)) {
			return;
		}
		
		fields = MappingSession.getEntity(clazz).getFields();
		
		dateCast.normalize();
		
		if (fields == null || fields.size() == 0) {
			Log.error("%s has not any field", clazz.getName());
			return;
		}
		
		for (Field field : fields) {
			
			if (field.isAutoIncrement()) {
				hasAutoInc = true;
				continue;
			}
			
			String typeName = getTypeIdentifierFromField(field);
		
			mapTypeCaster(typeName);
		}
		
		this.isReady = true;
		
	}
	
	private Object createNewInstanceOf(Class<E> clazz) {
		Constructor<?> ctor = null;
		Object instance;
		
		try {
			ctor = MappingSession.getEntity(clazz).getDefaultConstructor();
		}
		catch (NotDeclaredDefaultConstructorException e) {
			Log.error(e.getMessage());
			return null;
		}
		
		try {
			ctor.setAccessible(true);
			instance = ctor.newInstance();
		}
		catch (Exception e) {
			Log.error(e.getMessage());
			return null;
		}
		
		return instance;
		
	}
	
	private Object createObjectUsingFields(String[] rawFields) {
		Object newObject = createNewInstanceOf(this.classType);
		int fieldIndex=0;
		
		if (newObject == null)
			return null;
		
		for (Field field : fields) {
			
			if (field.isAutoIncrement())
				continue;
			
			TypeCastHandler caster = castMap.get(getTypeIdentifierFromField(field));
			Object realVal = caster.cast(rawFields[fieldIndex]);
			
			try {
				field.getSetterMethod().invoke(newObject, realVal);
			} catch (Exception e) {
				Log.error(e.getMessage());
			}
			
			fieldIndex++;
		}
		
		return newObject;
	}
	
	private void mapTypeCaster(String type) {
		
		//check for exist key
		if (castMap.containsKey(type))
			return;
		
		if (type.equals("String"))
			castMap.put(type, stringCast);
		else if (type.equalsIgnoreCase("long")) 
			castMap.put(type, longCast);
		else if (type.equals("int") || type.equals("Integer"))
			castMap.put(type, intCast);
		else if (type.equalsIgnoreCase("short"))
			castMap.put(type, shortCast);
		else if (type.equalsIgnoreCase("double"))
			castMap.put(type, doubleCast);
		else if (type.equalsIgnoreCase("float"))
			castMap.put(type, floatCast);
		else if (type.equalsIgnoreCase("boolean"))
			castMap.put(type, boolCast);
		else if (type.equals("char") || type.equals("Character"))
			castMap.put(type, charCast);
		else if (type.equals("Date"))
			castMap.put(type, dateCast);
		
	}
	
	private String getFileEncoding(File fileObject) throws IOException {
		FileInputStream fis = null;
		String encoding;
		byte[] magic = {0,0};
		
		
		fis = new FileInputStream(fileObject);
		fis.read(magic, 0, 2);
		
		encoding = (magic[0] == (byte)0xff && magic[1] == (byte)0xfe) ? "UTF-16" : "UTF-8";
		
		
		fis.close();
		
		return encoding;
	}
	
	private boolean createInputStream() {
		
		FileInputStream fis = null;
		File sourceFileObject = new File(sourceFile);
		String fileEncoding = null;
		
		if (!sourceFileObject.exists())
			return false;
		
		try {
			fileEncoding = getFileEncoding(sourceFileObject);
		} catch (IOException e) {
			Log.error(e.getMessage());
		}
		
		/*
		if (fileEncoding.equals("UTF-16"))
			this.sourceIsUnicode = true;
		
		this.sourceLength = sourceFileObject.length();
		*/
		
		try {
			fis = new FileInputStream(sourceFileObject);
			sourceStream = new InputStreamReader(fis,fileEncoding);
		}
		catch(Exception e) {
			Log.error(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	private void releaseSourceStream() {
		
		if (sourceStream != null) {
			try {
				sourceStream.close();
			} catch (IOException e) {}
			
			sourceStream = null;
		}
	}
	
	private final int getActualFieldsSize() {
		return hasAutoInc ? fields.size()-1 : fields.size();
	}
	
	private String getTypeIdentifierFromField(Field field) {
		String s = field.getClazz().toString();
		
		try {
			return s.substring(s.lastIndexOf('.')+1,s.length());
		}
		catch (Exception e){
			return s;
		}
	}
	
	
	private String[] stringSplitWithDelimiter(String str, String delim) {
		ArrayList<String> blocks = new ArrayList<String>();
		String[] arrayBlock=null;
		
		int beg=0,end;
		final int delimLen = delim.length();
		
		for (;;) {
			end = str.indexOf(delim, beg);
			
			if (end == -1) {
				if (beg < str.length())
					blocks.add(str.substring(beg,str.length()));
				break;
			}
			
			blocks.add(str.substring(beg,end));
			
			beg = end+delimLen;
		}
		
		if (blocks.size() > 0) {
			arrayBlock = new String[blocks.size()];
			blocks.toArray(arrayBlock);
			blocks.clear();
		}
		
		return arrayBlock;
	}
	
	
	private char readChar() {
		try {
			return (char)sourceStream.read();
		} catch (IOException e) {
			Log.error(e.getMessage());
		}
		
		return (char)-1;
	}
	
	private String readStringByLength(int len) {
		StringBuilder sb = new StringBuilder(len + 1);
		char val = 0;
		
		while (len-- > 0) {
			val = readChar();
			
			if (val == (char)-1)
				break;
			
			sb.append(val);
		}
		
		return sb.toString();
	}
	
	private int readBlock(StringBuilder buffer, int length) {
		char[] block = new char[length];
		int readed=0;
		
		try {
			readed = sourceStream.read(block, 0, length);
		} catch (IOException e) {
			Log.error(e.getMessage());
		}
		
		if (readed == -1)
			return -1;
		
		buffer.append(block,0,readed);
		
		return readed;
	}
	
	private String readRow() {
		StringBuffer sb = new StringBuffer();
		String temp;
		
		char val;
		int rowSepLen = rowSeperator.length();
		
		while ((val = readChar()) != (char)-1) {
			
			if (!rowSeperatorDefault) {
				if (val == '\r' || val == '\n')
					continue;
			}
			
			if (val == rowSeperator.charAt(0)) {
				
				if (rowSepLen == 1)
					break;
				
				temp = readStringByLength(rowSepLen-1);
				temp = val + temp;
				
				if (temp.equals(rowSeperator)) {
					break;
				}
				
				sb.append(temp);
			}
			else
				sb.append(val);
		}
		
		return sb.length() == 0 ? null : sb.toString();
	}
	
	
	
	@SuppressWarnings("unchecked")
	private int startBulkInsertUsingRegex() {
		StringBuilder fileBuffer;
		Model<E> model;
		Pattern pattern;
		Matcher matcher;
		String [] rowFields;
		boolean atLeastOneMatch=false;
		int matchEnd=0,affectedRecordCount=0;
		
		if (!createInputStream())
			return -1;
		
		fileBuffer = new StringBuilder(DEFAULT_BUFFER_SIZE);
		
		while (readBlock(fileBuffer,DEFAULT_BUFFER_SIZE) != -1) {
			
			pattern = Pattern.compile(this.regExp,Pattern.MULTILINE | Pattern.UNIX_LINES);
			
			matcher = pattern.matcher(fileBuffer.toString());
			
			while (matcher.find()) {
				atLeastOneMatch = true;
				matchEnd = matcher.end();
				
				if ((matcher.groupCount()) != getActualFieldsSize()) {
					Log.error("Mismatched record found = %s",matcher.group(0));
					continue;
				}
				
				rowFields = new String[getActualFieldsSize()];
				
				for (int i=0;i<matcher.groupCount();i++) {
					rowFields[i] = matcher.group(i+1);
				}
				
				model = (E)createObjectUsingFields(rowFields);
				
				if (model != null) {
					model.insert();
					affectedRecordCount++;
				}
				else {
					Log.error("Could not create object for row = %s", matcher.group(0));
				}
			}
			
			if (atLeastOneMatch) {
				fileBuffer.delete(0, matchEnd);
				atLeastOneMatch = false;
			}
		}
		
		return affectedRecordCount;
	}
	
	@SuppressWarnings("unchecked")
	private int startBulkInsertUsingSeperators() throws Exception {
		String row;
		String [] rowFields;
		Model<E> model;
		int affectedRecordCount=0;
		
		if (!createInputStream())
			return -1;
		
		while ((row = readRow()) != null) {
			rowFields = stringSplitWithDelimiter(row,fieldSeperator);
			
			if (rowFields.length != getActualFieldsSize()) {
				Log.error("Mismatched record found -> (%s)",row);
				continue;
			}
			
			model = (E)createObjectUsingFields(rowFields);
			
			if (model != null) {
				model.insert();
				affectedRecordCount++;
			}
			else {
				Log.error(String.format("Could not create object for row (%s)", row));
			}
		}
		
		return affectedRecordCount;
	}
	
	public int startBulkInsert() {
		int affectedRecords;
		
		if (!this.isReady)
			return -1;
		
		if (regExp != null) {
			affectedRecords = startBulkInsertUsingRegex();
		}
		else {
			try {
				affectedRecords = startBulkInsertUsingSeperators();
			} catch (Exception e) {
				Log.error(e.getMessage());
				affectedRecords = -1;
			}
		}
		
		releaseSourceStream();
		
		return affectedRecords;
	}
	
}
