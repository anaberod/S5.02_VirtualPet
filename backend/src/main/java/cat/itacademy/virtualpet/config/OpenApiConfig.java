package cat.itacademy.virtualpet.config; // Paquete de configuración (Spring escaneará esta clase)

/* ======================= IMPORTS ======================= */
import io.swagger.v3.oas.models.OpenAPI;                       // Objeto raíz del documento OpenAPI (la “descripción” de tu API)
import io.swagger.v3.oas.models.Components;                    // Sección de componentes reutilizables (securitySchemes, schemas, etc.)
import io.swagger.v3.oas.models.info.Info;                     // Metadatos de la API (título, descripción, versión)
import io.swagger.v3.oas.models.info.Contact;                  // Información de contacto (opcional)
import io.swagger.v3.oas.models.security.SecurityRequirement;  // Requisito de seguridad aplicado a operaciones/paths
import io.swagger.v3.oas.models.security.SecurityScheme;       // Definición de un esquema de seguridad (Bearer JWT en nuestro caso)
import org.springframework.context.annotation.Bean;            // Marca el método como proveedor de un bean
import org.springframework.context.annotation.Configuration;    // Marca la clase como configuración de Spring


@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI virtualPetOpenAPI() {


        Info apiInfo = new Info()
                .title("VirtualPet API")
                .description("API REST para la app de Mascota Virtual (Auth por email + JWT).")
                .version("v1")
                .contact(new Contact()
                        .name("Equipo VirtualPet")
                        .email("soporte@virtualpet.local")
                        .url("https://github.com/anaberod/S5.02_VirtualPet")
                );


        SecurityScheme bearerScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");


        Components components = new Components()
                .addSecuritySchemes("bearerAuth", bearerScheme);


        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");


        return new OpenAPI()
                .info(apiInfo)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
