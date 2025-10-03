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

/**
 * Configuración de OpenAPI/Swagger.
 * - Define metadatos visibles en Swagger UI (título, descripción, versión).
 * - Registra el esquema de seguridad "bearerAuth" (Bearer token JWT).
 * - (Opcional) Aplica el requisito de seguridad por defecto a toda la API.
 */
@Configuration // Indica a Spring que esta clase contiene beans de configuración
public class OpenApiConfig {

    /**
     * Crea y expone un bean OpenAPI para personalizar la documentación.
     * Springdoc lo detecta y construye Swagger UI con esta info.
     */
    @Bean // Registra el objeto OpenAPI en el contexto de Spring
    public OpenAPI virtualPetOpenAPI() {

        // ----- Metadatos de la API (lo que verás arriba en Swagger UI) -----
        Info apiInfo = new Info()
                .title("VirtualPet API")                             // Título visible en Swagger UI
                .description("API REST para la app de Mascota Virtual (Auth por email + JWT).") // Breve descripción
                .version("v1")                                       // Versión de la API (no confundir con versión de app)
                .contact(new Contact()
                        .name("Equipo VirtualPet")                   // Nombre de contacto (opcional)
                        .email("soporte@virtualpet.local")           // Email de contacto (opcional)
                        .url("https://github.com/anaberod/S5.02_VirtualPet") // URL de referencia (opcional)
                );

        // ----- Esquema de seguridad: Bearer token (JWT) -----
        SecurityScheme bearerScheme = new SecurityScheme()
                .name("bearerAuth")                  // Nombre lógico del esquema (lo usaremos para referenciarlo)
                .type(SecurityScheme.Type.HTTP)      // Tipo HTTP (frente a APIKEY, OAUTH2, etc.)
                .scheme("bearer")                    // Subtipo: “bearer” → cabecera Authorization
                .bearerFormat("JWT");                // Formato informativo (no obligatorio), ayuda a las UIs

        // Registramos el esquema en la sección de componentes con la clave "bearerAuth"
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", bearerScheme);

        // ----- Requisito de seguridad por defecto -----
        // Esto indica a OpenAPI que, por defecto, las operaciones requieren el esquema "bearerAuth".
        // OJO: Esto es documental (para Swagger UI). Tu SecurityConfig sigue mandando.
        // Si prefieres NO marcar TODA la API como segura aquí (para que /auth/* no muestre el candado),
        // puedes comentar la línea addSecurityItem(...) y anotar solo los controladores protegidos con:
        //   @SecurityRequirement(name = "bearerAuth")
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth"); // Vincula el requisito al esquema llamado "bearerAuth"

        // Construimos el objeto OpenAPI completo
        return new OpenAPI()
                .info(apiInfo)                 // Metadatos
                .components(components)        // Componentes con nuestro esquema JWT
                .addSecurityItem(securityRequirement); // Aplica requisito por defecto (puedes quitarlo si no lo quieres global)
    }
}
