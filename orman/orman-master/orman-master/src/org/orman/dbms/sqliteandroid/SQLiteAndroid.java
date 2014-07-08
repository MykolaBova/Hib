package org.orman.dbms.sqliteandroid;

import org.orman.dbms.DataTypeMapper;
import org.orman.dbms.Database;
import org.orman.dbms.DatabaseSchemaInspector;
import org.orman.dbms.QueryExecutionContainer;
import org.orman.dbms.sqlite.generic.DataTypeMapperImpl;
import org.orman.dbms.sqlite.generic.SQLiteGrammar;
import org.orman.dbms.sqlite.generic.SQLiteSchemaInspector;
import org.orman.sql.SQLGrammarProvider;
import org.orman.util.logging.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite wrapper for Android SDK <code>android.database.sqlite
 * </code> package. Uses Android SDK 3 as a reference, for high
 * compatibility.
 * 
 * Shares same grammar and data type mapper with <code>org.orman.sqlite</code>
 * package.
 * 
 * @author ahmet alp balkan <ahmetalpbalkan at gmail.com>
 */
public class SQLiteAndroid extends SQLiteOpenHelper implements Database {
	private static final int SQLITE_VERSION = 33; // legacy support for deprecated constructor. instead, specify db version explicitly.
	private DataTypeMapper typeMapper;
	private QueryExecutionContainerImpl executer;
	private SQLiteGrammar grammar;
	private SQLiteSchemaInspector schemaInspector;
	
	private String databaseName;
	private SQLiteDatabase db;
	
	/**
	 * android sets the version of the DB when it is opened.
	 */
	public SQLiteAndroid(Context context, String dbFilename, int version) {
		super(context, dbFilename, null, version);
		init(dbFilename);
	}

	/**
	 * constructor kept for legacy purposes: all DBs created before this patch
	 * will have version '33' and therefore need to specify a version equal or
	 * greater (in case of a schema upgrade).
	 * 
	 * where is the db version stored? see:
	 * http://stackoverflow.com/questions/3707797/where-does-android-store-sqlite-database-version
	 * 
	 * @deprecated use SQLiteAndroid(Context context, String dbFilename, int version) instead
	 */
	public SQLiteAndroid(Context context, String dbFilename) {
		super(context, dbFilename, null, SQLITE_VERSION);
		init(dbFilename);
	}
	
	private void init(String dbFilename) {
		SQLiteDatabase db = getWritableDatabase();
		this.db = db;
		
		this.databaseName = dbFilename;
		typeMapper = new DataTypeMapperImpl();
		executer = new QueryExecutionContainerImpl(this.db); //bind database onCreate.
		grammar = new SQLiteGrammar();
		schemaInspector = new SQLiteSchemaInspector(getExecuter());
		
		Log.trace("Orman: DB initialized at %s", this.db.getPath());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//TODO currently defers creation of database to SchemaCreationPolicy.
		// and does not create database here.
		Log.warn("Orman: SQLite database onCreate invoked. Database path %s.", db.getPath());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.warn("onUpgrade");
		Log.info(
				"Orman: SQLite database onUpgrade invoked. Database path %s. Old %d, new %d.",
				db.getPath(), oldVersion, newVersion);
	}

	@Override
	public QueryExecutionContainer getExecuter() {
		return executer;
	}

	@Override
	public DataTypeMapper getTypeMapper() {
		return typeMapper;
	}

	@Override
	public void closeConnection() {
		executer.close();
		Log.info("Connection terminated successfully.");
	}

	@Override
	public SQLGrammarProvider getSQLGrammar() {
		return grammar;
	}

	
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public DatabaseSchemaInspector getSchemaInspector() {
		return schemaInspector;
	}

}
