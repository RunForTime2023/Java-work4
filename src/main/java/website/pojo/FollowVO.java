package website.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

/**
 * 返回关注者、被关注者和朋友列表时列表内用户的信息（仅含用户ID、昵称、头像地址）
 */
@AllArgsConstructor
public class FollowVO {
    private String id;
    private String username;
    @JsonProperty(value = "avatar_url")
    private String avatarUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setAvatarUrl() {
        this.avatarUrl = "/user/avatar/" + this.id.toString() + ".jpg";
    }
}