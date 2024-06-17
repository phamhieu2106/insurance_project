package com.example.usermanager.utils.contraint;

import org.springframework.data.domain.Sort;

public class PageConstant {

    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "10";
    public static final String PAGE_SORT_TYPE = "ASC";
    public static final String PAGE_DEFAULT_VALUE = "";

    private PageConstant() {
        super();
    }

    public static Sort getSortBy(String sortBy, String sortType) {
        Sort sort;

        if ("ASC".equals(sortType)) {
            sort = Sort.by(Sort.Direction.ASC, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.DESC, sortBy);
        }
        return sort;
    }


}
