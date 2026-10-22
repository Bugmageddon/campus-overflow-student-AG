package com.campusoverflow.qa.application.port;

/** 浏览计数端口：先写 Redis，定时批量落库，避免热点行写放大。 */
public interface ViewCounter {
    void increment(long questionId);
}
