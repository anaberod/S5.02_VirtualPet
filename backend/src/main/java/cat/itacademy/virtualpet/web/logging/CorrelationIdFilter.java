package cat.itacademy.virtualpet.web.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {
    private static final String KEY = "corrId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(KEY, id);
        try {
            if (response instanceof HttpServletResponse resp) {
                resp.setHeader("X-Correlation-Id", id);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(KEY);
        }
    }
}
