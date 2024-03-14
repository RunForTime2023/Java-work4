package website.pojo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;

@Data
@Component
@ConfigurationProperties(prefix = "temp.jwt")
public class JwtDTO {
    private String secret;
    private String header;

    /**
     * 根据用户ID、用户名生成JWT Token
     * @param username
     * @return
     */
    public String createToken(String userId, String username) {
        Date now = new Date();
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", username);
        String resultToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(userId)
                .addClaims(map)
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
        return resultToken;
    }

    /**
     * 获取token中的用户名信息
     * @param token
     * @return
     */
    public String getUserId(String token) {
        if (StringUtils.isEmpty(token)) return "";
        try {
            String result = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 获取token中的用户名信息
     * @param request
     * @return
     */
    public String getUsername(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isEmpty(token)) return "";
        try {
            String result = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().get("name").toString();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * 核验token是否合法
     * @param token
     * @return
     */
    public boolean isTokenVaild(String token) {
        if (StringUtils.isEmpty(token)) return false;
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJwt(token);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
