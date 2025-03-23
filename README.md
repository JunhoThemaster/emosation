# 이모세이션(emosation) 감정 분석형 채팅 서비스

## emotion + conversation 

## 프로젝트 개발 동기 및 설명



 **본 프로젝트는 1인 개발 프로젝트입니다.
 개발 동기는 간단합니다. 
 저는 채팅 어플리케이션을 통해 사람들과 소통할때, 텍스트 외 이모티콘과 부가 표현들을 통해서만 문자 너머에 담긴 상대의 의도와 감정을 알수있었습니다.
 저처럼 눈치없는 이들을 도와줄 어플리케이션은 없을까 생각해, OPEN AI 를 이용하여 텍스트의 감정을 분석해주는 인공지능 융합형 채팅어플리케이션을 구상하게 되었습니다.
 이 프로젝트의 성립을 위하여 OPENAI API를 사용하였고, 메세지들을 전달하여 분석 요청의 프롬프트를 static하게 만들어 해당 태스크만 수행하게 만들었습니다.
 다음 도전에서는 딥러닝과 머신러닝을 좀 더 학습하여 외부 API 의존성을 줄이도록 하려합니다.**


**기술 스택**
- JAVA(java17)
- SpringBoot + Jpa(SpringFramework)
- Thymeleaf
- JWT
- Redis
- Oracle DB(Oracle-xe18c)
- Docker
- AWS EC2(Linux/Unix)
- ELASTIC IP 
- OPENAI API
- WebSocket API
- Grafana 
- Promethes
- Actuator
- Jmeter
- SSL 

  
**개발 기간**
 - 2024/11/04 ~ 2025/02/07

   
# 개발 과정 
- 요구 사항 분석 및 데이터베이스 테이블 구조 형성
- 상향식 구조 설계 방식 채택 후 어플리케이션 구조 형성
- 사용할 스택 학습
- 애플리케이션 기본 기능 구현(CRUD)
- 실시간 채팅 서비스 구현(WebSocket API)
- 감정분석 기능 구현(OPENAI API)
- 메모리기반 저장 구현(Redis)
- EC2인스턴스 생성 보안규칙 편집,Elastic ip할당후 배포준비
- Docker-compose.yml 작성 후 환경변수 설정과 포트 맵핑
- 실행가능한 jar 생성 -> docker image 생성 -> docker 리포지토리에 업로드 -> ec2인스턴스에서 해당 이미지 pull
- 테스트




