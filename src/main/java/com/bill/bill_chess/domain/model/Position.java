package com.bill.bill_chess.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document
public class Position {
    @Id
    private String id;
    private int rank; // 0-7
    private int file; // 0-7 (a-h)

    public Position(int rank, int file) {
        this.rank = rank;
        this.file = file;
    }

    public Position fromNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        char fileChar = notation.charAt(0);
        char rankChar = notation.charAt(1);

        int file = fileChar - 'a';
        int rank = Character.getNumericValue(rankChar);

        if (!isValid()) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }

        return new Position(rank, file);
    }

    public String toNotation() {
        char fileChar = (char) ('a' + file);
        return "" + fileChar + rank;
    }

    public boolean isValid() {
        return rank >= 0 && rank <= 7 && file >= 0 && file <= 7;
    }

}
