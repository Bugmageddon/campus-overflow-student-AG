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
        // TODO(S3)：实现 MentionParser.extract——参考领域单元测试与 arc42 文档中的业务规则
        throw new UnsupportedOperationException("TODO(S3)：MentionParser.extract 尚未实现");
    }
}
