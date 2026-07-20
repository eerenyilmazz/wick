package com.wick.user.dto;

import com.wick.invitation.dto.InvitationResponse;
import com.wick.match.dto.MatchResponse;
import com.wick.payment.dto.PurchaseExportItem;
import com.wick.photo.dto.PhotoResponse;

import java.util.List;

public record DataExportResponse(
        UserResponse profile,
        List<PhotoResponse> photos,
        List<MatchResponse> matches,
        List<InvitationResponse> sentInvitations,
        List<PurchaseExportItem> purchases,
        int tokenBalance
) {
}
