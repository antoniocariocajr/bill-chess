package com.bill.bill_chess.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChessRepository extends MongoRepository<ChessEntity, String> {

}
