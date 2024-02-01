package com.ssafy.backend.room.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.common.exception.BaseException;
import com.ssafy.backend.common.exception.MyException;
import com.ssafy.backend.room.model.domain.Answer;
import com.ssafy.backend.room.model.domain.Question;
import com.ssafy.backend.room.model.dto.AnswerDto;
import com.ssafy.backend.room.model.dto.ConnectionDto;
import com.ssafy.backend.room.model.dto.QuestionDto;
import com.ssafy.backend.room.model.dto.RoomEnterDto;
import com.ssafy.backend.room.model.repository.AnswerRepository;
import com.ssafy.backend.room.model.repository.QuestionRepository;
import com.ssafy.backend.user.model.dto.OpenviduRequestDto;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.ssafy.backend.common.response.BaseResponseStatus.NOT_EXIST_SESSION;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    private OpenVidu openvidu;

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    @Override
    public String enterDefaultroom(RoomEnterDto roomEnterDto) throws Exception {
        openvidu.fetch();
        Session session = openvidu.getActiveSession(roomEnterDto.getSessionName());
        if(session == null){
            // 방이 존재하지 않다면 생성하라
            HashMap<String,String> SessionPropertyJson = roomEnterDto.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        }
        ConnectionProperties properties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(properties);
        return connection.getToken();
    }

    @Override
    public ConnectionDto enterRandomroom(RoomEnterDto roomEnterDto) throws Exception {
        String sessionName = roomEnterDto.getSessionName();
        Session session;

        // 세션에 해당하는 방이 존재하는지 확인
        sessionName = getRandomroom(sessionName);
        roomEnterDto.setSessionName(sessionName);
        System.out.println("세션이름: "+sessionName);
        openvidu.fetch();
        session = openvidu.getActiveSession(sessionName);

        if(session == null){    // 방이 존재하지 않다면 생성하라
            HashMap<String,String> SessionPropertyJson = roomEnterDto.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        } else{                 // 새로고침할떄 다른 세션에 있는 나의 연결을 삭제한다.
            openvidu.fetch();
            List<Session> activeSessions = openvidu.getActiveSessions();
            String prevSession = roomEnterDto.getPrevSession();
            String prevConnectionId = roomEnterDto.getPrevConnectionId();
            for(Session s : activeSessions){ // 기존의 연결을 찾아서 삭제한다
                if(s.getSessionId().equals(prevSession)){ // 세션을 찾았다면
                    try{
                        System.out.println("기존의 연결을 끊습니다.");
                        //TODO: 연결을 안끊어진다. 이걸 끊어야한다
                        System.out.println(prevConnectionId);
                        s.forceDisconnect(prevConnectionId);
                    } catch (Exception e){
                        System.out.println("이미 연결이 끊어져있습니다.");
                    }
                }
            }
        }

        openvidu.fetch();
        session = openvidu.getActiveSession(sessionName);
        if(session == null){    // 이 세션이 나혼자 있었으면 연결이 끊기면서 방도 사라질수있으므로 , 다시 방을 만든다
            System.out.println("방이 사라졌으므로 새로 생성합니다.");
            HashMap<String,String> SessionPropertyJson = roomEnterDto.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        }

        ConnectionProperties properties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(properties);
        System.out.println("이번에 새로생긴 연결: "+connection.getConnectionId());
        ConnectionDto connectionDto = new ConnectionDto(connection.getConnectionId(),sessionName,connection.getToken());
        return connectionDto;
    }

    @Override
    public String enterMoccojiroom(RoomEnterDto roomEnterDto) throws Exception {
        String SessionName = roomEnterDto.getSessionName();
        Session session;
        session = openvidu.getActiveSession(SessionName);

        if(session == null){
            // 길드방이 존재하지 않다면 생성하라 (길드방에 아무도 없어서 세션이 종료된 상태라면 생성하라)
            HashMap<String,String> SessionPropertyJson = roomEnterDto.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        }

        ConnectionProperties properties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(properties);

        return connection.getToken();
    }

    @Override
    public QuestionDto askQuestion(QuestionDto questionDto) throws Exception {
        String sessionId = questionDto.getSession();
        Session session;

        openvidu.fetch();
        session = openvidu.getActiveSession(sessionId);
        if(session == null){
            throw new BaseException(NOT_EXIST_SESSION);
        }

        // DB에 질문 저장
        Question question = saveQuestion(questionDto);
        String questionId = question.getQuestionId();
        questionDto.setQuestionId(questionId);

        ObjectMapper om = new ObjectMapper();
        OpenviduRequestDto openviduRequestDto = new OpenviduRequestDto(sessionId,"question",om.writeValueAsString(questionDto));
        URI uri = UriComponentsBuilder
                .fromUriString(OPENVIDU_URL)
                .path("/openvidu/api/signal")
                .encode()
                .build()
                .toUri();

        String secret = "Basic " + OPENVIDU_SECRET;
        secret = Base64.getEncoder().encodeToString(secret.getBytes());

        RequestEntity<String> requestEntity = RequestEntity
                .post(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic T1BFTlZJRFVBUFA6TVlfU0VDUkVU")
                .body(openviduRequestDto.toJson());

        System.out.println(openviduRequestDto.toJson());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(uri, requestEntity, Object.class);
        return questionDto;
    }

    @Override
    public AnswerDto answerQuestion(AnswerDto answerDto) throws Exception {
        String sessionId = answerDto.getSession();
        Session session;

        openvidu.fetch();
        session = openvidu.getActiveSession(sessionId);
        if(session == null){
            throw new BaseException(NOT_EXIST_SESSION);
        }

        // DB에 대답 저장
        Answer answer = saveAnswer(answerDto);
        String answerId = answer.getAnswerId();
        answerDto.setAnswerId(answerId);
        ObjectMapper om = new ObjectMapper();

        // 답변 문제번호 전송
        OpenviduRequestDto openviduRequestDto = new OpenviduRequestDto(sessionId,"answer",om.writeValueAsString(answerDto));
        URI uri = UriComponentsBuilder
                .fromUriString(OPENVIDU_URL)
                .path("/openvidu/api/signal")
                .encode()
                .build()
                .toUri();

        String secret = "Basic " + OPENVIDU_SECRET;
        secret = Base64.getEncoder().encodeToString(secret.getBytes());

        RequestEntity<String> requestEntity = RequestEntity
                .post(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic T1BFTlZJRFVBUFA6TVlfU0VDUkVU")
                .body(openviduRequestDto.toJson());

        System.out.println(openviduRequestDto.toJson());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(uri, requestEntity, Object.class);

        return answerDto;
    }

    @Override
    public Answer saveAnswer(AnswerDto answerDto) throws Exception {
        Answer answer = answerDto.toEntity();
        answer = answerRepository.save(answer);
        return answer;
    }

    @Override
    public Question saveQuestion(QuestionDto questionDto) throws Exception {
        Question question = questionDto.toEntity();
        question = questionRepository.save(question);
        System.out.println(question);
        return question;
    }


    @Override
    public List<AnswerDto> findAnswerByQuestionId(String questionId) throws Exception {
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        List<AnswerDto> answerDtos = answers.stream()
                .map(a -> new AnswerDto(a.getUserId(),a.getSession(),a.getAnswer(),a.getQuestionId()))
                .collect(Collectors.toList());
        return answerDtos;
    }

    @Override
    public void leaveSession(String userId, String token) throws Exception {
        openvidu.fetch();
        Session session = openvidu.getActiveSession(userId);
        System.out.println(userId + " user ID  " + token + " token");
        if(session!=null){
            List<Connection> connections = session.getActiveConnections();
            for (Connection connection : connections){
                if(token.equals(connection.getToken())){
                        session.forceDisconnect(connection);
                }
            }
        }
    }

    public String getRandomroom(String sessionName){
        Random random = new Random();
        int roomNumber = random.nextInt( 3) + 1; // 1-3
        return sessionName+roomNumber;
    }
}
