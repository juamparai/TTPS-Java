package APP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TTPS - API Mascotas Perdidas")
                        .version("1.0")
                        .description("Documentación automática de la API - TTPS"))
                .servers(List.of(new Server().url("/TTPS_Raimundi_Suner").description("Application context path")));
    }
}

