package com.citron.javaintegrationsalesforce.repository;

import com.citron.javaintegrationsalesforce.model.ApiConnect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiConnectRepository extends JpaRepository<ApiConnect, Long> {

    @Query(
            value = "SELECT * FROM api_connect LIMIT 1",
            nativeQuery=true
    )
    public ApiConnect getRecord();

    @Query(
            value = "SELECT * FROM api_connect WHERE expired =:condition  LIMIT 1",
            nativeQuery=true
    )
    public ApiConnect getExpired(@Param("condition") Boolean condition);
}
