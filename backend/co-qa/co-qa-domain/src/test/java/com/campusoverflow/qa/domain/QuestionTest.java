package com.campusoverflow.qa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QuestionTest {

    static final Instant NOW = Instant.parse("2026-10-01T08:00:00Z");
    static final Actor ASKER = new Actor(1, "提问者", Role.STUDENT);
    static final Actor OTHER = new Actor(2, "路人", Role.STUDENT);
    static final Actor TEACHER = new Actor(9, "王老师", Role.TEACHER);

    static Question persistedQuestion() {
        Question q = Question.post(ASKER.userId(), 100L, new Title("红黑树插入后如何判断旋转方向？"),
                MarkdownBody.ofQuestion("插入节点后出现连续红色节点，什么时候左旋、什么时候右旋？"),
                Tags.of(List.of("数据结构", "红黑树")), NOW);
        q.assignId(10);
        return q;
    }

    static Answer persistedAnswer(Question q, long authorId, long id) {
        Answer a = Answer.submit(q, authorId, MarkdownBody.ofAnswer("看叔叔节点的颜色和插入位置。"), NOW);
        a.assignId(id);
        return a;
    }

    @Nested
    class Posting {
        @Test
        void titleMustBeBetween10And150Chars() {
            assertThatThrownBy(() -> new Title("太短了")).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("标题");
        }

        @Test
        void tagsAreNormalizedAndDeduplicated() {
            Tags tags = Tags.of(List.of("#Java", "java", " Spring-Boot "));
            assertThat(tags.names()).containsExactly("java", "spring-boot");
        }

        @Test
        void requiresOneToFiveTags() {
            assertThatThrownBy(() -> Tags.of(List.of())).isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(() -> Tags.of(List.of("a", "b", "c", "d", "e", "f")))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        void rejectsIllegalTagCharacters() {
            assertThatThrownBy(() -> new TagName("<script>")).isInstanceOf(BusinessRuleException.class);
            assertThat(new TagName("C++").value()).isEqualTo("c++");
        }
    }

    @Nested
    class Accepting {
        @Test
        void askerCanAcceptAnAnswerOnce() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, OTHER.userId(), 50);
            q.accept(a, ASKER, false);
            assertThat(q.acceptedAnswerId()).isEqualTo(50L);
            assertThat(q.isAccepted(50)).isTrue();

            Answer b = persistedAnswer(q, 3, 51);
            assertThatThrownBy(() -> q.accept(b, ASKER, false)).isInstanceOf(ConflictException.class);
        }

        @Test
        void courseTeacherCanAcceptOnBehalfOfAsker() {
            Question q = persistedQuestion();
            q.accept(persistedAnswer(q, OTHER.userId(), 50), TEACHER, true);
            assertThat(q.acceptedAnswerId()).isEqualTo(50L);
        }

        @Test
        void otherStudentsCannotAccept() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, 3, 50);
            assertThatThrownBy(() -> q.accept(a, OTHER, false)).isInstanceOf(ForbiddenException.class);
        }

        @Test
        void answerMustBelongToQuestion() {
            Question q = persistedQuestion();
            Question another = persistedQuestion();
            Answer foreign = Answer.reconstitute(60L, 999L, 3, MarkdownBody.ofAnswer("这是别的问题下面的回答内容"), 0,
                    null, NOW, NOW, null, 0);
            assertThatThrownBy(() -> q.accept(foreign, ASKER, false)).isInstanceOf(BusinessRuleException.class);
            assertThat(another.acceptedAnswerId()).isNull();
        }

        @Test
        void deletedAnswerCannotBeAccepted() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, 3, 50);
            a.softDelete(new Actor(3, "回答者", Role.STUDENT), false, false, NOW);
            assertThatThrownBy(() -> q.accept(a, ASKER, false)).isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    class EditingAndDeleting {
        @Test
        void onlyAuthorOrManagerCanEdit() {
            Question q = persistedQuestion();
            assertThatThrownBy(() -> q.edit(OTHER, false, q.title(), q.body(), q.tags(), NOW))
                    .isInstanceOf(ForbiddenException.class);
            q.edit(TEACHER, true, new Title("红黑树插入后的旋转规则是什么？"), q.body(), q.tags(), NOW.plusSeconds(60));
            assertThat(q.title().value()).contains("旋转规则");
            assertThat(q.updatedAt()).isAfter(q.createdAt());
        }

        @Test
        void askerCannotDeleteAnsweredQuestionButManagerCan() {
            Question answered = Question.reconstitute(10L, ASKER.userId(), 100L, new Title("一个已经有回答的问题标题"),
                    MarkdownBody.ofQuestion("这个问题已经有了一个回答，所以提问者不能删除。"), Tags.of(List.of("java")),
                    null, 0, 1, 0, NOW, NOW, null, 0);
            assertThatThrownBy(() -> answered.softDelete(ASKER, false, NOW)).isInstanceOf(BusinessRuleException.class);
            answered.softDelete(TEACHER, true, NOW);
            assertThat(answered.isDeleted()).isTrue();
        }

        @Test
        void deletedQuestionCannotBeAnswered() {
            Question q = persistedQuestion();
            q.softDelete(ASKER, false, NOW);
            assertThatThrownBy(() -> Answer.submit(q, OTHER.userId(), MarkdownBody.ofAnswer("这是一个迟到了很久的回答内容"), NOW))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }
}
