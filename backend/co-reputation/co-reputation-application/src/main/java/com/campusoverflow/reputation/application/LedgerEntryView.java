package com.campusoverflow.reputation.application;

import java.time.Instant;

public record LedgerEntryView(long id, String reason, String reasonLabel, int delta, String refType, long refId,
                              Instant createdAt) {
}
