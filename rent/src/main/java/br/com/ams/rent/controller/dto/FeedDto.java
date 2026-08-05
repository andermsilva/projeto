package br.com.ams.rent.controller.dto;

import java.util.List;

public record FeedDto(List<FeedItemDto> feedItens, int page,
                      int pageSize,int totalPpages, Long totalElements) {
}
