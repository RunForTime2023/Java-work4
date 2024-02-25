package website.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import website.mapper.UserMapper;
import website.pojo.StatusDTO;
import website.pojo.UserDO;
import website.pojo.JwtDTO;

import java.io.IOException;
import java.util.HashMap;

@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private JwtDTO jwtDTO;
    @Autowired
    private UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User temp = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", temp.getUsername());
        UserDO user = userMapper.selectOne(queryWrapper);
        user.setUpdatedAt();
        String token = jwtDTO.createToken(user.getId(),user.getUsername());
        response.setHeader("Authorization", "Bearer" + token);
        HashMap<String, Object> result = new HashMap<>();
        result.put("status", new StatusDTO(1, "success"));
        result.put("token", token);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(new ObjectMapper().writeValueAsString(result));
    }
}
