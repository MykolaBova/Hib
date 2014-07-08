package org.julp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pager<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    protected List<DomainObject<T>> objectList = new ArrayList<>();
    /**
     * Records per page
     */
    protected int pageSize = 20;

    public Pager(List<DomainObject<T>> objectList, int pageSize) {
        this.objectList = objectList;
        this.pageSize = pageSize;
    }

    public Pager(List<DomainObject<T>> objectList) {
        this.objectList = objectList;
    }

    public Pager() {
    }

    /**
     * Getter for property pageSize.
     *
     * @return Value of property pageSize.
     */
    public int getPageSize() {
        return pageSize;
    }

    public int getPagesTotal() {
        double d1 = Double.valueOf(this.objectList.size());
        double d2 = Double.valueOf(this.pageSize);
        double d3 = java.lang.Math.ceil(d1 / d2);
        int pagesTotal = (Double.valueOf(d3)).intValue();
        return pagesTotal;
    }

    /**
     * This is NOT zero based index: idx of the first object is 1, the second is 2, etc...
     */
    public PageHolder getPage(int pageNumber) {
        PageHolder pageHolder = new PageHolder();
        pageHolder.setObjectsTotal(this.objectList.size());
        if (getPageSize() == 0) {
            pageHolder.setPageNumber(1);
            pageHolder.setPagesTotal(1);
            pageHolder.setPage(this.objectList);
        } else {
            if (this.objectList.isEmpty()) {
                pageHolder.setPageNumber(0);
                pageHolder.setPagesTotal(0);
                pageHolder.setPage(new ArrayList<DomainObject<T>>());
            } else {
                int pagesTotal = getPagesTotal();
                pageHolder.setPagesTotal(pagesTotal);
                if (pageNumber > pagesTotal) {
                    pageNumber = pagesTotal;
                } else if (pageNumber < 1) {
                    pageNumber = 1;
                } else {
                    //
                }
                pageHolder.setPageNumber(pageNumber);
                int start = (pageNumber - 1) * getPageSize();
                int end = (pageNumber) * getPageSize();
                if (end > objectList.size()) {
                    end = objectList.size();
                }
                List<DomainObject<T>> page = new ArrayList();
                page.addAll(objectList.subList(start, end));
                pageHolder.setPage(page);
            }
        }
        return pageHolder;
    }

    /**
     * Setter for property pageSize.
     *
     * @param pageSize New value of property pageSize.
     *
     */
    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Invalid Page size: " + pageSize);
        }
        this.pageSize = pageSize;
    }

    public List<DomainObject<T>> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<DomainObject<T>> objectList) {
        this.objectList = objectList;
    }
}
