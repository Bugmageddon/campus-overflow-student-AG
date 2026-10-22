package com.campusoverflow.shared.event;

import java.util.UUID;

/** 事件 ID 生成器。 */
public final class EventIds {
    private EventIds() {
    }

    public static String next() {
        return UUID.randomUUID().toString();
    }
}
