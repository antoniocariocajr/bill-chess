package com.bill.bill_chess.service; // ou seu package

import com.bill.bill_chess.config.LocalStockfishProps; // ajuste o import
import com.bill.bill_chess.core.MoveEngine;
import com.bill.bill_chess.exception.ChessEngineException;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.time.Duration;

@Slf4j
@Primary
@Service
public class LocalStockfishService implements MoveEngine {

    private final LocalStockfishProps props;
    private final Process process;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /* ====== 1) INJEÇÃO DA PROPRIEDADE ====== */
    public LocalStockfishService(LocalStockfishProps props) throws IOException {
        this.props = props;

        ProcessBuilder pb = new ProcessBuilder(props.getExe()); // agora vindo do .properties
        pb.redirectErrorStream(true);
        this.process = pb.start();

        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.writer = new OutputStreamWriter(process.getOutputStream());

        handshake();
    }

    private void handshake() throws IOException {
        send("uci");
        waitFor("uciok");
        send("isready");
        waitFor("readyok");
        log.info("Stockfish local inicializado com executável: {}", props.getExe());
    }

    @Override
    public Mono<String> bestMove(String fen, int depth) {
        return Mono.fromCallable(() -> {
            send("position fen " + fen);
            send("go depth " + depth);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    return line.split(" ")[1];
                }
            }
            throw new ChessEngineException("Nenhum bestmove recebido");
        })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10));
    }

    private void send(String cmd) throws IOException {
        writer.write(cmd + "\n");
        writer.flush();
    }

    private void waitFor(String token) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(token))
                return;
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        send("quit");
        process.destroy();
        reader.close();
        writer.close();
    }
}