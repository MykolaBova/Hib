/*
 * Copyright (C) 2013 Jajja Communications AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.jajja.jorm.exceptions;

import java.sql.SQLException;

/**
 *
 * @author Daniel Adolfsson <daniel.adolfsson@jajja.com>
 * @since 1.0.0
 */
public class JormSqlException extends SQLException {
    private static final long serialVersionUID = 1L;
    private String database;
    private String sql;

    private static String rewriteMessage(String message, String database, String sql) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message);
        stringBuilder.append(" [jORM database ");
        stringBuilder.append(database);
        if (sql != null) {
            stringBuilder.append(", SQL: ");
            stringBuilder.append(sql);
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public JormSqlException(String database, String sql, SQLException sqlException) {
        super(
                rewriteMessage(sqlException.getMessage(), database, sql),
                sqlException.getSQLState(),
                sqlException.getErrorCode(),
                sqlException.getCause()
                );
        this.database = database;
        this.sql = sql;
        setStackTrace(sqlException.getStackTrace());
    }

    public String getDatabase() {
        return database;
    }

    public String getSql() {
        return sql;
    }
}