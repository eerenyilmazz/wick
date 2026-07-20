package com.wick.testsupport;

import com.wick.invitation.domain.Invitation;
import com.wick.legal.domain.LegalDocument;
import com.wick.legal.domain.LegalDocumentType;
import com.wick.match.domain.Like;
import com.wick.match.domain.Match;
import com.wick.payment.domain.Purchase;
import com.wick.photo.domain.UserPhoto;
import com.wick.session.domain.Session;
import com.wick.token.domain.TokenBalance;
import com.wick.user.domain.User;

import java.time.Instant;

public final class TestEntities {

    private TestEntities() {
    }

    public static User user(String email, String passwordHash, String displayName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setDisplayName(displayName);
        return user;
    }

    public static Match match(User userOne, User userTwo) {
        Match match = new Match();
        match.setUserOne(userOne);
        match.setUserTwo(userTwo);
        return match;
    }

    public static Invitation invitation(Match match, User sender) {
        Invitation invitation = new Invitation();
        invitation.setMatch(match);
        invitation.setSender(sender);
        return invitation;
    }

    public static Session session(Match match, Instant startedAt, Instant endsAt) {
        Session session = new Session();
        session.setMatch(match);
        session.setStartedAt(startedAt);
        session.setEndsAt(endsAt);
        return session;
    }

    public static Like like(User liker, User liked) {
        Like like = new Like();
        like.setLiker(liker);
        like.setLiked(liked);
        return like;
    }

    public static Purchase purchase(User user, String productId, String purchaseToken) {
        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setProductId(productId);
        purchase.setPurchaseToken(purchaseToken);
        return purchase;
    }

    public static UserPhoto userPhoto(User user, String storageKey, int displayOrder) {
        UserPhoto photo = new UserPhoto();
        photo.setUser(user);
        photo.setStorageKey(storageKey);
        photo.setDisplayOrder(displayOrder);
        return photo;
    }

    public static TokenBalance tokenBalance(User user) {
        TokenBalance balance = new TokenBalance();
        balance.setUser(user);
        return balance;
    }

    public static LegalDocument legalDocument(LegalDocumentType type, int version, String content, Instant publishedAt) {
        LegalDocument document = new LegalDocument();
        document.setType(type);
        document.setVersion(version);
        document.setContent(content);
        document.setPublishedAt(publishedAt);
        return document;
    }
}