# 어플리케이션 주요 구현 기능
 - 기본적인 CRUD
 - Jwt 인증 로그인 방식 , 실시간 채팅(WebSocket 원시 API),실시간 채팅 내용 OPENAI API를 통해 분석 결과 도출 및 확인
   - ![스크린샷 2025-02-25 173949](https://github.com/user-attachments/assets/4a19c0cd-1e64-40f2-aa5e-39d7caf6721d)


 - RESTful API( URI 설계시 원칙을 완전히 따르지 않고 일부는 쿼리파라미터를 이용함)
 - 비동기 처리 fetch()
 - 마이 페이지
 - 친구 추가 
 - Redis와 RDBMS(Oracle) 을 이용한 메세지 저장 및 읽기
 - Redis 분산 Lock을  이용한 동시성 제어
   - ![image](https://github.com/user-attachments/assets/06ca08dd-273d-4f35-9567-566754a53814)

   
   
 - 관리자 페이지 - 최근 7일 사용자 수,현재 접속 사용자 수 , 회원 탈퇴 및 복구
   
 - 모니터링 메트릭 시각화
   - ![image](https://github.com/user-attachments/assets/b74c0eee-3cf4-4036-a872-b4fe2f572a7e)

 - Docker 이미지 빌드후 ec2인스턴스에서 실행가능
   - ![image](https://github.com/user-attachments/assets/d2a28b2c-31f4-44ee-ad11-ebdad6800fb2)
   - ec2인스턴스에 업로드 된 민감정보 파일들을 내부 컨테이너 파일에 마운트 하는 방식




------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
## 어플리케이션 구조 
  - ![image](https://github.com/user-attachments/assets/0c1056b0-dc1c-40dc-8ff3-8c7f84f8cf54)



# 회고 
 - AWS EC2 + Docker 를 이용해 배포를 하는 프로세스는 좋았으나 Oracle 연결시 Oracle 을 로컬 환경(내 PC) Oracle에 포트포워딩을 통한 연결해주는 부분이 아쉬웠습나다.
   이유는 로컬 환경에 변화에 따른 리스너 수정 등등이 번거로웠습니다. 그리고 계정을 미리 만들어놔야 한다는 단점, 내 로컬 환경이 oFf 상태면 당연하게도 컨테이너 내부 톰캣서버 off 라는 단점이 있었습니다.
   이는 웹 어플리케이션 이용시 사용자 입장에서 운영의 불규칙으로 인해 편의성을 망친다는 것을 알게 되었습니다.

   하지만 해당 과정을 통해 같은 ip내에 있지 않아도  포트포워딩을 통한 연결이 가능하다는 것 또한 배웠습니다.

    다음에는 RDS를 이용하면 해당문제를 해결할수 있을 것 같습니다.



------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 -  SOFT DELETE 삭제 방법을 이용해 user삭제시 레코드 자체를 삭제하는게 아닌 enum 필드에서만 삭제 되었다는 명시를 해줬었는데 , 이 부분에서 탈퇴 사용자가 오랜 기간 들어오지 않는다는 가정을 해본다면
    필요없는 레코드가 계속 존재하게 될 것입니다. 추후 많은 회원 수용시 이는 성능적인 측면에서 좋지 않은 영향을 주게 될 것 입니다.

    하지만 해당 과정을 통해 HARD DELETE 방법을 사용하지 않았기에 이 점을 이용하여  추후 데이터 복구를 할수 있다는 것을 알게 되었고 Redis를 통한 user정보 복구를 할수 있게 되었습니다.

     다음에는   @Scheduler 를 이용하여 삭제 사이클을 만들어 보면 좋을 것 같습니다. 예를 들어 로컬 시간을 반영하여 30일 이후 삭제 , 15일 이후 삭제.. 등등

    

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - 메세지 타입을 나누기위한 코드가 추후 메세지 타입이 늘어남에 따라 같이 늘어나게 될 것 같습니다. 하지만  메세지 json 객체를 서버에서 파싱하여 확인하고 타입별 액션을 지정해주는 로직을 통해 json객체를 주고받는게 당연하게도 익숙해졌고
   정보를 주고 받기 상당히 편하다는 것을 알게 되었습니다.




------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - 다대다 참조 문제를 풀어내면서 서로가 서로를 참조하려 하는 것을 끊어내는 방법을 알게 되었고, 중간 조인 테이블 생성을 통해 좀더 명시적이고 추후 작업시 수정이 가능하게 만드는 과정을 배웠고 다대다 관계를 풀어내는 방법 중 한가지를 알게되었습니다.

   다음에는 다대다 문제를 좀더 풀어내  만들어 보고 싶습니다.


------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - RESTful API 에 대한 개념을 배우게 되었습니다. REST원칙을 알게 되었고 URL 설계, 메서드 GET,POST,PUT,DELETE 각각에 대한 표준 역할 을 배웠습니다.

   다음에는 이러한 개념과 원칙을 좀 더 준수하며 API를 설계 하고 싶습니다.


   
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - Websocket API 사용을 통해 웹소켓의 연결과정에 대한 이해를 쌓았고 실시간 통신을 가능하게 만들었습니다. 이에 대한 문제로 메세지의 송수신 량 증가에 따른 데이터 처리를 Redis를 학습하게 되는 계기가 되었고, 학습 후 Redis를 이용하여 많은 양의 데이터 송수신시 빠른
   읽기와 쓰기를 구현할수 있게 되었습니다. 
   


------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - 동시성 문제를 해결해 보면서 이를 해결하기위한 방법으로 트랜잭션 격리수준을 높이는 방법과 , Redis의 분산 Lock, 익스큐터를 이용한 스레드 풀 설정을 학습하게 되었고 최종적으로 Redis의 분산 Lock 을 적용하게 되었습니다.
   
   처음 겪어보는 동시성 문제를 마주할수 있었고 추후 데이터베이스의 ACID원칙에서의 C를 준수하게 되었고 ACID원칙 또한 배우게 되었습니다. 또한 동시성 문제를 관찰하기 위한 모니터링 시스템 actuator + prometheus -> Grafana 를 배웠고

   Jmeter를 이용한 1000개의 동시 요청, 모니터링을 통한 비정상적인 요청을 관찰하고 어떤부분이 문제인지 수정해 볼 수 있는 경험을 쌓았습니다.



------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - 배포 프로세스 과정 학습과 기술 습득 과정에서 Linux 기반 환경에서의 기초적인 명령어 들을 배우게 되었고 이를 계기로 Linux 사용에 대한 학습을 계획하게 되었습니다.

   또한 Docker를 통한 이미지 빌드, 컨테이너 화 과정을 배웠고 AWS EC2의 사용을 배우게 되었습니다.


------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


  - JWT인증방식 채택을 통해 JWT의 구조 (Header, Payload, Signature) 를 학습할수 있었고 이를 파싱하고 발급, 인가 요청, 검증 과정을 통해 예외 처리와 검증 기술을 배우게 되었습니다.

    본 프로젝트에서 Spring Security를 쓰지않고 구현했던터라 JwtUtil 내에 검증 메서드를 /checkLogin 컨트롤러에서 주입받아 사용하게 되었습니다.
    
    이를 클라이언트 측도 CheckLogin이라는 API가 해당 컨트롤러로 인가 요청을 보내고 서버에서 검증후 사용자 데이터를 받게 되는데

    이는 모든 인가가 필요한 API에서 CheckLogin API를 호출하게 되는 식으로 만들었습니다.
    
    허나 모든 인가요청 필요 API에서 CheckLogin API를 호출하게 되니까 중앙집중형 인증 방식이 되어 버렸습니다.

    모든 인가요청이 필요한 엔드포인트로 이동시 2개의 요청이 서버로 전송되기 때문에 지연시간이 늘어나게 된다는 문제점이 있었습니다.

    다음에는 API Gateway사용과 필터클래스를 만들어주면서 해당 개선점을 보완해 보도록 하겠습니다.


    
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


 - 배포 프로세스에서 코드 변경과 테스트를 로컬에서 실행후 재빌드후 - > Docker 이미지 빌드 -> Git commit -> ec2 docker hub에서 pull  하는 프로세스가
   상당히 불편하다는 점을 알게 되었습니다. 해당 프로세스가 자동화가 된다면? Docker 이미지 빌드가 새로 실행 될 때 혹은 Git commit이 발생할 때 마다 배포를 자동으로 할수있다면 편리 할 것 같다는 생각이 들었습니다.

   찾아보니 CI/CD 프로세스를 수행하는 방법은 여러가지가 있었습니다.  Github action, jenkins 등등 2가지중 한가지를 학습하여 적용해보고 싶습니다.


    
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    
# 배운점과 아쉬웠던 점 그리고 학습해보고 싶은 기술 : 
    
    끝으로 이번 프로젝트를 통해 평소 다뤄보지 못했던 동시성 문제 와 실시간 어플리케이션 구현에 있어서 많은 배움을 얻었습니다.
     
     
     동시성 제어 문제에서  Redis를 활용하여 제어 한 부분은 생각보다 구현하기 간단 했었고 강력했습니다.

     
     
     또 다대다 참조 간에 조인테이블 관리 및 순환 참조 방지 에 대한 학습을 할수 있었고, 
     
     
     무결성 문제를 해결하기 위한 해결책 등을 찾으며  해당 문제가 왜 문제가 될 것인지 미리 파악해보는 시간도 가질수 있었습니다.
     

     이를 통해 어플리케이션의 안정성에 대한 가이드라인을 조금씩 볼 수 있게 되었습니다.
     
     
     한가지 아쉬웠던 점은 각 서비스 로직 마다, 시간 복잡도, 공간 복잡도를 더 세심하게 고려하여 로직을 구현 했어야 했는데 그러지 못했던 것이 아쉽습니다.

     
     다음 번에는 해당 사항들을 고려하여 필요 서비스 마다 효율적인 알고리즘과 자료구조를 선택하여 구현 하려고 합니다.


    
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------     

    그리고 이번 어플리케이션 구현에 있어서 많은 정보들을 찾던 와중에 MSA 아키텍쳐 기술에 대한 글을 보게 됐습니다.
    

     
   
     MSA 아키텍쳐란 마이크로 서비스 아키텍쳐,  각 서비스 로직 마다 독립적인 서버를 가진다. 라는 것이었습니다.


     즉 , 이는 한가지 서비스 로직에서 비정상적인 작업이 일어난다면 특정 서비스 로직만 수정해 재배포를 하면 되기 때문에

     
     운영에 있어서 많은 이점을 가져다 줄 것 같습니다.


     또 하나의 서비스 로직이 작동하는 서버에 클라이언트가 몰릴 경우 해당 서비스만 확장이 가능하다고 합니다.

     
     24시간 365일 무 중단 운영을 이상적인 운영이다 라고 생각한다면 너무 좋은 아키텍쳐인 것 같습니다.
     
     
     제 프로젝트는 모놀리식 아키텍쳐이기 때문에 한부분이 망가지면 전체가 영향을 받을 수 있습니다.

     
     이러한 장애와 사용자 편의성 안정성 등을 고려하여 다음에는 MSA아키텍쳐에 대한 학습 이후  적용 해보고 싶습니다.
    

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


## 🔴 트러블 슈팅 
  - 주요 문제 사항들과 기술적용 사례,해결방법, 흐름을 기술하겠습니다. 


------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


### 🔴 다대다 참조 문제
  - 테이블 구조 설계 간에 다대다 문제 (many to many) 문제 chatroom과 user 에서의 문제.

    user는 다수의 chatroom을 가질수있어야하고 chatroom 역시 다수의 user와 엮여야 합니다. 문제는 chatroom을 불러왔을때 chatroom은 user를 참조하고 user는 chatroom을 참조하는 무한 굴레(순환참조)에 빠지게 되는 점입니다.

    또한 ManytoMany 어노테이션으로 참조 관계 설정시 중간 테이블이 자동 생성됩니다. 이 테이블 내부는 제어도 불가합니다. 이를 해결하기 위해 중간 조인테이블인 RoomInUser테이블을 생성했고 user는 RoomInUser테이블을 통해 본인이 속한 채팅방이 어느 곳인지 명확히 확인할 수 있게 되었습니다.

    허나 이는 근본적인 순환참조의 해결방식은 아니었습니다.  User -> RoomInUser -> Chatroom -> RoomInUser -> User 와 같은 식으로 양방향 참조는 결국 서로를 참조하려들었습니다. 실행중인 어플리케이션에 콘솔로그에서 받아온 json object는 user가 무한히 뻗어나가있었습니다.

    이를 해결하기위해 DTO를 생성 UserDTO, RoomDTO 를 생성했고 테이블구조인 양방향 관계와는 다르게 roomDTO객체만 userDTO를 참조하도록 만들었습니다.

    예시로 저는 user가 자기가 속한 room을 찾을 때에 user를 인자로 전달후 대조하여 roomInUser에서 chatroom의 정보를 찾아오도록 했고 roomInUser(채팅방에 속한 유저들)는 userDTOs 라는 리스트 타입으로 변환습니다. 그리고 chatroom의 정보를 토대로 RoomDTO에 앞서 만든에 모든 로그인이 필요한 api에서 해당 토큰을 불러와야만 하는 문제가 발생했습니다.

    ### 🔵 해결
       **로컬 스토리지 불러오기** : 토큰은 로컬스토리지에 저장되어있기에 불러와서 파싱만 해주면됩니다. 따라서 저는 checkLogin이라는 함수를 작성했습니다. 함수에서는 비동기 방식으로 js의 내장 비동기함수인 fetch함수로 작성해주었고 서버에서의 응답값을 제대로 불러오기위해 await fetch함수를 사용했습니다.
                                     서버에서 토큰이 유효한지를 검증 - > 유효하지 않다면 refToken을 이용하여 재발급 - > 토큰 클레임 추출 메서드 호출 -> 추출된 사용자 정보 클라이언트 측에 json형식으로 전달
       
        -> 클라이언트는 해당 정보를 토대로  return { loggedIn: true, userEmail: data.userEmail } 객체 반환 -> 이후 로그인 검증이 필요한 모든 api,사용자 지정 함수에서 await checkLogin 호출 -> 반환받은 객체에 저장된 값을 통해 통과 혹은 거부

       **단일 책임 원칙(SRP, Single Responsibility Principle)** : 단일 책임 원칙은 서버에서도 클라이언트에서도 당연시 적용되어야하는 규칙이기에 클라이언트 측에서도 하나의 api, 하나의 함수에서 하나의 역할만 하도록 하기위해 ui업데이트 및 기타 기능들을
    
      따로 정의해두었습니다.
      
   
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### 🔴 동시성 문제 
   - 동시가입 1000명 시나리오를 테스트 해봤습니다. 이유는 1000명이 동시에 가입시에 응답속도가 얼마나 걸리는지 등등 궁금했습니다.
     시나리오는 이렇습니다.
     동일한 회원정보를 Jmeter를 이용해  /auth/register로 1000개의 요청을 보냅니다.
           
        ![스크린샷 2025-01-06 185414](https://github.com/user-attachments/assets/8caa90f5-52c4-41ab-be03-6d61a5c9dd90)
       
       
       
        ![스크린샷 2025-01-07 171024](https://github.com/user-attachments/assets/c43f8f25-3a9c-4621-8204-ed8deac2b10a)
        - 결과:
          http_request_seconds_sum : 모든 요청의 응답시간을 합한값입니다.
          그래서 697/1000 = 0.697 이기에 1개의 요청에 0.697초가 소요되었다는 얘기입니다.
          허나 중요한점은 동일한 값을 가진 행이 삽입 됐다는 것이 중요한 문제 입니다.
 
    
       #### 🔵 방법1
       **Synchronized키워드로 회원가입 블럭 감싸기** : 한번에 하나의 쓰레드만 해당 블럭에 접근가능하도록 해보았습니다. 이후 Jmeter를 이용해 다시 1000개의 요청을 보내봤습니다.
          - 결과 : 요청의 응답시간은 동일, 동일 행 삽입수는 현저히 줄었으나 여전히 100개 이상의 동일한 행 삽입
            
          - 문제점 :

          Sychronized 는 우선 JVM 내부에서 단일 스레드만 특정 블럭에 접근가능하게 만들어 줍니다.
                        
          허나 트랜잭션이 완전 커밋 되고 나서 접근을 가능하게 해준다는 보장이 없습니다. 
                        
          트랜잭션은 별개의 문제이고 커밋이 일어나기 직전 다른 스레드가 해당 블럭에 또 접근 하게 되니 결국 동일한 행이 같이 삽입 됐습니다.
                       
          
           
      
      
       #### 🔵 방법2 
       **Redis 분산 Lock 적용** :

 
         Redis를 학습하며 배운 SETNX 연산을 활용하여 회원가입 요청이 들어올때 이메일 값을 이용하여 키를 생성 -> 키가 존재하면 true 아니면 false를 반환하게 하여 true일 경우에만 회원가입 로직으로 접근
          
         Redis 의 SETNX연산자는 해당 키가 존재하지 않을 경우에만 값을 설정하는 연산자입니다.
      
         이를 활용하여 동시에 동일 회원 삽입 요청을 보내면 , SETNX연산자에 의해 키 값은 회원가입 요청을 한 user의 email 이 되고 
      
         얻은 Lock 을  5초 동안 유지 하게 됩니다. 먼저 들어온 요청 1개 에 대한 요청만 처리를 보장해 주기에 동시성 문제는 이렇게 해결 됐습니다. 
                                       
          


        
  
 ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 


    
 # 🔴 실시간 채팅 
  - 실시간 채팅기능 구현에 있어서 단순 http 통신으로 메세지를 주고 받는 비효율적이고, 실시간성을 구현하기 어렵습니다. http 는 statless하다는 특성 덕분에 클라이언트와 서버간의 연결을 유지하지 않았기에 지속적인 연결을 성립시키기 위한 기술이 필요했습니다.
  
 
    따라서 SockJs와 STOMP를 사용하여 메세지 전송시 엔드포인트 설정-> 해당 엔드포인트 구독 대상자에게 전송 하는 식으로 구현하고자 시도했습니다.


    하지만 연결을 수립하는 부분에서부터 문제가 발생했습니다. SockJs는 각 클라이언트 별로 고유의 세션을 생성하는데, 이 부분에서 세션의 정보가 명확하게 설정되지 않았기에 메세지를 보내는 주체와 구독하는 대상자가
    

    명확하지 않아 메세지 주고 받음 자체가 불가능 했습니다. 따라서 저는 WebSocket 원시 API를 이용했습니다.


 
       ## 🔵 실시간 채팅 : 흐름
    
       1. **유저 로그인** : 클라이언트에서 로그인 후 WebSocket 연결을 시도.
        
       2. **WebSocket 연결** : JWT 토큰 값을 인터셉터에 전달.
        
       3. **인터셉터 처리** : 인터셉터에서 토큰을 파싱하여 이메일을 추출하고, 이를 **Map 객체**에 저장.
        
       4. **WsSessionManager** : 저장된 이메일을 이용해 세션을 생성.
        
       5. **채팅방 입장** : 채팅방에 입장할 때, 해당 세션을 **roomSession**에 주입.
        
       6. **메시지 전송** : 메시지를 전송할 때, 전송 대상의 이메일 값을 이용하여 세션 정보를 확인.  대상 세션이 존재하면 그 세션에 메시지를 전송.
        
       7. **메시지 수신** : 메시지를 수신하고 화면에 표시.
        
       8. **로그아웃** : 로그아웃 시, 해당 세션과 **roomSession**에 저장된 세션을 제거하고 WebSocket 연결 종료.

       ## 🔵 실시간 채팅: 상세설명
               
       ![image](https://github.com/user-attachments/assets/fd379202-5111-4bff-9017-8f1ab0739aaa)
       ![image](https://github.com/user-attachments/assets/677ea166-6edf-48f5-b9f5-684bf3bc8e1d)
    
        
        - 일단 Websocket 연결이 수립되면  서버는 해당 클라이언트를 관리하게 됩니다.
          
          그리고 저는 스레드를 생성하지 않았음에도 Spring 내부적으로  스레드를 관리 해줍니다.
     
          만약 AWS 에 배포된 어플리케이션에서 사용자 1이 로그인하면 WebSocket연결이 수립되는데 이와 같은 과정이 멀티 스레드 방식이 아니라면 메세지를 주고 받는것이
     
          동시적이지 않고 한명이 발송을 완료하기 전 까지는 사용자 2는 메세지를 발송하지 못하게 됩니다. 
          
          위 사진은 제가 웹소캣 핸들러 인터페이스를 오버라이딩 하여 연결수립시의 메서드를 정의한 코드 입니다.  
          
          근데 여기서 부터 스레드가 생성 되는 것은 아닙니다.
     
          톰캣 컨테이너는 자신만의 스레드 풀을 가지고 있는데 거기서 남는 스레드를 클라이언트 측에서 연결 요청이 들어올 때 할당 해 줍니다.
     
          즉 , 서로 다른 스레드가 웹소켓 연결 요청 시 할당되며  해당 스레드는 NIO (Non-blocking I/O) 스레드 풀 에서 할당 된 것 입니다.
     
          BIO 에서는 앞서 서술한 한명이 발송을 완료 혹은 처리를 완료 하기 전까지 다른 스레드는 접근이 불가하다는 특징이 있습니다.
     
          WebSocket 연결 에서는 NIO 스레드 이니까 서로 신경 쓰지 않고 할일을 하게 된다 .. 라고 이해하고 있습니다.
     
          위 사진은 그렇게 할당된 스레드가 수행하게 될 작업인 것 입니다. 
          
          다음으로 연결 수립 이후 온라인 유저 추적을 위한 데이터 생성과 
          
          유저의 세션을 간편하게 관리하기위해 key : userEmail , value : WebSocketsession 형식으로 담아둘 ConcurrentHashMap을 
    
          생성해주는 코드 입니다.  왜 세션을 인메모리에 저장했느냐? 
    
          본 어플리케이션은 분산 서버 환경이 아니기에 타 서버와 세션을 공유 할 필요가 없습니다. 
    
          추후 분산 서버 환경 구축시에 Redis를 이용하여 세션의 정보를 담아 두고 써도 될거라 판단하여 인메모리에 저장했습니다.
    
          단점으로는 서버가 재시작 되면 정보가 유실된다는 점이 있습니다.
    
          또 ConcurrentHashMap을 쓴 이유는 앞서 말씀 드린 이유 때문입니다. 멀티 쓰레드 환경에서 HashMap을 쓰게 된다면
    
          기본적으로 HashMap은 동기화 되어 있지 않기에 여러 쓰레드가 접근해서 수정하게 된다면 데이터가 손상되거나 변조될 위험이 있습니다.
    
          하지만 유저의 세션 자체는 해당 자료구조를 쓰지 않아도 상관은 없지 않나 라고 생각 했었지만  만약 세션이 잘못 설정된다거나 하게 되면
    
          세션 자체의 무결성을 보장 하지 못한다는 것이기에 메세지 자체의 송수신이 안됩니다. 따라서 해당 자료구조의 특징 중 Null값을 허용하지 않는다는
    
          특징을 이용해 userSession을 만들어 줬습니다.
      


     
     
      
     

       
       
       
       




 ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------    
### 🔴 배포시 DB커넥션 불가
 - ec2인스턴스 생성후 ssh를 통해 접속하여 인스턴스 내부에 oracle-xe18c Linux용 rpm파일을 다운로드 했습니다. 이후 deb로 변환후 설치하려 했으나 인스턴스 내부 사양의 한계로 설치가 불가했습니다.

   이를 해결하고자 ec2인스턴스의 사양을 변경, oracle설치까지 가능했으나 리스너 등록 문제가 발생했습니다. 이를 로컬에서 사용하던 db와 ec2인스턴스를 연결해주는 방법으로 해결했습니다.

   ### 🔵 해결
       1. **로컬 Oracle DB 연결** : 로컬에서 사용되는 리스너는 수정할 필요가없음 -> 프로젝트 내부 application-ignore.properties 파일에서 커넥션 url 을 로컬 공인 ip로 변경
       
       3. **ec2인스턴스 환경설정** : ec2 인바운드 규칙에 1521 포트 추가 -> 로컬 공인 ip 확인후 jdbc.url을 공인 ip로 변경 -> 통신사 모뎀 설정에서 1521포트 포워딩 설정추가 (로컬 리스너에서 리스닝하는 host가 로컬 내부 ip임,그래서 공인 로컬 ip로 내부 접속을 위한 포워딩 필)-> 연결 완료
  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


### 🔴 Docker이미지 실행시 환경변수로드
 - 이미지 빌드시에 주요 민감정보들을 application-ignore.properties와 .env에 작성했고 컨테이너 내부에서 마운트하는 방식을 채택했습니다.

   이미지를 ec2인스턴스에서 pull 하여 docker-compose up -d 명령어를 이용하여 실행시키니 환경변수들이 로드되지않았습니다. 마운팅 경로를 /home/config 에서 파일을 불러와 /app/ 에 마운팅 되게 해줬었는데, 문제는 /home/config는 ec2 인스턴스에서 실행될테니 인스턴스 내부경로고

   해당 경로에 해당 파일들이 있어야 했습니다.


   ### 🔵 해결
       1. **인스턴스로 파일 전송** : 로컬에 작성했던 파일들을 내부경로로 전송해줘야하기에 명령프롬프트에서 scp -i "c:\{키페어파일}" "c:\{전송할파일}" ubuntu@ec2-ip 를 입력하여 전송가능
    
       2. **컨테이너 재시작후 로드 확인** : 이제 경로에 파일이 있으니 모든 정보가 로드가능 했습니다.

 
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  


### 🔴 SSL자체서명 인증서 발급후 적용
 - ssl인증서 발급 과정에서 keytool을 사용하여 csr 없이 한번에 인증서를 발급받았습니다. alias(별칭) , keyalg(알고리즘) = RSA , validity(유효기간) , storetype(키스토어 타입) =PCKS#12 그 외 정보들을 이용하여 파일을 생성했습니다.

   그리고 springboot에서 https를 적용하기 위해 파일을 ec2인스턴스  /home/config/  경로로 로컬에서 전송했고 , 스프링부트의 ignore.properties에서 키파일의 경로와 비밀번호 타입을 명시해주었고 docer컨테이너 내부 오리지널 properties에 마운트

   했습니다. 이후 컨테이너 실행후 https:\\ec2-ip:8080 으로 접속하니 접속이 안됐습니다.


   ### 🔵 해결
       1. **포트 설정** : https는 기본적으로 8443 포트를 사용하는데 , ec2에서 인바운드 규칙으로 해당 포트를 열어주지 않았습니다. 그래서 인바운드 규칙을 추가해주었습니다.
                       이후 https:\\elastic-ip:8443으로 접속하니 접속이 되었습니다.

   ### 🔴 개선할 포인트
       1. **포트 리다이렉팅** : 사용자가 8080 http로 접속하여도 https 보안연결을 유도하도록 TomCatConfig.class 를 작성후 8080으로 접속되면 자동으로 8443으로 리다이렉팅되게 작성했지만 어떤 이유에서인지 되지가 않았습니다.
                              학습이 필요합니다.

 
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------  


### 🔴 SSL 인증서 적용후 WebSocket 연결 실패
- ssl인증서 발급후 적용후에 어플리케이션 실행 -> 로그인후 WebSocket연결시 바로 연결이 업그레이드 되지 않았습니다.

  유추할수 있었던 문제점으로 WebSocket연결은 먼저 http 프로토콜로 먼저 연결요청을 보내는데 이제 https로 변경되었으니 해당 연결 url이 유효하지않다라는 생각이었습니다.

  ### 🔵 해결
      1. **웹소켓 연결 url 변경** : 기존 웹소켓 연결요청을 보낼 때 클라이언트 측에서 ws://ec2-ip:8080/ws-login?Authorization={accessToken} 으로 연결요청을 보냄
                                 ws-login 경로를 등록 -> 인터셉터 적용 -> 인터셉터에서 토큰 유효 검증후 연결 .. 이 흐름 자체는 문제가 없어보이지만 https로 바뀔때는 ws://...가 아니라
                                 wss://.... 로 바꿔줘야한다고 합니다. 그래서 해당 사항 변경후 재연결 시도해보니 성공.


