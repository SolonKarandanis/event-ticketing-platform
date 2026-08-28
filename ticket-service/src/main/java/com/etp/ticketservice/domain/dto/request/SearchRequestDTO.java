package com.etp.ticketservice.domain.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequestDTO {
	protected Paging paging;

	public SearchRequestDTO() {
		this.paging = new Paging();
	}
}
