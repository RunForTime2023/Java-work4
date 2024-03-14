package website.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.bytedeco.javacv.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import website.mapper.*;
import website.pojo.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

@RestController
public class Controller {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private LikeMapper likeMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private JwtDTO jwtDTO;
    private List<String> deleteList;

    /**
     * 注册
     *
     * @param username 账号
     * @param password 密码
     * @return
     */
    @PostMapping("/user/register")
    @Transactional
    public ModelMap register(@RequestParam("username") String username, @RequestParam("password") String password) {
        ModelMap result = new ModelMap();
        if (!username.isEmpty() && !password.isEmpty()) {
            QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            List<UserDO> userList = userMapper.selectList(queryWrapper);
            if (userList.isEmpty()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                UserDO user = new UserDO(username, encoder.encode(password));
                userMapper.insert(user);
                result.addAttribute("status", new StatusDTO(1, "success"));
            } else {
                result.addAttribute("status", new StatusDTO(-2, "用户名已存在"));
            }
        } else {
            result.addAttribute("status", new StatusDTO(-3, "用户名或密码为空"));
        }
        return result;
    }

    /**
     * 用户信息
     *
     * @param userId 用户ID
     * @return
     */
    @GetMapping("/user/info")
    public ModelMap getUserInfo(@RequestParam("user_id") String userId) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        ModelMap result = new ModelMap();
        queryWrapper.eq("id", userId).select("id", "username", "avatar_url", "created_at", "updated_at", "deleted_at");
        UserInfoVO userDetail = userMapper.selectOne(queryWrapper).turnType2();
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", userDetail);
        return result;
    }

    /**
     * 上传头像
     *
     * @param token 当前登录用户的JWT token，下同
     * @param file  文件
     * @return
     */
    @PutMapping("/user/avatar/upload")
    @Transactional
    public ModelMap saveUserPic(@RequestHeader("Access-Token") String token, @RequestParam("data") MultipartFile file) {
        ModelMap result = new ModelMap();
        try {
            if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
                UserDO user = userMapper.selectById(jwtDTO.getUserId(token));
                user.setAvatarUrl();
                user.setUpdatedAt();
                File file1 = new File(user.getAvatarUrl());
                file1 = new File(file1.getAbsolutePath());
                if (!file1.exists()) {
                    file1.createNewFile();
                }
                file.transferTo(file1);
                BufferedImage image = ImageIO.read(file1);
                if (image == null) {
                    throw new Exception("图片文件为空");
                } else {
                    ImageIO.write(image, "jpg", file1);
                    userMapper.updateById(user);
                    result.addAttribute("status", new StatusDTO(1, "success"));
                }
            } else {
                throw new Exception("不是图片");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result.addAttribute("status", new StatusDTO(-5, "图片上传失败，可能是格式有误或已损坏。"));
        }
        return result;
    }

    /**
     * 关注/取消关注
     *
     * @param toUserId
     * @param isFollowed 操作，0表示关注，1表示取消关注
     * @return
     */
    @PostMapping("/relation/action")
    @Transactional
    public ModelMap saveFollow(@RequestHeader("Access-Token") String token, @RequestParam("to_user_id") String toUserId, @RequestParam("action_type") int isFollowed) {
        ModelMap result = new ModelMap();
        QueryWrapper<FollowDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("following_id", jwtDTO.getUserId(token)).eq("follower_id", toUserId);
        FollowDO follow1 = followMapper.selectOne(queryWrapper);
        if (follow1 == null && isFollowed == 0) {
            FollowDO follow2 = new FollowDO(jwtDTO.getUserId(token), toUserId);
            followMapper.insert(follow2);
            result.addAttribute("status", new StatusDTO(1, "success"));
        } else if (follow1 != null && isFollowed == 1) {
            followMapper.delete(queryWrapper);
            result.addAttribute("status", new StatusDTO(1, "success"));
        } else {
            result.addAttribute("status", new StatusDTO(-9, "unknown error"));
        }
        return result;
    }

    /**
     * 关注列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/following/list")
    public ModelMap listFollowing(@RequestParam("user_id") String userId) {
        ModelMap result = new ModelMap();
        QueryWrapper<FollowDO> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("following_id", userId);
        List<FollowDO> followList = followMapper.selectList(followQueryWrapper);
        List<FollowVO> userDetailList = new ArrayList<>();
        for (FollowDO each : followList) {
            QueryWrapper<UserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", each.getFollowerId()).select("id", "username", "avatar_url");
            FollowVO userDetail = userMapper.selectOne(userQueryWrapper).turnType1();
            userDetailList.add(userDetail);
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("items", userDetailList);
        map.put("total", userDetailList.size());
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", map);
        return result;
    }

    /**
     * 粉丝列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/follower/list")
    public ModelMap listFans(@RequestParam("user_id") String userId) {
        ModelMap result = new ModelMap();
        QueryWrapper<FollowDO> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("follower_id", userId);
        List<FollowDO> fansList = followMapper.selectList(followQueryWrapper);
        List<FollowVO> userDetailList = new ArrayList<>();
        for (FollowDO each : fansList) {
            QueryWrapper<UserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", each.getFollowingId()).select("id", "username", "avatar_url");
            FollowVO userDetail = userMapper.selectOne(userQueryWrapper).turnType1();
            userDetailList.add(userDetail);
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("items", userDetailList);
        map.put("total", userDetailList.size());
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", map);
        return result;
    }

    /**
     * 好友列表
     *
     * @param token 当前登录用户的JWT token
     * @return
     */
    @GetMapping("/friends/list")
    public ModelMap listFriend(@RequestHeader("Access-Token") String token) {
        ModelMap result = new ModelMap();
        QueryWrapper<FollowDO> followQueryWrapper1 = new QueryWrapper<>(), followQueryWrapper2 = new QueryWrapper<>();
        followQueryWrapper1.eq("follower_id", jwtDTO.getUserId(token)); //粉丝
        followQueryWrapper2.eq("following_id", jwtDTO.getUserId(token)); //关注的人
        List<FollowDO> result1 = followMapper.selectList(followQueryWrapper1), result2 = followMapper.selectList(followQueryWrapper2);
        List<FollowVO> friendList = new ArrayList<>();
        for (FollowDO each1 : result1) {
            for (FollowDO each2 : result2) {
                if (each1.getFollowingId().equals(each2.getFollowerId())) {
                    QueryWrapper<UserDO> userQueryWrapper = new QueryWrapper<>();
                    userQueryWrapper.eq("id", each1.getFollowingId()).select("id", "username", "avatar_url");
                    FollowVO userDetail = userMapper.selectOne(userQueryWrapper).turnType1();
                    friendList.add(userDetail);
                    break;
                }
            }
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("items", friendList);
        map.put("total", friendList.size());
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", map);
        return result;
    }

    /**
     * 评论
     *
     * @param token
     * @param videoId
     * @param commentId 针对的评论ID
     * @param content   评论正文
     * @return
     */
    @PostMapping("/comment/publish")
    @Transactional
    public ModelMap saveComment(@RequestHeader("Access-Token") String token, @RequestParam(value = "video_id", required = false) String videoId, @RequestParam(value = "comment_id", required = false) String commentId, @RequestParam("content") String content) {
        if (videoId == null || videoId.isEmpty()) {
            //父评论的子评论数+1
            CommentDO comment1 = commentMapper.selectById(commentId);
            comment1.setChildCount(comment1.getChildCount() + 1);
            commentMapper.updateById(comment1);
            //视频的评论总数+1
            VideoDO video = videoMapper.selectById(comment1.getVideoId());
            video.setCommentCount(video.getCommentCount() + 1);
            videoMapper.updateById(video);
            //添加新评论
            CommentDO comment2 = new CommentDO(jwtDTO.getUserId(token), comment1.getVideoId(), commentId, content);
            commentMapper.insert(comment2);
        } else if (commentId == null || commentId.isEmpty()) {
            //视频的评论总数+1
            VideoDO video = videoMapper.selectById(videoId);
            video.setCommentCount(video.getCommentCount() + 1);
            videoMapper.updateById(video);
            CommentDO comment = new CommentDO(jwtDTO.getUserId(token), videoId, "0", content);
            commentMapper.insert(comment);
        } else {
            CommentDO comment = new CommentDO(jwtDTO.getUserId(token), videoId, commentId, content);
            commentMapper.insert(comment);
        }
        ModelMap result = new ModelMap();
        result.put("status", new StatusDTO(1, "success"));
        return result;
    }

    /**
     * 获取评论（不含子评论）
     *
     * @param commentId
     * @return
     */
    @GetMapping("/comment/list")
    public ModelMap getComment(@RequestParam("comment_id") String commentId) {
        ModelMap result = new ModelMap();
        CommentDO comment = commentMapper.selectById(commentId);
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("items", comment);
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", map);
        return result;
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    @DeleteMapping("/comment/delete")
    @Transactional
    public ModelMap removeComment(@RequestParam("comment_id") String commentId) {
        String videoId = commentMapper.selectById(commentId).getVideoId();
        deleteList = new ArrayList<>();
        count(commentId);
        deleteList.add(commentId);
        commentMapper.deleteBatchIds(deleteList);
        VideoDO video = videoMapper.selectById(videoId);
        video.setCommentCount(video.getCommentCount() - deleteList.size());
        videoMapper.updateById(video);
        ModelMap result = new ModelMap();
        result.put("status", new StatusDTO(1, "success"));
        return result;
    }

    /**
     * 点赞
     *
     * @param videoId
     * @param commentId
     * @return
     */
    @PostMapping("/like/action")
    @Transactional
    public ModelMap saveLike(@RequestHeader("Access-Token") String token, @RequestParam(value = "video_id", required = false) String videoId, @RequestParam(value = "comment_id", required = false) String commentId) {
        ModelMap result = new ModelMap();
        QueryWrapper<LikeDO> queryWrapper = new QueryWrapper<>();
        if (videoId == null || videoId.isEmpty()) {
            queryWrapper.eq("user_id", jwtDTO.getUserId(token)).eq("comment_id", commentId);
            LikeDO like = likeMapper.selectOne(queryWrapper);
            if (like == null) {
                CommentDO comment = commentMapper.selectById(commentId);
                comment.setLikeCount();
                comment.setUpdatedAt();
                commentMapper.updateById(comment);
                like = new LikeDO(jwtDTO.getUserId(token), null, commentId);
                likeMapper.insert(like);
                result.addAttribute("status", new StatusDTO(1, "success"));
            } else {
                result.addAttribute("status", new StatusDTO(-6, "不可重复点赞"));
            }
        } else if (commentId == null || commentId.isEmpty()) {
            queryWrapper.eq("user_id", jwtDTO.getUserId(token)).eq("video_id", videoId);
            LikeDO like = likeMapper.selectOne(queryWrapper);
            if (like == null) {
                VideoDO video = videoMapper.selectById(videoId);
                video.setLikeCount();
                video.setUpdatedAt();
                videoMapper.updateById(video);
                like = new LikeDO(jwtDTO.getUserId(token), videoId);
                likeMapper.insert(like);
                result.addAttribute("status", new StatusDTO(1, "success"));
            } else {
                result.addAttribute("status", new StatusDTO(-8, "不可重复点赞"));
            }
        }
        return result;
    }

    /**
     * 点赞列表
     *
     * @param userId
     * @return
     */
    @GetMapping("/like/list")
    public ModelMap listLike(@RequestParam("user_id") String userId) {
        QueryWrapper<LikeDO> likeQueryWrapper = new QueryWrapper<>();
        likeQueryWrapper.eq("user_id", userId).isNotNull("video_id");
        List<LikeDO> likeList = likeMapper.selectList(likeQueryWrapper);
        List<VideoDO> videoList = new ArrayList<>();
        for (LikeDO each : likeList) {
            VideoDO video = videoMapper.selectById(each.getVideoId());
            videoList.add(video);
        }
        ModelMap result = new ModelMap(), temp = new ModelMap();
        temp.addAttribute("items", videoList);
        temp.addAttribute("total", videoList.size());
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", temp);
        return result;
    }

    /**
     * 投稿
     *
     * @param token
     * @param file 视频文件
     * @param title 视频标题
     * @param description 视频简介
     * @return
     */
    @PostMapping("/video/publish")
    @Transactional
    public ModelMap saveVideo(@RequestHeader("Access-Token") String token, @RequestParam("data") MultipartFile file, @RequestParam("title") String title, @RequestParam("description") String description) {
        ModelMap result = new ModelMap();
        try {
            VideoDO video = new VideoDO(jwtDTO.getUserId(token), title, description);
            videoMapper.insert(video);
            video.setVideoUrl();
            video.setCoverUrl();
            File file1 = new File(video.getVideoUrl());
            file1 = new File(file1.getAbsolutePath());
            if (!file1.exists()) {
                file1.createNewFile();
            }
            file.transferTo(file1);
            Tika tika = new Tika();
            MediaType mediaType = MediaType.parse(tika.detect(file1));
            if (mediaType.getType().equals("video")) {
                // 提取第一帧作为封面
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file1);
                grabber.start();
                Frame frame = grabber.grabImage();
                if (frame == null) {
                    videoMapper.deleteById(video.getId());
                    file1.deleteOnExit();
                    throw new Exception("视频文件为空");
                }
                // 转换为Java 2D图像，然后保存封面
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bufferedImage = converter.convert(frame);
                File cover = new File(video.getCoverUrl());
                ImageIO.write(bufferedImage, "jpg", cover);
                grabber.stop();

                videoMapper.updateById(video);
                redisTemplate.opsForZSet().add("rank", video.getId(), video.getVisitCount());
                result.addAttribute("status", new StatusDTO(1, "success"));
            } else {
                videoMapper.deleteById(video.getId());
                file1.deleteOnExit();
                throw new Exception("文件格式有误");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result.addAttribute("status", new StatusDTO(-7, "视频上传失败，可能是格式错误或已损坏"));
        }
        return result;
    }

    /**
     * 投稿列表
     *
     * @param userId
     * @param pageNum  页面尺寸
     * @param pageSize 页码
     * @return
     */
    @GetMapping("/video/list")
    public ModelMap listPublish(@RequestParam("user_id") String userId, @RequestParam("page_num") int pageNum, @RequestParam("page_size") int pageSize) {
        QueryWrapper<VideoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<VideoDO> videoList = videoMapper.selectList(queryWrapper), tempList = new ArrayList<>();
        int tempPage = 0;
        ModelMap tempModel = new ModelMap(), result = new ModelMap();
        for (VideoDO each : videoList) {
            tempList.add(each);
            if (tempList.size() == pageSize) {
                tempPage++;
                if (tempPage > pageNum) {
                    break;
                }
                tempList.clear();
            }
        }
        if (tempPage > pageNum || tempPage == pageNum && !tempList.isEmpty()) {
            tempModel.addAttribute("items", tempList);
            tempModel.addAttribute("total", videoList.size());
            result.addAttribute("status", new StatusDTO(1, "success"));
            result.addAttribute("data", tempModel);
        } else {
            result.addAttribute("status", new StatusDTO(-8, "页码错误"));
        }
        return result;
    }

    /**
     * 热门排行榜
     *
     * @return
     */
    @GetMapping("/video/popular")
    public ModelMap listPopular() {
        Set<String> popular = redisTemplate.opsForZSet().reverseRange("rank", 0, -1);
        List<VideoDO> videoList = new ArrayList<>();
        for (String each : popular) {
            videoList.add(videoMapper.selectById(each));
        }
        ModelMap result = new ModelMap(), temp = new ModelMap();
        temp.addAttribute("items", videoList);
        result.addAttribute("status", new StatusDTO(1, "success"));
        result.addAttribute("data", temp);
        return result;
    }

    /**
     * 搜索视频（搜索记录用Redis保存）
     *
     * @param keywords 关键词
     * @param pageSize 页面尺寸
     * @param pageNum  页码（从0开始）
     * @return
     */
    @PostMapping("/video/search")
    @Transactional
    public ModelMap searchVideo(@RequestParam("keywords") String keywords, @RequestParam("page_size") int pageSize, @RequestParam("page_num") int pageNum) {
        QueryWrapper<VideoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title", keywords).or().like("description", keywords);
        List<VideoDO> videoList = videoMapper.selectList(queryWrapper), tempList = new ArrayList<>();
        int tempPage = 0;
        ModelMap tempModel = new ModelMap(), result = new ModelMap();
        for (VideoDO each : videoList) {
            tempList.add(each);
            if (tempList.size() == pageSize) {
                tempPage++;
                if (tempPage > pageNum) {
                    break;
                }
                tempList.clear();
            }
        }
        if (tempPage > pageNum || tempPage == pageNum && !tempList.isEmpty()) {
            tempModel.addAttribute("items", tempList);
            tempModel.addAttribute("total", videoList.size());
            result.addAttribute("status", new StatusDTO(1, "success"));
            result.addAttribute("data", tempModel);
//            redisTemplate.setEnableTransactionSupport(true);
//            redisTemplate.multi();
//            redisTemplate.opsForList().remove("HistorySearch", 0, keywords);
//            redisTemplate.opsForList().leftPush("HistorySearch", keywords);
//            redisTemplate.exec();
            redisTemplate.execute(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    redisTemplate.multi();
                    redisTemplate.opsForList().remove("HistorySearch", 0, keywords);
                    redisTemplate.opsForList().leftPush("HistorySearch", keywords);
                    return redisTemplate.exec();
                }
            });
        } else {
            result.addAttribute("status", new StatusDTO(-8, "页码错误"));
        }
        return result;
    }

    /**
     * 递归删除子评论
     *
     * @param parentId 当前父评论ID
     */
    public void count(String parentId) {
        QueryWrapper<CommentDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        List<CommentDO> commentList = commentMapper.selectList(queryWrapper);
        for (CommentDO each : commentList) {
            count(each.getId());
            deleteList.add(each.getId());
        }
    }
}