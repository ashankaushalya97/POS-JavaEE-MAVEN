package lk.ijse.dep.pos.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/api/v1/customer","/api/v1/item","/api/v1/order"})
public class CorsFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println(request.getMethod());
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods","OPTIONS, POST, GET, PUT, DELETE, UPDATE");
        response.setHeader("Access-Control-Allow-Headers","Content-Type");
        super.doFilter(request, response, chain);
    }
}
