package website.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import website.detailsservice.DetailsServiceImpl;
import website.pojo.JwtDTO;

import java.io.IOException;

@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtDTO jwtDTO;
    @Autowired
    private DetailsServiceImpl detailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = request.getHeader(jwtDTO.getHeader());
        String username = jwtDTO.getUsername(request);
        // 当token中的username不为空时进行验证token是否是有效的token
        if (!username.equals("") && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = detailsService.loadUserByUsername(username);
            if (jwtDTO.isTokenVaild(authToken)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // 放行给下个过滤器
        filterChain.doFilter(request, response);
    }
}