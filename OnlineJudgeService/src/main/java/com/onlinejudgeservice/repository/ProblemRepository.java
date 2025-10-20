package com.onlinejudgeservice.repository;

import com.onlinejudgeservice.entity.Problem;
import com.onlinejudgeservice.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findDistinctByTagsIn(Collection<Tag> tags);
}
