package com.umbral.domain.service;

import com.umbral.domain.dto.ExternalGameResponse;

import java.util.List;

public interface GameDiscoveryClient {
    List<ExternalGameResponse> search(String query);

    ExternalGameResponse findById(Long rawgGameId);
}
