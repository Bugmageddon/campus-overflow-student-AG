/**
 * Discovery 是纯下游上下文（遵奉者 Conformist）：它订阅 Q&A、Reputation、Identity 的集成事件，
 * 维护自己的搜索投影与通知，但目前不向其他上下文发布任何接口或事件。
 * <p>未来若将 Discovery 拆分为独立服务（arc42 第 11 章 R-3 的演进路径），对外接口在此定义。</p>
 */
package com.campusoverflow.discovery.api;
