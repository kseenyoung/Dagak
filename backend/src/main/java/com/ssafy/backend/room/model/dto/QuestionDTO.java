package com.ssafy.backend.room.model.dto;

import com.ssafy.backend.common.exception.BaseException;
import com.ssafy.backend.room.model.domain.Question;
import lombok.*;

import static com.ssafy.backend.common.response.BaseResponseStatus.FAIL_TO_CONNECT;

@Getter
@Builder
public class QuestionDTO {
    private String questionId;
    private String userId;
    private String session;
    private String data;

    public QuestionDTO() {
    }

    public QuestionDTO(String questionId, String userId, String session, String data) {
        this.questionId = questionId;
        this.userId = userId;
        this.session = session;
        this.data = data;
    }

    public void setUserId(String userId) {
        if(userId == null || userId.isEmpty()){
            throw new BaseException(FAIL_TO_CONNECT);
        } else {
            this.userId = userId;
        }

    }

    public void setSession(String session) {
        if(session == null || session.isEmpty()){
            throw new BaseException(FAIL_TO_CONNECT);
        } else {
            this.session = session;
        }
    }

    public void setData(String data) {
        if(data == null){
            throw new BaseException(FAIL_TO_CONNECT);
        } else {
            this.data = data;
        }
    }

    public QuestionDTO(String userId, String session, String data) {
        this.userId = userId;
        this.session = session;
        this.data = data;
    }

    public Question toEntity(){
        return Question.builder()
                .userId(this.userId)
                .session(this.session)
                .question(this.data)
                .build();
    }
}
