package website.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;


@Configuration
@EnableWebSecurity
@Component
public class SecurityConfig {
    @Autowired
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Autowired
    private MyAuthenticationFilter myAuthenticationFilter;

    /**
     * 设置访问页面（发送请求）时网站的行为
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin((login) -> login
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .successHandler(myAuthenticationSuccessHandler)
                .failureHandler(new MyAuthenticationFailureHandler())
        );
        httpSecurity.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/user/register", "/user/login").permitAll()
                .anyRequest().authenticated()
        );
        httpSecurity.csrf((csrf) -> csrf.disable());
        httpSecurity.exceptionHandling((handle) -> handle.authenticationEntryPoint(new MyAuthenticationEntryPoint()));
        httpSecurity.addFilterBefore(myAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }


    /**
     * 密钥加密方式
     * @return
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
