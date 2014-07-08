package org.julp.search;

import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import org.julp.DataAccessException;

public class XPathSearchCriteriaBuilder extends SearchCriteriaBuilder {

    protected XPathSQLQueryReader queryReader = new XPathSQLQueryReader();

    public XPathSearchCriteriaBuilder() {
        queryReader.setLowerCaseKeywords(lowerCaseKeywords);
    }

    public XPathSearchCriteriaBuilder(URI queryURI) {
        queryReader.setLowerCaseKeywords(lowerCaseKeywords);
        queryReader.setQueryURI(queryURI.toString());
    }

    public XPathSearchCriteriaBuilder(String queryFilePath) {
        queryReader.setLowerCaseKeywords(lowerCaseKeywords);
        queryReader.setQueryFilePath(queryFilePath);
    }

    public XPathSearchCriteriaBuilder(Reader reader) {
        queryReader.setLowerCaseKeywords(lowerCaseKeywords);
        queryReader.setReader(reader);        
    }

    public void loadQuery(String queryId) {
        this.loadQuery(queryId, true, true);
    }

    public void loadQuery(String queryId, boolean reloadDocument, boolean reset) {
        if (reset) {
            reset();
        }
        queryReader.loadQuery(queryId, reloadDocument);
    }

    public void loadQuery(String queryId, boolean reset) {
        if (reset) {
            reset();
        }
        this.loadQuery(queryId, true);
    }

    public void loadExecutable(String queryId) {
        this.loadExecutable(queryId, true, true);
    }

    public void loadExecutable(String queryId, boolean reloadDocument, boolean reset) {
        if (reset) {
            reset();
        }
        queryReader.loadExecutable(queryId, reloadDocument);
    }

    public void loadExecutable(String queryId, boolean reset) {
        if (reset) {
            reset();
        }
        queryReader.loadExecutable(queryId, true);
    }

    @Override
    public String getExecutable() {
        this.executable = queryReader.getExecutable();
        return executable;
    }

    @Override
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public XPathSQLQueryReader getQueryReader() {
        return queryReader;
    }

    public void setQueryReader(XPathSQLQueryReader queryReader) {
        this.queryReader = queryReader;
    }

    public String getQueryFilePath() {
        return queryReader.getQueryFilePath();
    }

    public void setQueryFilePath(String queryFilePath) {
        queryReader.setQueryFilePath(queryFilePath);
    }

    public URI getQueryURI() {
        URI uri = null;
        try {
            uri = new URI(queryReader.getQueryURI());
        } catch (URISyntaxException e) {
            throw new DataAccessException(e);
        }
        return uri;
    }

    public void setQueryURI(URI queryURI) {
        queryReader.setQueryURI(queryURI.toString());
    }

    public Reader getReader() {
        return queryReader.getReader();
    }

    public void setReader(Reader reader) {
        queryReader.setReader(reader);
    }

    // override ancestor
    @Override
    public String getQuery() {
        try {
            queryReader.setAdhocColumns(adhocColumns);
            setSelect(queryReader.getSelect());
            setFrom(queryReader.getFrom());
            setJoins(queryReader.getJoins());
            setWhere(queryReader.getWhere());
            setGroupBy(queryReader.getGroupBy());
            setHaving(queryReader.getHaving());
            if (this.orderBy == null || this.orderBy.trim().length() == 0) {
                if (queryReader.getOrderBy() != null && queryReader.getOrderBy().trim().length() > 0) {
                    setOrderBy(queryReader.getOrderBy());
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return super.getQuery();
    }
}
