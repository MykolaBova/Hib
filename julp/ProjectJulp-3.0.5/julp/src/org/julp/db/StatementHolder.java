package org.julp.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;

public class StatementHolder implements Serializable {

    private static final long serialVersionUID = 1L;
    private Timestamp timestamp;
    private String query;
    private Collection<?> params;

    public StatementHolder(Timestamp timestamp, String query, Collection<?> params) {
        this.timestamp = timestamp;
        this.query = query;
        this.params = params;
    }

    public StatementHolder(String query, Collection<?> params) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.query = query;
        this.params = params;
    }

    public Collection<?> getParams() {
        return params;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Statement::");
        sb.append(timestamp).append("::").append(query.trim());
        if (params != null && !params.isEmpty()) {
            sb.append("::").append(params);
        }
        return sb.toString();
    }

}
