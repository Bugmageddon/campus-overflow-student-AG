package com.campusoverflow.bootstrap.demo;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.application.AdminUserService;
import com.campusoverflow.identity.application.CourseService;
import com.campusoverflow.identity.application.CreateCourseCommand;
import com.campusoverflow.identity.application.CreateStaffCommand;
import com.campusoverflow.identity.application.RegisterUserCommand;
import com.campusoverflow.identity.application.UserRegistrationService;
import com.campusoverflow.identity.domain.CourseRole;
import com.campusoverflow.qa.application.command.AnswerCommandService;
import com.campusoverflow.qa.application.command.CommentService;
import com.campusoverflow.qa.application.command.PostCommentCommand;
import com.campusoverflow.qa.application.command.PostQuestionCommand;
import com.campusoverflow.qa.application.command.QuestionCommandService;
import com.campusoverflow.qa.application.command.VoteService;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.reputation.application.BountyService;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 演示数据（仅 demo profile 生效）：一门课、6 个账号、3 个问题，覆盖提问→回答→投票→认证→采纳→评论→悬赏全链路。
 * 课堂演示与验收时可直接登录查看效果；所有账号密码均为 {@value #PASSWORD}。
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

    public static final String PASSWORD = "Campus2026";
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final IdentityApi identity;
    private final UserRegistrationService registration;
    private final AdminUserService admins;
    private final CourseService courses;
    private final QuestionCommandService questions;
    private final AnswerCommandService answers;
    private final VoteService votes;
    private final CommentService comments;
    private final BountyService bounties;

    public DemoDataSeeder(IdentityApi identity, UserRegistrationService registration, AdminUserService admins,
                          CourseService courses, QuestionCommandService questions, AnswerCommandService answers,
                          VoteService votes, CommentService comments, BountyService bounties) {
        this.identity = identity;
        this.registration = registration;
        this.admins = admins;
        this.courses = courses;
        this.questions = questions;
        this.answers = answers;
        this.votes = votes;
        this.comments = comments;
        this.bounties = bounties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (identity.findByDisplayName("系统管理员").isPresent()) {
            log.info("演示数据已存在，跳过初始化");
            return;
        }
        Actor system = new Actor(0L, "system", Role.ADMIN); // 引导用虚拟主体，仅用于创建第一个管理员

        long adminId = admins.createStaff(system, new CreateStaffCommand("admin01", "系统管理员",
                "admin@campus.edu.cn", PASSWORD, Role.ADMIN, "信息中心"));
        Actor admin = new Actor(adminId, "系统管理员", Role.ADMIN);

        long teacherId = admins.createStaff(admin, new CreateStaffCommand("T2001", "王老师", "wang@campus.edu.cn",
                PASSWORD, Role.TEACHER, "计算机工程学院"));
        long taId = admins.createStaff(admin, new CreateStaffCommand("TA3001", "李助教", "li@campus.edu.cn",
                PASSWORD, Role.TA, "计算机工程学院"));
        Actor teacher = new Actor(teacherId, "王老师", Role.TEACHER);
        Actor ta = new Actor(taId, "李助教", Role.TA);

        long mingId = registration.register(new RegisterUserCommand("2023001", "小明", "ming@campus.edu.cn",
                PASSWORD, "计算机工程学院", "计科2301"));
        long hongId = registration.register(new RegisterUserCommand("2023002", "小红", "hong@campus.edu.cn",
                PASSWORD, "计算机工程学院", "计科2301"));
        long gangId = registration.register(new RegisterUserCommand("2023003", "小刚", "gang@campus.edu.cn",
                PASSWORD, "计算机工程学院", "计科2302"));
        Actor ming = new Actor(mingId, "小明", Role.STUDENT);
        Actor hong = new Actor(hongId, "小红", Role.STUDENT);
        Actor gang = new Actor(gangId, "小刚", Role.STUDENT);
        List.of(mingId, hongId, gangId).forEach(id -> admins.verify(admin, id));

        long courseId = courses.create(teacher, new CreateCourseCommand("CS2001", "数据结构", "2026-秋"));
        courses.join(ta, courseId);
        courses.assignRole(teacher, courseId, taId, CourseRole.TA);
        List.of(ming, hong, gang).forEach(s -> courses.join(s, courseId));

        long q1 = questions.post(ming, new PostQuestionCommand(courseId, "红黑树插入新节点后为什么必须旋转？",
                """
                课上讲到红黑树插入后要做变色和旋转，我理解变色是为了维持“红节点不能相邻”，
                但是为什么某些情况下仅靠变色不够、一定要旋转？有没有一个直观的解释方式？

                ```java
                void insertFixup(Node z) { /* ... */ }
                ```
                """, List.of("数据结构", "红黑树")));

        long a1 = answers.submit(hong, q1, """
                关键在于**黑高**这个不变量：变色只能在局部交换红黑属性，不能改变某条路径上的黑节点个数分布；
                当“父节点与叔节点”颜色不同（叔节点为黑）时，单纯变色会破坏黑高平衡，只能通过旋转把子树结构改成
                可以合法变色的形态。可以这样记：**叔红变色、叔黑旋转**。
                """);
        answers.submit(teacher, q1, """
                补充一个动手的办法：用 5、3、7、1、2 这个序列手工插入一遍，
                在每一步画出树并标注黑高，你会看到第 5 个元素插入时不旋转必然出现两个相邻红节点。
                """);

        votes.cast(gang, TargetType.ANSWER, a1, 1);
        votes.cast(ming, TargetType.ANSWER, a1, 1);
        votes.cast(gang, TargetType.QUESTION, q1, 1);
        answers.endorse(teacher, a1);
        answers.accept(ming, q1, a1);
        comments.post(gang, new PostCommentCommand(TargetType.ANSWER, a1, null, "@小红 “叔红变色、叔黑旋转”这个口诀太好记了，谢谢！"));

        long q2 = questions.post(gang, new PostQuestionCommand(courseId,
                "Spring Boot 中 @Transactional 为什么在同类方法调用时会失效？",
                """
                我在 Service 里写了 `public void a() { b(); }`，其中 `b()` 上标了 `@Transactional`，
                但是抛异常后数据并没有回滚。换成从另一个 Bean 调用 `b()` 就正常了，这是为什么？
                """, List.of("java", "spring-boot", "事务")));
        bounties.open(teacher, q2, 50, 7);

        questions.post(hong, new PostQuestionCommand(courseId, "MySQL 的 ngram 全文索引是如何支持中文检索的？",
                """
                默认的全文索引按空格分词，对中文几乎无效。课程项目里用到了 `WITH PARSER ngram`，
                想知道它的分词粒度 `ngram_token_size` 如何影响检索效果和索引体积？
                """, List.of("mysql", "全文检索")));

        log.info("演示数据初始化完成：管理员 admin01 / 教师 T2001 / 助教 TA3001 / 学生 2023001-2023003，密码 {}", PASSWORD);
    }
}
