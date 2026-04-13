package Util;

import java.util.List;

public class Page<E> {
    private List<E> elementsOnPage;
    private int pageNumber;
    private int pageSize;

    public List<E> getElementsOnPage() {
        return elementsOnPage;
    }


    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Page(List<E> elementsOnPage, int pageNumber, int pageSize) {
        this.elementsOnPage = elementsOnPage;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

}
