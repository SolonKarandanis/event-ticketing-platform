package com.etp.ticketservice.common.paging;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paging extends AbstractPaging{
	
	public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer DEFAULT_PAGE_START = 0;

    public Paging() {
        setPagingSize(DEFAULT_PAGE_SIZE);
        setPagingStart(DEFAULT_PAGE_START);
    } 

}
