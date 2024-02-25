package website.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import website.pojo.UserDO;

/**
 * 用于数据表userlist的CRUD（MyBatis-Plus配置）
 */
@Repository
public interface UserMapper extends BaseMapper<UserDO> {
}