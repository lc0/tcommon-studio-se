// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.utils.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.talend.fakejdbc.FakeDatabaseMetaData;

/**
 * DOC gcui TeradataDataBaseMetadata.
 */
public class TeradataDataBaseMetadata extends FakeDatabaseMetaData {

    private static final String[] TABLE_META = { "ID", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private static final String[] COLUMN_META = { "TABLE_NAME", "COLUMN_NAME", "TYPE_NAME", "COLUMN_SIZE", "DECIMAL_DIGITS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "IS_NULLABLE", "REMARKS", "COLUMN_DEF" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String[] PK_META = { "COLUMN_NAME","PK_NAME" }; //$NON-NLS-1$ //$NON-NLS-2$
    private Connection connection;

    private String databaseName;
    
    private static final String CONST_TABLE="TABLE";//$NON-NLS-1$
    private static final String CONST_VIEW="VIEW";//$NON-NLS-1$
    private static final String CONST_SYNONYM="SYNONYM";//$NON-NLS-1$

    /**
     * 
     * @param metaData
     */
    public TeradataDataBaseMetadata(Connection connection) {
        this.connection = connection;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String dbName) {
        this.databaseName = dbName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getSchemas()
     */
    @Override
    public ResultSet getSchemas() throws SQLException {
        return connection.getMetaData().getSchemas();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getPrimaryKeys(java.lang.String, java.lang.String,
     * java.lang.String)
     */
	@Override
	public ResultSet getPrimaryKeys(String catalog, String schema, String table)
			throws SQLException {
		String sql = "HELP COLUMN " + schema + "." + table + ".* ";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

		ResultSet rs = null;
		Statement stmt = null;
		List<String[]> list = new ArrayList<String[]>();
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String columnName = rs.getString("Column Name").trim(); //$NON-NLS-1$
				String pk = rs.getString("Primary?");//$NON-NLS-1$
				String[] r = new String[] { columnName, pk };
				list.add(r);
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				rs.close();
				stmt.close();
			} catch (Exception e) {
			}
		}
		TeradataResultSet tableResultSet = new TeradataResultSet();
		tableResultSet.setMetadata(PK_META);
		tableResultSet.setData(list);
		return tableResultSet;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getTableTypes()
     */
    @Override
    public ResultSet getTableTypes() throws SQLException {
        String[] s1 = new String[] { CONST_TABLE }; 
        String[] s2 = new String[] { CONST_VIEW };
        String[] s3 = new String[] { CONST_SYNONYM }; 

        List<String[]> list = new ArrayList<String[]>();

        list.add(s1);
        list.add(s2);
        list.add(s3);

        TeradataResultSet tableResultSet = new TeradataResultSet();
        tableResultSet.setMetadata(new String[] { "TABLE_TYPE" }); //$NON-NLS-1$
        tableResultSet.setData(list);

        return tableResultSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getExportedKeys(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return new TeradataResultSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public ResultSet getTables(String catalog, String database, String tableNamePattern, String[] types) throws SQLException {
        // modify by wzhang
        if (databaseName != null) {
            database = databaseName;
        }
        // end
        String sql =null;
        if(types!=null&&types.length>0){
        	//value of index 0 is indicating the table,view or synonym.
        	String kind = "'T'";//$NON-NLS-1$
        	if(types[0].equalsIgnoreCase(CONST_VIEW)){
        		kind = "'V'";//$NON-NLS-1$
        	}else if(types[0].equalsIgnoreCase(CONST_SYNONYM)){
        		//TODO check if the alias of synonym is really "S".
        		kind = "'S'";//$NON-NLS-1$
        	}
        	sql="SELECT * from DBC.TABLES WHERE UPPER(databasename) = UPPER('" + database //$NON-NLS-1$
            + "') AND tablekind = "+kind+"  Order by tablename "; //$NON-NLS-1$//$NON-NLS-2$
        }else{
        	//When the types is empty, all the tables and views will be retrieved.
        	sql="SELECT * from DBC.TABLES WHERE UPPER(databasename) = UPPER('" + database //$NON-NLS-1$
            + "') AND (tablekind = 'T' or tablekind = 'V') Order by tablekind, tablename "; //$NON-NLS-1$
        }
        
        ResultSet rs = null;
        Statement stmt = null;
        List<String[]> list = new ArrayList<String[]>();
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String name = rs.getString("TableName").trim(); //$NON-NLS-1$
                // for bug 12811
                if (database == null || "".equals(database)) {//$NON-NLS-1$
                    database = rs.getString("CreatorName").trim(); //$NON-NLS-1$
                }
                String type = rs.getString("TableKind").trim(); //$NON-NLS-1$

                String[] r = new String[] { "", database, name, type, "" }; //$NON-NLS-1$ //$NON-NLS-2$
                list.add(r);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
            }
        }

        TeradataResultSet tableResultSet = new TeradataResultSet();
        tableResultSet.setMetadata(TABLE_META);
        tableResultSet.setData(list);
        return tableResultSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#supportsSchemasInDataManipulation()
     */
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#supportsSchemasInTableDefinitions()
     */
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return true;
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getColumns(java.lang.String, java.lang.String,
//     * java.lang.String, java.lang.String)
//     */
//    @Override
//    public ResultSet getColumns(String catalog, String database, String tableNamePattern, String columnNamePattern)
//            throws SQLException {
//        // for real
//        String sql = "SELECT * from DBC.COLUMNS Where UPPER(databasename) = UPPER('" + database + "') and tablename = '" //$NON-NLS-1$ //$NON-NLS-2$
//                + tableNamePattern + "' order by columnid"; //$NON-NLS-1$
////    	String sql ="HELP COLUMN "+database+"+."+tableNamePattern+".* ";
//        boolean needRetrieveFromDriver = false;
//
//        ResultSet rs = null;
//        Statement stmt = null;
//        List<String[]> list = new ArrayList<String[]>();
//        try {
//            stmt = connection.createStatement();
//            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                String tableName = rs.getString("TableName").trim(); //$NON-NLS-1$
//                String columnName = rs.getString("ColumnName").trim(); //$NON-NLS-1$
//                String typeName = rs.getString("ColumnType"); //$NON-NLS-1$
//                // add by wzhang. for teradata can't get the type for view. just set by default.
//                if (typeName == null) {
//                    typeName = "String"; //$NON-NLS-1$
//                    needRetrieveFromDriver = true;
//                }
//                String columnSize = rs.getString("ColumnLength"); //$NON-NLS-1$
//                String decimalDigits = rs.getString("DecimalFractionalDigits"); //$NON-NLS-1$
//                String isNullable;
//                if ("Y".equals(rs.getString("Nullable"))) { //$NON-NLS-1$ //$NON-NLS-2$
//                    isNullable = "YES"; //$NON-NLS-1$
//                } else {
//                    isNullable = rs.getString("Nullable"); //$NON-NLS-1$
//                }
//                String remarks = ""; //$NON-NLS-1$
//                String columnDef = ""; //$NON-NLS-1$
//                
//                String[] r = new String[] { tableName, columnName, typeName, columnSize, decimalDigits, isNullable, remarks,
//                        columnDef };
//                list.add(r);
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try {
//                rs.close();
//                stmt.close();
//            } catch (Exception e) {
//            }
//        }
//
//        if (needRetrieveFromDriver) {
//            return connection.getMetaData().getColumns(catalog, database, tableNamePattern, columnNamePattern);
//        }
//        TeradataResultSet tableResultSet = new TeradataResultSet();
//        tableResultSet.setMetadata(COLUMN_META);
//        tableResultSet.setData(list);
//        return tableResultSet;
//    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.utils.database.FakeDatabaseMetaData#getColumns(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public ResultSet getColumns(String catalog, String database, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        // for real
    	String sql ="HELP COLUMN "+database+"."+tableNamePattern+".* ";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

        ResultSet rs = null;
        Statement stmt = null; 
        List<String[]> list = new ArrayList<String[]>();
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tableName = tableNamePattern; //$NON-NLS-1$
                String columnName = rs.getString("Column Name").trim(); //$NON-NLS-1$
                String typeName = rs.getString("Type"); //$NON-NLS-1$
                String columnSize = rs.getString("Max Length"); //$NON-NLS-1$
                String decimalDigits = rs.getString("Decimal Fractional Digits"); //$NON-NLS-1$
                String isNullable;
                if ("Y".equals(rs.getString("Nullable"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    isNullable = "YES"; //$NON-NLS-1$
                } else {
                    isNullable = rs.getString("Nullable"); //$NON-NLS-1$
                }
                String remarks = ""; //$NON-NLS-1$
                String columnDef = ""; //$NON-NLS-1$
                
                String[] r = new String[] { tableName, columnName, typeName, columnSize, decimalDigits, isNullable, remarks,
                        columnDef };
                list.add(r);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
                stmt.close();
            } catch (Exception e) {
            }
        }
        TeradataResultSet tableResultSet = new TeradataResultSet();
        tableResultSet.setMetadata(COLUMN_META);
        tableResultSet.setData(list);
        return tableResultSet;
    }
}