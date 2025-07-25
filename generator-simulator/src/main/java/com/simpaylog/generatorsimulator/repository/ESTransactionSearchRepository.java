package com.simpaylog.generatorsimulator.repository;

import com.simpaylog.generatorsimulator.dto.TransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ESTransactionSearchRepository extends ElasticsearchRepository<TransactionLog, String> {
    Page<TransactionLog> findAllByOrderByUserIdAscTimestampAsc(Pageable pageable);
}
