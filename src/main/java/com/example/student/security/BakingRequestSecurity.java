package com.example.student.security;

import com.example.student.repository.BakingRequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("bakingRequestSecurity")
public class BakingRequestSecurity {

    private final BakingRequestRepository requestRepository;

    public BakingRequestSecurity(BakingRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public boolean isOwner(Long requestId, Authentication authentication) {
        if (authentication == null) return false;
        String email = authentication.getName();
        return requestRepository.findById(requestId)
                .map(r -> r.getUser() != null && r.getUser().getEmail().equals(email))
                .orElse(false);
    }
}