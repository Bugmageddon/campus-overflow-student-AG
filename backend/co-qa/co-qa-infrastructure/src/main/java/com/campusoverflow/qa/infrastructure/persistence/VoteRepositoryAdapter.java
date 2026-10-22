package com.campusoverflow.qa.infrastructure.persistence;

import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Vote;
import com.campusoverflow.qa.domain.VoteRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepositoryAdapter implements VoteRepository {

    private final VoteJpaRepository jpa;

    public VoteRepositoryAdapter(VoteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Vote> find(long voterId, TargetType targetType, long targetId) {
        return jpa.findByVoterIdAndTargetTypeAndTargetId(voterId, targetType.name(), targetId)
                .map(e -> Vote.reconstitute(e.getId(), e.getVoterId(), TargetType.valueOf(e.getTargetType()),
                        e.getTargetId(), e.getValue(), e.getUpdatedAt()));
    }

    @Override
    public Vote save(Vote v) {
        VoteJpaEntity e = v.id() == null
                ? new VoteJpaEntity(v.voterId(), v.targetType().name(), v.targetId())
                : jpa.findById(v.id()).orElseThrow();
        e.setValue(v.value());
        e.setUpdatedAt(v.updatedAt());
        VoteJpaEntity saved = jpa.save(e);
        v.assignId(saved.getId());
        return v;
    }

    @Override
    public void delete(Vote v) {
        if (v.id() != null) {
            jpa.deleteById(v.id());
        }
    }
}
