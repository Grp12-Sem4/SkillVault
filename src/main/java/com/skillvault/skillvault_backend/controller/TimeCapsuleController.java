package com.skillvault.skillvault_backend.controller;

import com.skillvault.skillvault_backend.dto.CompareTimeCapsulesRequest;
import com.skillvault.skillvault_backend.dto.CreateTimeCapsuleSnapshotRequest;
import com.skillvault.skillvault_backend.dto.TimeCapsuleComparisonResponse;
import com.skillvault.skillvault_backend.dto.TimeCapsuleSnapshotDetailResponse;
import com.skillvault.skillvault_backend.dto.TimeCapsuleSnapshotSummaryResponse;
import com.skillvault.skillvault_backend.service.TimeCapsuleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/time-capsules")
public class TimeCapsuleController {

    private final TimeCapsuleService timeCapsuleService;

    public TimeCapsuleController(TimeCapsuleService timeCapsuleService) {
        this.timeCapsuleService = timeCapsuleService;
    }

    @PostMapping
    public TimeCapsuleSnapshotSummaryResponse createSnapshot(@RequestBody CreateTimeCapsuleSnapshotRequest request,
                                                             Principal principal) {
        return timeCapsuleService.createSnapshot(requireUserEmail(principal), request);
    }

    @GetMapping
    public List<TimeCapsuleSnapshotSummaryResponse> getAllSnapshots(Principal principal) {
        return timeCapsuleService.getAllSnapshots(requireUserEmail(principal));
    }

    @GetMapping("/{snapshotId}")
    public TimeCapsuleSnapshotDetailResponse getSnapshot(@PathVariable Long snapshotId, Principal principal) {
        return timeCapsuleService.getSnapshot(requireUserEmail(principal), snapshotId);
    }

    @PostMapping("/compare")
    public TimeCapsuleComparisonResponse compareSnapshots(@RequestBody CompareTimeCapsulesRequest request,
                                                          Principal principal) {
        return timeCapsuleService.compareSnapshots(requireUserEmail(principal), request);
    }

    private String requireUserEmail(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return principal.getName();
    }
}
