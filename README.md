# CampusOverflow · 学生初始框架（Java / Spring Boot 3）

这是《软件工程》贯穿项目的**起始代码**。整体骨架、构建脚本、部署与 CI 已经就位，
**四个限界上下文中的 Identity（身份与认证）是完整的参考实现**，其余三个上下文的核心业务留给你们按 Sprint 实现。

现在克隆下来直接跑 `mvn test`，你会看到**大量失败的测试**——这不是环境坏了，这是你们这学期的需求说明书。

## 1. 五分钟跑起来

```bash
# 1) 起中间件（需要 Docker）
docker compose -f deploy/docker-compose.yml up -d mysql redis

# 2) 编译并跑测试（第一次会红，这是预期的）
cd backend && mvn clean test

# 3) 启动后端（dev profile，不要加 demo，因为演示数据依赖尚未实现的业务代码）
mvn spring-boot:run -pl co-bootstrap -Dspring-boot.run.profiles=dev

# 4) 启动前端
cd ../frontend && npm install && npm run dev      # http://localhost:5173
```

接口文档：http://localhost:8080/swagger-ui.html

## 2. 已经给你们的 vs 需要你们写的

| 已提供（读懂它，不要重写） | 需要你们实现 |
| --- | --- |
| 27 个 Maven 模块的骨架与依赖关系 | Q&A、Reputation、Discovery 三个上下文的**领域层**（聚合、值对象、业务规则） |
| `co-shared-kernel`：领域异常、Actor、事件契约、分页 | 上述三个上下文的**应用层**（用例编排、事务边界、事件发布） |
| **Identity 上下文完整实现**（注册、登录、课程、角色）——当作范例 | 前端页面的功能增强（见第 6 节） |
| 所有 `*-api` 发布语言、所有基础设施适配器（JPA / Redis / JDBC）与 REST 控制器 | 你们自己补充的单元测试（领域层覆盖率要求见第 5 节） |
| 安全链、发件箱中继、全局异常处理、Flyway 迁移、Docker、CI | |
| **107 个 `TODO` 方法与 38 个随之失败的测试**（领域测试 + 应用层用例测试 + 端到端集成测试） | |

> 为什么基础设施是给好的？因为本课程要考察的是**领域建模与架构约束**，而不是 JPA 映射的熟练度。
> 你只要把领域方法写对，适配器就能正常工作。

## 3. 怎么开始：跟着测试走

1. 打开 `docs/学生任务清单.md`，里面列出了全部 107 个 `TODO` 方法及其所在文件。
2. 从 Q&A 的领域层开始：`co-qa/co-qa-domain/src/test/java/.../QuestionTest.java` 就是规格说明。
3. 运行单个测试类：`mvn test -pl co-qa/co-qa-domain -Dtest=QuestionTest`
4. 红 → 绿 → 重构，逐个把测试变绿；再写应用层，让 `AnswerAndVoteServiceTest` 变绿。
5. 三个上下文都绿之后，跑 `mvn verify`（需要 Docker），端到端集成测试会验证“提问 → 回答 → 采纳 → 声誉 → 搜索 → 通知”整条链路。

**允许你们增加方法、增加类、增加测试**，但**不要修改已有测试的断言**——那相当于改需求；
如果你确信某条测试与文档矛盾，在 PR 里说明并与教师确认。

## 4. Sprint 与任务对应

| Sprint | 周次 | 目标 | 对应任务 |
| --- | --- | --- | --- |
| S0 | 1–2 | 跑通环境、读懂 Identity 参考实现与架构文档 | 无编码任务；提交环境验证截图与架构理解笔记 |
| S1 | 3–6 | 身份与课程：读懂并**扩展** Identity（如选课审批、个人资料） | 在 Identity 上下文中自行设计增量需求 |
| S2 | 7–10 | 问答核心：Q&A 上下文全部 TODO | `docs/学生任务清单.md` 中标记 **S2** 的 36 个方法 |
| S3 | 11–13 | 声誉与发现：Reputation + Discovery 上下文全部 TODO | 标记 **S3** 的 71 个方法 |
| S4 | 14–15 | 打磨：性能、可观测性、前端体验、演示准备 | 第 6 节的增强任务 + 压测与指标 |

## 5. 验收门槛（对应课程考核）

| 项目 | 要求 | 如何自检 |
| --- | --- | --- |
| 单元测试 | 全部通过 | `mvn test` |
| 领域层覆盖率 | ≥ 70%（把 `backend/pom.xml` 中的 `domain.coverage.minimum` 从 `0` 改成 `0.70` 后仍能通过） | `mvn verify` |
| 架构约束 | ArchUnit 全部通过（领域层零框架依赖、跨上下文只经 api、无环、实体表名前缀） | `mvn test -pl co-bootstrap -Dtest=ArchitectureTest` |
| 集成测试 | 端到端链路通过 | `mvn verify`（需要 Docker） |
| 前端 | 类型检查与构建通过 | `cd frontend && npm run typecheck && npm run build` |
| 流水线 | GitHub Actions 全绿 | 推送后看 Actions 页 |

**不要为了让测试通过而删测试或放宽断言**——CI 的历史记录会保留每一次改动，这属于学术诚信范畴。

## 6. 前端增强任务（S4 建议）

前端已提供可运行的骨架（登录/注册、问题列表与搜索、详情页投票与采纳、提问、排行榜、通知）。建议自行完成：

- 分页与无限滚动、加载骨架屏与错误重试；
- Markdown 实时预览与代码高亮；
- 悬赏发起对话框（当前是 `prompt()` 占位）、悬赏倒计时展示；
- 通知红点的实时刷新（当前是 30 秒轮询，可改 SSE）；
- 移动端适配与可访问性（键盘操作、对比度）。

## 7. 你必须先读的三份材料

1. `docs/adr/`：8 条架构决策记录——**为什么**这样设计比代码本身更重要；
2. 《CampusOverflow 软件架构设计文档（arc42）》：第 5 章构建块视图、第 6 章运行时视图、第 10 章质量场景；
3. `docs/错误码.md`：错误码分段与 HTTP 状态映射，实现领域异常时必须遵守。

## 8. 常见坑

| 现象 | 原因 |
| --- | --- |
| `UnsupportedOperationException: TODO(S2)...` | 正常，说明这个方法还没实现 |
| 声誉不变化 | 声誉是异步结算的：事件先落 `outbox_event`，由中继投递；检查 `last_error` 字段 |
| 中文搜索没结果 | 必须用 compose 里的 MySQL（已配置 `ngram_token_size=2`），自建库请对照参数 |
| 事件重复导致加分两次 | 消费端必须幂等：账本唯一键 `(event_id, user_id, reason)` 或 `processed_event` 表 |
| 提交后 CI 红 | 先本地跑 `mvn verify`，再推送；不要把红的分支合并到 `main` |
