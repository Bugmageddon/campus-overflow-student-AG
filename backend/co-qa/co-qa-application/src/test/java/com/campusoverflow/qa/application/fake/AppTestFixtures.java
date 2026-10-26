package com.campusoverflow.qa.application.fake;

import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.port.ViewCounter;
import com.campusoverflow.qa.application.query.AnswerRow;
import com.campusoverflow.qa.application.query.CommentRow;
import com.campusoverflow.qa.application.query.QuestionRow;
import com.campusoverflow.qa.application.query.QuestionSummaryView;
import com.campusoverflow.qa.application.query.TagView;
import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.CommentRepository;
import com.campusoverflow.qa.domain.TagRepository;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** 应用层测试用的补充替身：评论、标签、读模型、渲染器、浏览计数、可命中缓存。 */
public final class AppTestFixtures {

    private AppTestFixtures() {
    }

    public static class InMemoryComments implements CommentRepository {
        private final Map<Long, Comment> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Optional<Comment> findById(long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Comment save(Comment comment) {
            if (comment.id() == null) {
                comment.assignId(sequence.incrementAndGet());
            }
            store.put(comment.id(), comment);
            return comment;
        }
    }

    public static class RecordingTags implements TagRepository {
        public final List<Collection<String>> calls = new ArrayList<>();
        public final List<Integer> deltas = new ArrayList<>();

        @Override
        public void adjustUsage(Collection<String> tagNames, int delta) {
            calls.add(List.copyOf(tagNames));
            deltas.add(delta);
        }
    }

    public static class FakeRenderer implements ContentRenderer {
        @Override
        public String toSafeHtml(String markdown) {
            return "<p>" + markdown + "</p>";
        }

        @Override
        public String toPlainText(String markdown, int maxLength) {
            return markdown.length() <= maxLength ? markdown : markdown.substring(0, maxLength);
        }
    }

    public static class CountingViewCounter implements ViewCounter {
        public int count;

        @Override
        public void increment(long questionId) {
            count++;
        }
    }

    /** 可读可写的缓存替身，用于验证 Cache-Aside 的命中与回填。 */
    public static class MemoryCache implements QuestionListCache {
        private final Map<String, PageResult<QuestionSummaryView>> store = new HashMap<>();
        public int gets;
        public int puts;

        @Override
        public Optional<PageResult<QuestionSummaryView>> get(Long courseId, PageRequest page) {
            gets++;
            return Optional.ofNullable(store.get(key(courseId, page)));
        }

        @Override
        public void put(Long courseId, PageRequest page, PageResult<QuestionSummaryView> value) {
            puts++;
            store.put(key(courseId, page), value);
        }

        @Override
        public void invalidateAll() {
            store.clear();
        }

        private static String key(Long courseId, PageRequest page) {
            return courseId + ":" + page.page() + ":" + page.size();
        }
    }

    public static class FakeQaReadModel implements QaReadModel {
        public PageResult<QuestionRow> listResult = PageResult.empty(1, 10);
        public Optional<QuestionRow> question = Optional.empty();
        public List<AnswerRow> answers = new ArrayList<>();
        public List<CommentRow> comments = new ArrayList<>();
        public Map<String, Integer> myVotes = new HashMap<>();
        public List<TagView> tags = new ArrayList<>();
        public int listCalls;
        public int questionCalls;
        public int answerCalls;
        public int commentCalls;
        public int voteCalls;
        public int tagCalls;
        public int lastTagLimit;

        @Override
        public PageResult<QuestionRow> listQuestions(Long courseId, PageRequest page) {
            listCalls++;
            return listResult;
        }

        @Override
        public Optional<QuestionRow> findQuestion(long questionId) {
            questionCalls++;
            return question;
        }

        @Override
        public List<AnswerRow> findAnswers(long questionId) {
            answerCalls++;
            return answers;
        }

        @Override
        public List<CommentRow> findComments(long questionId) {
            commentCalls++;
            return comments;
        }

        @Override
        public Map<String, Integer> findMyVotes(long voterId, long questionId, Collection<Long> answerIds) {
            voteCalls++;
            return myVotes;
        }

        @Override
        public List<TagView> popularTags(int limit) {
            tagCalls++;
            lastTagLimit = limit;
            return tags;
        }
    }
}
