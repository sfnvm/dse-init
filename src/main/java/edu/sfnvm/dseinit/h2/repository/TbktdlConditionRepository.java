package edu.sfnvm.dseinit.h2.repository;

import edu.sfnvm.dseinit.h2.model.TbktdlCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TbktdlConditionRepository extends JpaRepository<TbktdlCondition, Long> {
}
