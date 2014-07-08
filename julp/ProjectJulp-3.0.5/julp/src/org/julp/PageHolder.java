package org.julp;

public class PageHolder implements java.io.Serializable {

    private static final long serialVersionUID = 517005814562304951L;
    protected int pagesTotal = 0;
    protected int pageNumber = 0;
    protected int objectsTotal = 0;
    protected java.util.List<?> page = null;

    public PageHolder() {
    }

    public PageHolder(int pageNumber, int pagesTotal, int objectsTotal, java.util.List<?> page) {
        this.pageNumber = pageNumber;
        this.pagesTotal = pagesTotal;
        this.objectsTotal = objectsTotal;
        this.page = page;
    }

    /**
     * Getter for property pagesTotal.
     *
     * @return Value of property pagesTotal.
     *
     */
    public int getPagesTotal() {
        return pagesTotal;
    }

    /**
     * Setter for property pagesTotal.
     *
     * @param pagesTotal New value of property pagesTotal.
     *
     */
    public void setPagesTotal(int pagesTotal) {
        this.pagesTotal = pagesTotal;
    }

    /**
     * Getter for property pageNumber.
     *
     * @return Value of property pageNumber.
     *
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Setter for property pageNumber.
     *
     * @param pageNumber New value of property pageNumber.
     *
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Getter for property page.
     *
     * @return Value of property page.
     *
     */
    public java.util.List<?> getPage() {
        return page;
    }

    /**
     * Setter for property page.
     *
     * @param page New value of property page.
     *
     */
    public void setPage(java.util.List<?> page) {
        this.page = page;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("pageNumber=").append(pageNumber);
        sb.append("&pagesTotal=").append(pagesTotal);
        sb.append("&objectsTotal=").append(objectsTotal);
        sb.append("&page=").append(page);
        return sb.toString();
    }

    /**
     * Getter for property objectsTotal.
     *
     * @return Value of property objectsTotal.
     *
     */
    public int getObjectsTotal() {
        return objectsTotal;
    }

    /**
     * Setter for property objectsTotal.
     *
     * @param objectsTotal New value of property objectsTotal.
     *
     */
    public void setObjectsTotal(int objectsTotal) {
        this.objectsTotal = objectsTotal;
    }
}
