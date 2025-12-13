package com.bill.bill_chess.infra.swagger;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.bill.bill_chess.infra.swagger.OpenApiConstants.*;
import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@Configuration
@SecurityScheme(
        name   = SECURITY_SCHEME_NAME,
        type   = HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI chessOpenAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers())
                .tags(buildGlobalTags())
                .externalDocs(buildExternalDocs())
                .addSecurityItem(buildSecurityRequirement());
    }

    private Info buildInfo() {
        return new Info()
                .title("Bill Chess API")
                .description("API for Bill Chess Application")
                .version("1.0")
                .contact(new Contact()
                        .name(AUTHOR_NAME)
                        .url(AUTHOR_URL)
                        .email(AUTHOR_EMAIL))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }

     private List<Server> buildServers() {
        return List.of(
                new Server().url(DEV_SERVER_URL).description("Servidor de Desenvolvimento"),
                new Server().url(PROD_SERVER_URL).description("Servidor de Produção")
        );
    }

    private List<Tag> buildGlobalTags() {
        return List.of(
                new Tag().name("Chess").description("Endpoints relacionados às sessões de xadrez"),
                new Tag().name("User").description("Gestão de usuarios"),
                new Tag().name("Authentication").description("Autenticação dos usuarios do sistema")
        );
    }

    private ExternalDocumentation buildExternalDocs() {
        return new ExternalDocumentation()
                .description("Documentação completa no GitHub")
                .url(AUTHOR_URL+"/bill-chess/wiki");
    }

    private SecurityRequirement buildSecurityRequirement() {
        return new SecurityRequirement().addList(OpenApiConstants.SECURITY_SCHEME_NAME);
    }

}
