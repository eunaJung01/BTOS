package com.umc.btos.src.user;

import com.umc.btos.config.BaseException;
import com.umc.btos.config.BaseResponse;
import com.umc.btos.src.user.model.*;
import com.umc.btos.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static com.umc.btos.utils.ValidationRegex.*;

import com.umc.btos.config.secret.Secret; // 테스트용
import io.jsonwebtoken.*; // 테스트용
import static com.umc.btos.config.BaseResponseStatus.*; // 테스트용



@RestController
@RequestMapping("/btos/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;

    public UserController(UserProvider userProvider,
                          UserService userService,
                          JwtService jwtService){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * 회원가입 API
     * [POST] /btos/users
     */

    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq){

        // ***형식적 validation***
        // email 값 존재 검사
        if (postUserReq.getEmail() == null) { // null 값 시 오류 메시지
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        // email 형식 검사
        if (!isRegexEmail(postUserReq.getEmail())) { // email@domain.xxx와 같은 형식인지 검사. 형식이 올바르지 않다면 에러 메시지
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }

        // nickname 값 존재 검사
        if (postUserReq.getNickName() == null) { // null 값 시 오류 메시지
            return new BaseResponse<>(POST_USERS_EMPTY_NICKNAME);
        }
        // nickname 형식 검사
        if (postUserReq.getNickName().length() > 10) { // 10글자 초과 시 오류 메시지
            return new BaseResponse<>(POST_USERS_INVALID_NICKNAME);
        }


        try {
            PostUserRes postUserRes = userService.createUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원 상태 변경(탈퇴 / 휴면 / 재활성화) API
     * [PATCH] /btos/users/status
     */
    /*
    @ResponseBody
    @PatchMapping("/status")
    public BaseResponse<String> deleteUser(@RequestBody PatchUserReq patchUserReq){
        // 형식적 validation은 클라이언트 단에서 처리
        try {
            //jwt에서 idx 추출.
            int userIdxByJwt = jwtService.getUserIdx();

            //userIdx와 접근한 유저가 같은지 확인
            if(userIdx != userIdxByJwt){
                return new BaseResponse<>(INVALID_USER_JWT);
            }

            PatchDeleteReq patchDeleteReq = new PatchDeleteReq(userIdx, user.getEmail(), user.getNickname(), user.getPassword());
            userService.deleteUser(patchDeleteReq);

            String result = "회원탈퇴가 완료되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

 */


    /**
     * 프로필 조회 API
     * [GET] /btos/users/:userIdx
     */

    @ResponseBody
    @GetMapping("/{userIdx}") //path variable
    public BaseResponse<GetUserRes> getUser(@PathVariable("userIdx") int userIdx) throws BaseException {

// *********************소셜 로그인으로 발급받은 jwt로 본인 인증이 됐다고 가정********************************
        String jwt = jwtService.createJwt(userIdx);
        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(Secret.JWT_SECRET_KEY)
                    .parseClaimsJws(jwt);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }
        int userIdxByJwt = claims.getBody().get("userIdx", Integer.class);

        // jwt에서 idx 추출.
        // int userIdxByJwt = jwtService.getUserIdx(); 소셜 로그인 테스트 후 주석해제.
        //userIdx와 접근한 유저가 같은지 확인
        if(userIdx != userIdxByJwt){
            return new BaseResponse<>(INVALID_USER_JWT);
        }
        //같다면 변경
// *********************소셜 로그인으로 발급받은 jwt로 본인 인증이 됐다고 가정********************************

        try {
            GetUserRes getUserRes = userProvider.getUser(userIdx);
            return new BaseResponse<>(getUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }

    }
}
