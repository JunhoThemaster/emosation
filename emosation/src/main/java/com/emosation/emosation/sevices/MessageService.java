package com.emosation.emosation.sevices;

import com.emosation.emosation.model.chat.ChatRoom;
import com.emosation.emosation.model.chat.Message;
import com.emosation.emosation.model.chat.MessageDTO;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.repository.MessageRepository;
import com.emosation.emosation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MessageService {


    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository  chatRepository;
    private final ChatService chatService;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository, ChatRepository chatRepository, ChatService chatService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.chatService = chatService;
    }


    @Transactional
    public void save(Long rdmsId,Long roomId, String sender, String content) {

        ChatRoom chatRoom = chatRepository.findById(roomId).orElseThrow(() ->  new RuntimeException("room not found"));

        User user = userRepository.findByEmail(sender);
        Message message = new Message();
        message.setRdmsId(rdmsId);
        message.setSender(user);
        message.setChatRoom(chatRoom);
        message.setContent(content);
        messageRepository.save(message);

    }






    public List<MessageDTO> findByChatRoom(Long roomId) {
        ChatRoom chatRoom = chatRepository.findById(roomId).orElseThrow(() ->  new RuntimeException("room not found"));

        List<Message> msgList = messageRepository.findByChatRoomOrderBySentedAtAsc(chatRoom);
//        for (Message msg : msgList) {
//
//            messageRepository.save(msg);
//        }

        List<MessageDTO> messageDTOList = msgList.stream()
                .map(message -> {
                    // senderEmail을 UserDTO로 변환
                    UserDTO senderDTO = new UserDTO(
                            message.getSender().getId(),
                            message.getSender().getName()
                            ,message.getSender().getEmail(),
                            message.getSender().getPics(),
                            message.getSender().getRegisterd_at().toString(),
                            message.getSender().getStatus()
                    );

                    // MessageDTO 객체 생성
                    return new MessageDTO(
                            message.getId(),
                            message.getContent(),
                            senderDTO,  // 발신자 정보는 UserDTO로
                            message.getSentedAt(),
                            roomId
                    );
                })
                .collect(Collectors.toList());

        return messageDTOList;

    }


    public Optional<Message> findByRoomAndRdmsId(Long roomId, Long rdmsId) {

        ChatRoom chatRoom = chatRepository.findById(roomId).orElseThrow(() ->  new RuntimeException("room not found"));
        System.out.println("chatroom : " + chatRoom);
        return messageRepository.findByRdmsIdAndChatRoom(rdmsId,chatRoom);
    }











}
