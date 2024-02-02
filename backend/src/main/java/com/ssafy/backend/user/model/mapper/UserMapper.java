package com.ssafy.backend.user.model.mapper;

// 추후 JPA 로 전환 예정

import com.ssafy.backend.user.model.dto.UserSignupDto;
import com.ssafy.backend.user.model.vo.UserViewVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    void signup(UserSignupDto userSignupDto) throws Exception;

    String getUserPassword(String loginUserId);

    void changePassword(String originUserId, String newSafePassword);

    List<UserViewVO> viewAllUser(String userIdForFriendBoard);
}
