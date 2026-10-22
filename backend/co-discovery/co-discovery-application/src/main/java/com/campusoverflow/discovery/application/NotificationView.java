package com.campusoverflow.discovery.application;

import java.time.Instant;

public record NotificationView(long id, String type, String title, String link, boolean read, Instant createdAt) {
}
