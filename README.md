# ♟️ Bill Chess API
>
> **Where Strategy Meets Silicon.**

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4.4+-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

---

## 📖 Sobre o Projeto aka "The Grandmaster"

Bem-vindo ao **Bill Chess**, uma API robusta e elegante projetada para orquestrar partidas de xadrez com precisão milimétrica. Não estamos falando apenas de mover peças; estamos falando de um sistema que entende a **alma do jogo**.

Este projeto não é apenas um backend; é um árbitro digital imparcial, um oponente formidável (integrado com Stockfish 🐟) e um gerenciador de estados complexos. Se você quer construir o próximo grande aplicativo de xadrez, o Bill Chess é o seu motor.

### ✨ O que faz ele brilhar?

* **Arbitragem Completa**: Validação de movimentos legais, xeque, xeque-mate, roque e *en passant*. Nada escapa.
* **Oponente IA Integrado**: Desafie o **Bot** alimentado pelo Stockfish. Ele não tem piedade (mas você pode configurar a profundidade 😉).
* **Arquitetura Limpa**: Código organizado, desacoplado e fácil de estender.
* **Documentação Viva**: Swagger UI integrado para você testar a API sem escrever uma linha de código frontend.

---

## 🚀 Stack Tecnológico

Construído sobre ombros de gigantes:

| Tecnologia | Função |
| :--- | :--- |
| **Java 21** | O coração robusto e moderno do sistema. |
| **Spring Boot 3** | Framework que traz agilidade e "mágica" para a configuração. |
| **MongoDB** | Persistência NoSQL para armazenar estados de jogo flexíveis. |
| **Stockfish** | O cérebro tático por trás do modo Bot. |
| **SpringDoc (Swagger)** | Documentação interativa e visual. |
| **Lombok** | Porque a vida é muito curta para escrever getters e setters. |

---

## 🛠️ Guia de Início Rápido

Prepare seu tabuleiro (terminal) e suas peças (IDE)!

### Pré-requisitos

* Java JDK 21 instalado.
* MongoDB rodando (localmente ou Docker).
* Maven (opcional, pois usamos o wrapper `mvnw`).
* **Binário do Stockfish** disponível no sistema (o serviço procura por ele para fazer a mágica acontecer).

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/bill-chess.git
cd bill-chess
```

### 2. Configure o Banco

Certifique-se de que o MongoDB está rodando na porta padrão `27017`. Se não, ajuste em `src/main/resources/application.properties`.

### 3. Build & Run

```bash
./mvnw clean spring-boot:run
```

Assim que vir o logo do Spring no console... **Xeque!** O servidor está de pé.

---

## 🔌 Documentação da API

Não acredita na nossa palavra? Teste você mesmo.

Acesse a interface interativa do Swagger UI e comece a fazer jogadas HTTP:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### Endpoints Principais

* `POST /api/chess/init`: Começa uma nova guerra... digo, partida.
* `POST /api/chess/{id}/move`: Faz um movimento humano.
* `POST /api/chess/{id}/bot/move`: Pede para o Bot responder (prepara-se para suar).
* `GET /api/chess/{id}`: Espia o estado atual do tabuleiro.
* `GET /api/chess/{id}/legal-moves`: Pergunta ao árbitro "pra onde posso ir?".

---

## 🧩 Arquitetura

O projeto segue princípios sólidos para manter a sanidade mental dos desenvolvedores:

* **Controller**: Recebe as requisições HTTP e devolve DTOs. Simples porteiro.
* **Service**: A lógica de negócios. Onde a validação acontece e as regras são aplicadas.
  * *Refatoração Recente*: Agora com logs elegantes (`@Slf4j`) e tratamento de exceções centralizado.
* **Domain**: O núcleo puro. Modelos como `Board`, `Piece`, `Move` que representam o xadrez real.
* **Persistence**: Camada que fala a língua do MongoDB.

---

## 🤝 Contribuindo

Quer ensinar um truque novo para esse cachorro velho?

1. Faça um **Fork**.
2. Crie uma **Branch** (`git checkout -b feature/nova-jogada`).
3. Faça o **Commit** (`git commit -m 'Adiciona gambito da rainha'`).
4. Faça o **Push** (`git push origin feature/nova-jogada`).
5. Abra um **Pull Request**.

---

<div align="center">
  <sub>Feito com ☕ e ♟️ por <b>Antonio</b> (e ajudinha do Antigravity).</sub>
</div>
