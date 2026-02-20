package br.com.danieleleao.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.danieleleao.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();
        if (servletPath.startsWith("/tasks/")) {

            // pegar autenticao
            var authorization = request.getHeader("Authorization");

            byte[] authDecode = Base64.getDecoder().decode(authorization);
            var authString = new String(authDecode);

            System.out.println("Authorization");
            System.out.println(authString);

            String[] credentials = authString.split(":");
            var username = credentials[0];
            var password = credentials[1];
            System.out.println(username);
            System.out.println(password);

            var user_password = authorization.substring("Basic ".length()).trim();
            System.out.println("Authorization");
            System.out.println(user_password);
            // validar utilizador
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "utilizador sem autorizacao");
            } else {
                // validar senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (!passwordVerify.verified) {
                    // segue viagem
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "senha invalida");
                    

                }

            }
        }else{
            filterChain.doFilter(request, response);
        }
    }
}
