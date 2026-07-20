package com.example.photoGroupe.service.share;

import com.example.photoGroupe.dto.share.ShareResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.share.Share;
import com.example.photoGroupe.model.share.ShareableType;
import com.example.photoGroupe.repo.ShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * @param pathSegment the URL segment for this entity type, e.g. "workshop", "album", "event", "profile"
     */
    public ShareResponse share(ShareableType type, Long entityId, String pathSegment, User sharer) {
        String shareLink = baseUrl + "/" + pathSegment + "/" + entityId;

        shareRepository.save(new Share(type, entityId, sharer, shareLink));

        return ShareResponse.builder()
                .entityId(entityId)
                .entityType(type.name())
                .shareLink(shareLink)
                .totalShares(shareRepository.countDistinctSharers(type, entityId))
                .build();
    }
}