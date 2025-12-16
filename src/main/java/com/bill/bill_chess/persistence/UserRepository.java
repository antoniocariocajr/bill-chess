package com.bill.bill_chess.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("{ 'chessEntities.$id' : ?0 }")
    Optional<User> findByChessEntityId(ObjectId chessEntityId);

    @Query(value = "{ '_id' : ?0, 'chessEntities.$id' : ?1 }", exists = true)
    boolean existsByIdAndChessEntityId(String userId, String chessEntityId);
}
