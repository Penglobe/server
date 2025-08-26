package com.penglobe.server.service;

import com.penglobe.server.dto.RegionDTO;
import com.penglobe.server.repository.RegionRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;
    private final UserRepository userRepository;

public List<RegionDTO> getRegionRankings() {
    return  regionRepository.getRegionRankings();
}



}
