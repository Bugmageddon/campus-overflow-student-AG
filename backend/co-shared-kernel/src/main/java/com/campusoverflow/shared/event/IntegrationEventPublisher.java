package com.campusoverflow.shared.event;

/**
 * 集成事件发布端口。实现（事务性发件箱）位于 co-bootstrap：
 * 事件与业务数据在<strong>同一本地事务</strong>中写入 outbox_event 表，事务提交后由 OutboxRelay 异步投递。
 * 因此调用方必须处于事务之中。
 */
public interface IntegrationEventPublisher {
    void publish(IntegrationEvent event);
}
