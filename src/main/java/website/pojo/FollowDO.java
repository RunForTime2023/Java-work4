package website.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data
@TableName("`followlist`")
public class FollowDO {
    private String followingId;
    private String followerId;
    public FollowDO(String followingId, String followerId) {
        this.followingId = followingId;
        this.followerId = followerId;
    }

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }
}
