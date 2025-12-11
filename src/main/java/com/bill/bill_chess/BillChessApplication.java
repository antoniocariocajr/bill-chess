package com.bill.bill_chess;

import com.bill.bill_chess.config.LocalStockfishProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LocalStockfishProps.class)
public class BillChessApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillChessApplication.class, args);
	}

}
