package APP.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para redirigir todas las rutas no API a index.html
 * Esto permite que Angular maneje el routing del frontend
 */
@Controller
public class SpaRedirectController {

    @GetMapping(value = "/{path:(?!api|swagger-ui|v3|uploads).*}/**")
    public String redirect() {
        // Forward all non-API requests to Angular's index.html in browser subfolder
        return "forward:/browser/index.html";
    }
}

