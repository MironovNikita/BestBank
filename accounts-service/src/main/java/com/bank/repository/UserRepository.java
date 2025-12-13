package com.bank.repository;

import com.bank.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<User> findUserById(Long id);

    @Query("""
            SELECT * from users u
            WHERE u.email = :email
            """)
    Mono<User> getUserByEmail(@Param("email") String email);
}
