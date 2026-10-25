package com.campusoverflow.discovery.domain.notification;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 解析评论中的 @昵称（昵称不含空格与 @，长度 2–20）。每条评论最多提醒 5 人，防止滥用。 */
public final class MentionParser {

    private static final Pattern MENTION = Pattern.compile("@([^\\s@，。,.!?！？:：;；、]{2,20})");
    public static final int MAX_MENTIONS = 5;

    private MentionParser() {
    }

    public static Set<String> extract(String text) {
        Set<String> mentions = new LinkedHashSet<>();
        if (text == null) {
            return mentions;
        }
        Matcher m = MENTION.matcher(text);
        while (m.find() && mentions.size() < MAX_MENTIONS) {
            mentions.add(m.group(1));
        }
        return mentions;
    }
}
