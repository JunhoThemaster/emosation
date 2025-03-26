

document.addEventListener('DOMContentLoaded', function (){

    if(window.location.pathname === '/main'){
            main();
    }

    if(window.location.pathname === '/auth/mypage'){
        myPage();
    }

});


async function register() {
    // 폼 값들 가져오기
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const pw = document.getElementById("upPw").value;
    const phone = document.getElementById("Phone").value;
    const confirmpw = document.getElementById("cupPW").value;

    // 비밀번호와 확인 비밀번호 일치 체크
    if (pw !== confirmpw) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    // 유효성 검사: 필수 값이 비어 있지 않은지 확인
    if (!name || !email || !pw || !phone || !confirmpw) {
        alert("모든 필드를 채워주세요.");
        return;
    }

    // 비밀번호 강도 체크 (간단한 예시: 6자 이상)
    if (pw.length < 6) {
        alert("비밀번호는 6자 이상이어야 합니다.");
        return;
    }

    // 서버로 전송할 데이터 형식
    const body = JSON.stringify({
        Name: name,
        Email: email,
        Pw: pw,
        Phone: phone,
    });

    // 버튼 비활성화 (중복 클릭 방지)
    const submitButton = document.getElementById("reg");
    submitButton.disabled = true;

    try {
        // fetch를 통한 서버로의 POST 요청
        const resp = await fetch('/auth/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: body,
        });

        if (!resp.ok) {
            alert("회원가입 실패. 다시 시도해 주세요.");
            submitButton.disabled = false; // 실패 시 버튼 다시 활성화
            return;
        }

        alert("회원가입 성공");
        window.location.href = '/'; // 회원가입 성공 후 리다이렉션

    } catch (error) {
        console.error("회원가입 중 오류 발생", error);
        alert("회원가입 실패. 다시 시도해 주세요.");
    } finally {
        submitButton.disabled = false; // 요청이 끝난 후 버튼 다시 활성화
    }
}




async function Login(){
    const em = document.getElementById('em').value;
    const pw = document.getElementById('pw').value;

    if(!em || !pw){
        alert("이메일과 비밀번호 모두 입력")
        return;
    }

    try{
        const params = new URLSearchParams();
        params.append('email', em);  // 서버에서 'email' 파라미터 받음
        params.append('pw', pw);
        const resp = await fetch('/auth/login',{
            method: 'POST',
            headers : { 'Content-Type': 'application/x-www-form-urlencoded'},
            body: params.toString(),
        })

        if (resp.ok){
            const data = await resp.json();
            if(data.accessToken && data.refreshToken){
                localStorage.setItem("accessToken",data.accessToken);
                localStorage.setItem("refreshToken",data.refreshToken);

                alert(data.msg);
                window.location.href ='/main';

            } else if(data.recovered && data.accessToken){
                localStorage.removeItem("accessToken");

                localStorage.setItem("accessToken",data.accessToken);
                window.location.href = '/auth/resetPw';

            }

        }else{
            const data = await resp.json();
            alert(data.msg);
        }
    }catch (error) {
        alert("서버 요청 실패" + error);
    }
}



async function checkLogin() {
    const token = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken')
    if (!token) {
        console.log('로그인 상태가 아닙니다.');
        return  { loggedIn: false};
    }

    try {
        const response = await fetch('/auth/chekLogin', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`, "Refresh-Token": refreshToken,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json(); // 응답 데이터가 있을 경우 처리
            console.log('로그인 상태 확인됨:', data.userEmail);
            if (data.accessToken && data.userEmail) {
                localStorage.setItem('accessToken', data.accessToken);
                return { loggedIn: true, userEmail: data.userEmail };
            }else{
                window.location.href = '/';
                return { loggedIn: false, userEmail: null };
            }
        } else {
            console.log('인증 실패:', response.status);
            window.location.href = '/';
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');

            alert("인증 실패");

            return { loggedIn: false};
        }
    } catch (error) {
        console.error('서버 요청 실패:', error);
        window.location.href = '/';
        alert("서버 요청 실패");
        return { loggedIn: false};
    }
}
window.checkLogin = checkLogin;

async function main(){
    const LoggedIn = await checkLogin();

    if(LoggedIn.loggedIn){
        const userId = LoggedIn.userEmail;
        console.log(userId);
        roomList(LoggedIn);
        connectws(LoggedIn);
        try{
            const resp = await fetch(`/main/myfrlist?userid=${userId}`,{
                method:'GET',
                headers:{'Content-Type':'application/json'},

            });
            if(resp.ok){
                const data = await resp.json();
                const tbody = document.getElementById('frlist');
                tbody.innerText = "" ;// 친구추가시에 친구 목록의 중첩을 방지하기위함.
               if(data && data.length > 0){
                   data.forEach(fr => {

                       const tr = document.createElement('tr');
                       const td = document.createElement('td');
                       td.innerText = fr.name;

                       tr.id = "userEm"
                       tr.className = fr.email;
                       tr.appendChild(td);

                       const btntd = document.createElement("button");
                       btntd.onclick = function (ev){
                           ev.preventDefault();
                           openChatBox(LoggedIn,fr.email) };
                       btntd.id = fr.id;
                       btntd.innerText = "메세지 보내기";

                       btntd.className = "button button--wayra button--border-medium button--text-upper button--size-s button--text-thick button--inverted";

                       tr.appendChild(btntd);

                       tbody.appendChild(tr);

                   });


               }
            }else {
                alert("서버응답 실패");
            }
        }catch (error){
            alert("호출 실패" + error);
        }
    }
}



async function search(){


    const loggedIn = await checkLogin();

    if(loggedIn.loggedIn){
        try {
            const name = document.getElementById("find").value;

            const resp = await fetch(`/main/findfr?name=${encodeURIComponent(name)}`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (resp.ok) {
                // 응답을 JSON으로 변환하여 데이터 받기
                const data = await resp.json(); // await 추가

                console.log(data);
                const modal = document.querySelector('.modal')
                const close = document.querySelector('.close_btn')
                const tr = document.getElementById("searchResult");
                const td = document.createElement('td')
                // 모달창 내용
                td.innerText = data.user.name;
                td.id = data.user.id;

                const btntd = document.createElement('td');
                const btn = document.createElement('button');
                    btn.className="button button--wayra button--border-medium button--text-upper button--size-s button--text-thick button--inverted"
                    btn.innerText="친구추가"
                btntd.appendChild(btn);

                btn.name = 'frid'
                btn.onclick = function (){
                    addFr(data.user.id);
                }
                tr.appendChild(td);
                tr.appendChild(btntd);
                if(data.user.name){
                    modal.style.display = 'block'; // 데이터가 있어야만 뛰우자.
                }
                close.addEventListener('click',function (){
                    modal.style.display = 'none';
                    btn.remove();
                    btntd.remove();
                    td.remove();
                })


                console.log(data.user);  // 받은 데이터 로그로 확인
                alert("찾은 사용자: " + (data.user.name || "이름 없음")); // 데이터 처리 예시
            } else {
                alert("없는 사용자!");
            }
        } catch (error) {
            console.error('Error:', error);
            alert("네트워크 오류");
        }
    }
}

async function addFr(frid){

    const loggedIn = await checkLogin();

    if(loggedIn.loggedIn){
        const userid = loggedIn.userEmail;



        const resp = await fetch(`/main/addfr/${userid}/${frid}`,{
            method : 'POST',
            headers : {'Content-Type' : 'application/json'}

        });
        try{
            if(resp.ok){
                const data = await resp.json();
                main();
                alert(data.msg);
            }
            else {
                alert(data.errors);
            }

        } catch (error){
            alert('오류 발생' + error);
        }

    }else{
        alert("로그인이 필요합니다");
        window.location.href = '/';
    }


}




let ws = null;
function connectws(LoggedIn) {// JWT 토큰을 로컬 스토리지에서 가져옴

        const token = localStorage.getItem('accessToken');
        const wsUrl = "wss://13.125.58.11:8443/ws-login?Authorization=" + encodeURIComponent('Bearer ' + token);
        ws = new WebSocket(wsUrl);  // WebSocket 연결 생성

        ws.onopen = function() {
            console.log("WebSocket 연결 성공");

            console.log("WebSocket 연결 상태:", ws.readyState); // 상태 확인 (OPEN -> 1)

        };

        ws.onmessage = function(event) {
            // 서버로부터 받은 메시지를 처리
            const message = JSON.parse(event.data);  // 예시: 메시지를 JSON 형식으로 처리
            // console.log("서버로부터 받은 메시지:", message);

            if(message.type === "message"){ //type이 그냥 메세지인경우는 상대세션의 존재여부에 따른 검증을 해줄 showmsg 호출
                                            //  showmsg에는 채팅방을열면 ul요소의 Id 값이 chat-{id} 이런식으로 설정되게 해줬다 .
                showmsg(message);

            }else if(message.type === "status"){ // 메세지 오브젝트에서의 type을 비교함
                const isSessioninroom = message.doesSheInRoom; // 핸들러에서 보낸 boolean값 담아줌

                console.log("isSessioninroom :" + isSessioninroom);

                if(isSessioninroom){  // 상대 세션이 채팅방에 존재하고 나도 존재한다면? 이떄부터 옵져버 시작 실시간으로 읽음 표시를 보낸 메세지를 읽음처리함.
                                        // 채팅방 클로즈 호출시에 isSessionInroom은 false로 설정되어서 전달받게될것임.
                    const roomId = message.roomId;
                    observerForChat(roomId);
                }
                else{
                    stopObserver();    // 만약 없다면 옵져버 중단.
                    console.log("상대방이 없음");

                }
            }else if(message.type === "newOne"){   // 여기서는 사용자 끼리 연결된 채팅방이 없었고 새로운 채팅방을 생성하고 메세지를 보내면
                                                    // 핸들러에서 양방향으로 메세지 type: newOne 이라는 메세지를 전송 상대와 나 둘다 알수있게.
                                                    // 상대는 모르기 때문에 roomList를 호출하여  채팅방 목록 갱신.
                roomList(LoggedIn);
                if(message.sender === LoggedIn.userEmail){ // 여기서는 메세지를 내가전송했다? 그러면 채팅을 처음 전송시에는 db에 저장된 메세지를 보는게 아닌 sendmessage호출시에 만들어진 요소를 보는것이기에
                                                            // ul의 클래스 이름도 채팅방의 id가 아니게된다 그러므로 첫 전송시 상대가 답장을 하게된다면 내가 열어놓은 채팅방에 메세지가 출력되지 않기때문임.
                   setTimeout(() =>
                       {
                           getroombyroomId(message.roomId);
                            },100);
                }
            }

        };

        ws.onerror = function(error) {
            console.error("WebSocket 오류:", error);
            window.location.href = "/";
        };

        ws.onclose = function() {
            console.log("WebSocket 연결 종료");
            window.location.href = "/";
        };

}

let observer;
function observerForChat(roomId,sentiments){  // 현재 실시간 읽음 처리에서 dom요소를 선택 하지 못하는 문제가 생겼기에 실시간 변이 감지기.
    const ul = document.querySelector(`ul.chat-${roomId}`);

    if(!ul){
        console.log("ul요소 없음");
        return;
    }

    observer = new MutationObserver((mutations) =>{
        mutations.forEach((mutations) =>{
            if(mutations.type === "childList"){
                mutations.addedNodes.forEach((node)=>{
                    if(node.nodeType === 1 ){
                        if(node.classList.contains("me")){
                            console.log("내가 새로 만든 요소", node);
                            const lastmsg = ul.querySelector("li.me:last-child");

                            const existingDiv = node.querySelector('.readStat');
                            if (!existingDiv) {
                                const div = document.createElement("div");
                                div.innerText = "읽음";
                                div.className = "readStat"; // 스타일링을 위한 클래스 추가
                                node.appendChild(div);

                                // 3초 후 "읽음" 표시 제거
                                setTimeout(() => {
                                    div.remove();
                                }, 3000);
                            } else {
                                console.log("이미 '읽음' 표시가 추가되어 있습니다.");
                            }

                        } else if(node.classList.contains("you")){
                            console.log("새로 추가된 상대 메시지 요소", node);

                           // 실제로 들어올 메시지로 변경 필요
                            const existingH2 = node.querySelector("h2.sentiment");
                            if (existingH2) {
                                const sentiment = existingH2.innerText;

                                typeEffect(existingH2, sentiment, 30); // 100ms 간격으로 타이핑
                            }

                        }
                    }
                });
            }
        })
    })

    observer.observe(ul,{childList:true});

    console.log(`MutationObserver가 채팅방 (roomId: ${roomId})을 감지하고 있습니다.`);

}


function stopObserver(){
    if(observer){
        observer.disconnect();
        console.log("옵져버 중단");
    }
}

async function sendMessage(receiverEmail) {
    const LoggedIn = await checkLogin();
    if (LoggedIn.loggedIn) {
        // WebSocket이 연결되어 있지 않으면 연결 시도
        if (!ws || ws.readyState !== WebSocket.OPEN) {
            console.log("WebSocket 연결되지 않음. 연결을 시도합니다...");
            await connectws(); // WebSocket 연결을 위한 함수 호출
        }

        const msginput = document.getElementById("msginput");

        const msg = {
            type: "message",
            destination: encodeURIComponent(receiverEmail),  // 수신자의 이메일
            payload: {
                sender: LoggedIn.userEmail, // 발신자 이메일
                content: msginput.value
            }
        };

        // WebSocket을 통해 메시지 전송
        if (ws && ws.readyState === WebSocket.OPEN) {
            const chatbox = document.getElementById("chatbox")



            const ul = document.getElementById("chat");
            setTimeout(()=>{
                ul.scrollTop = ul.scrollHeight;
            },100);

            const li = document.createElement("li");
            li.className = "me";

            const div = document.createElement('div');
            div.className = "entete";



            const currentDate = new Date();
            const currentTime = currentDate.toLocaleTimeString();
            const h3 = document.createElement("h3");
            h3.innerText = currentTime;

            const h2 = document.createElement("h2");
            h2.innerText = LoggedIn.userEmail;

            const div1 = document.createElement("div");
            div1.className = "triangle";

            const div2 = document.createElement("div");

            div2.className = "message";
            div2.innerText = `${msg.payload.content}`;

            const croomId = ul.className;
            const roomId = croomId.split('-')[1];


            ws.send(JSON.stringify(msg));


            div.appendChild(h3);
            div.appendChild(h2);
            li.appendChild(div);
            li.appendChild(div1);
            li.appendChild(div2);

            ul.appendChild(li);

            console.log(`메시지 전송됨: ${msg.payload.content} (수신자: ${receiverEmail})`);

        } else {
            console.log("WebSocket 연결이 열리지 않았습니다.");
        }
    }

}


async function showmsg(msg){
    const roomId = msg.roomId;

    // const chatbox =

    const ul = document.getElementById("chat");
    if(ul.className === `chat-${roomId}`){    /// 클라이언트가 채팅방을 조회시에는 class네임이 설정되기에.. roomId로 ul의 구분을 짓지않으면 내가 상대에게 보낸 모든메세지가
                                                // 상대측이아니어도 출력하게된다.


        const li =  document.createElement("li");
        li.className = "you";

        const div = document.createElement("div");
        div.className = "entete";

        const currentDate = new Date();
        const currentTime = currentDate.toLocaleTimeString();


        const h2 = document.createElement("h2");
        h2.innerText = `${msg.sender}`

        const h3 = document.createElement("h3");
        h3.innerText = currentTime;




        const div1 = document.createElement("div");
        div1.className = "triangle";

        const div2  = document.createElement("div");
        div2.className = "message";
        div2.innerText = `${msg.content}`

        const sentimentElement = document.createElement("h2");
        sentimentElement.className = "sentiment";

        sentimentElement.innerText = msg.sentiment;



        div.appendChild(h2);
        div.appendChild(h3);
        li.appendChild(div);
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(sentimentElement);
        ul.appendChild(li);
        setTimeout(()=>{
            ul.scrollTop = ul.scrollHeight;
        },100);


    } else{   // 조회하고 있지않다면 알림을 해줄 목적으로 unreadCnt를 ++ 해준다.


        const span = document.getElementById(`unread-${roomId}`)
        if(span){
            span.className = "note-num";
            span.innerText = msg.newMsg;

        }

    }

}



async function typeEffect(element,text,speed){

    element.innerText = "";
    for (let char of text){
        element.innerText += char;
        await new Promise(resolve => setTimeout(resolve,speed));
    }

    setTimeout(() => {
        element.remove();
        console.log("타이핑 요소가 제거되었습니다.");
    }, 20000); // 1초 후 제거 (필요시 조정 가능)

}


async function roomList(LoggedIn){   // roomlist들을 화면에 보여주기위한.

    if(LoggedIn.loggedIn)
        try {
            const resp = await fetch(`/main/chatlist?userEm=${LoggedIn.userEmail}`,{
                method : 'GET',
                headers : {"Content-Type" : "application/json"},
            });
            if (resp.ok){

                const data = await resp.json();
                const ul = document.getElementById("chatList");
                ul.innerText = "";
                console.log(data);
                if(data.rooms && data.rooms.length > 0){


                    data.rooms.forEach(chatroom =>{
                        const roomName = chatroom.room.roomName;
                        const unreadCnt = chatroom.unread;

                        const span = document.createElement("span");
                        span.className = "note-num";
                        span.id = `unread-${chatroom.room.id}` ;



                        span.className = "note-num";
                        span.innerText = unreadCnt;


                        console.log(roomName);



                        const li = document.createElement("li")

                        const div = document.createElement("div");

                        const a = document.createElement("a");
                        div.style.position ="relative";
                        a.innerText = roomName + "의 채팅방";
                        a.href = "#";
                        a.addEventListener("click",async (e) => {
                            e.preventDefault();
                            getroombyroomId(chatroom.room.id);
                        })



                        div.appendChild(a);
                        div.appendChild(span);
                        li.appendChild(div);
                        ul.appendChild(li);



                    })

                }else{
                    const ul = document.getElementById("chatList");
                    ul.innerText = data.msg;
                }

            }


        } catch(error){
            alert(error);
        }



}



async function getroombyroomId(roomId){
    const loggedIn = await checkLogin();


    if(loggedIn.loggedIn){
        const resp = await fetch(`/main/check?roomId=${roomId}`,{
            method: 'GET',
            headers : {'Content-Type' : 'application/json'}
        })

        if (resp.ok){

            const data = await resp.json()
            var emails= null;
            var receiver = null;
            if(data.messages && data.users){

                console.log(data.users);
                console.log(data.messages);
                const span = document.getElementById(`unread-${roomId}`);

                span.innerText = "";
                const sbox = document.getElementById("sendbox");
                sbox.style.display = "block";
                const ul = document.getElementById("chat");
                const closebtn = document.getElementById("closechat");
                closebtn.innerText = "닫기";



                setTimeout(()=>{
                    ul.scrollTop = ul.scrollHeight;
                },100);

                emails = data.users.map(users => users.email);
                receiver = emails.filter(trgt => trgt !== loggedIn.userEmail);


                ul.className= `chat-${roomId}`;


                console.log(receiver);
                if (receiver) {
                    alert(`Receiver: ${receiver}`); // 수신자 이메일을 확인 , 근데 여기서도 첫 메세지면 sender의 email만 존재하니 receiver는 없음 없을때
                    const trgt = document.getElementById("trgt");
                    trgt.onclick = function (ev) {
                        ev.preventDefault();
                        sendMessage(receiver); // 추출된 이메일로 메시지 전송
                    };
                    closebtn.onclick= function (cl){
                        closeChat(roomId,loggedIn.userEmail);
                    };
                    const msg = {
                        type: "enterRoom",
                        roomId : roomId,
                        receiver: receiver,
                        sender : loggedIn.userEmail,
                    };

                    ws.send(JSON.stringify(msg));
                } else {
                    console.log("No valid receiver found.");
                }

                const chatlist = document.getElementById("chat");
                renderMessages(data.messages, chatlist, loggedIn ); // 메시지 렌더링 호출



            }

        }
    }

}


async function openChatBox(LoggedIn,trgtEm){

    const receiverEmail = document.getElementById("userEm").className;

    if(LoggedIn.loggedIn){

        const resp = await fetch(`/main/checkrooms/${trgtEm}/${LoggedIn.userEmail}`,{

        });
        if(resp.ok){

            const data = await resp.json();

            if(data.messages){

                const sbox = document.getElementById("sendbox");

                sbox.style.display = "block";

                const trgt = document.getElementById("trgt");
                trgt.onclick = function (ev) {
                    ev.preventDefault();
                    sendMessage(trgtEm);
                };

                const chatlist = document.getElementById("chat");

                renderMessages(data.messages, chatlist, LoggedIn); // 메시지 렌더링 호출


            } else if(data.msg){

                console.log(data.msg);
                const chatlist = document.getElementById("chat");
                chatlist.innerText = "";
                chatlist.className = "";
                const sbox = document.getElementById("sendbox");
                sbox.style.display = "block";

                const trgt = document.getElementById("trgt");
                trgt.onclick = function (ev) {
                    ev.preventDefault();
                    sendMessage(trgtEm);
                };


            }
            }
        }
}



async function closeChat(roomId,em){


    const resp = await fetch(`/main/close/${roomId}/${em}`,{
        method : 'PATCH',
        headers : {'Content-Type' : 'application/json'}
    });
    updateChatbox(roomId);
    closeMsg(roomId,em)
    if(resp.ok){
        return true;
    }else{
       const error = await resp.json();
       console.log("error : " + error);
    }


}


function updateChatbox(roomId){
    const ul = document.querySelector(`ul.chat-${roomId}`);// roomId를 사용해 각 채팅방별 ul을 찾아야 함
    if (ul) {
        ul.className = "";
        ul.innerHTML = ""; // 해당 채팅방의 메시지를 비움
        const sbox = document.getElementById("sendbox");

        sbox.style.display = "none";

    } else {
        console.log(`채팅방 ID ${roomId}에 해당하는 ul을 찾을 수 없습니다.`);
    }
    return true;

}

function closeMsg(roomId,em){
    const ormsg = {
        type : "close",
        roomId : roomId,
        semail : em,
    };
    stopObserver();
    ws.send(JSON.stringify(ormsg));


}



function createMessageElement(msg, isSender) {
    const li = document.createElement("li");
    li.className = isSender ? "me" : "you";
    if(li.className === "you"){
        const div = document.createElement("div");
        div.className = "entete";

        const h2 = document.createElement("h2");
        h2.innerText = msg.sender.email;

        const h3 = document.createElement("h3");
        const time = formatTime(msg.sentedAt);
        h3.innerText = time;

        const div1 = document.createElement("div");
        div1.className = "triangle";

        const div2 = document.createElement("div");
        div2.className = "message";
        div2.innerText = msg.content;

        const div3= document.createElement("div");



        div.appendChild(h2);
        div.appendChild(h3);
        li.appendChild(div);
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(div3);

        return li;

    } else {

        const div = document.createElement("div");
        div.className = "entete";

        const h2 = document.createElement("h2");
        h2.innerText = msg.sender.email;

        const h3 = document.createElement("h3");
        const time = formatTime(msg.sentedAt);
        h3.innerText = time;

        const div1 = document.createElement("div");
        div1.className = "triangle";

        const div2 = document.createElement("div");
        div2.className = "message";
        div2.innerText = msg.content;

        const div3 = document.createElement("div");
        div.appendChild(h3);
        div.appendChild(h2);
        li.appendChild(div);
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(div3);
        return li;

    }

}


function renderMessages(messages, chatlist, LoggedIn) {
    chatlist.innerHTML = ""; // 기존 메시지 초기화
    chatlist.className = ""; //  기존 채팅방 아이디 초기화.
    messages.forEach((msg) => {
        const isSender = msg.sender.email === LoggedIn.userEmail;
        const roomId = msg.roomId;
        const messageElement = createMessageElement(msg, isSender);
        chatlist.className = `chat-${roomId}`;
        chatlist.appendChild(messageElement);
    });
}

async function resetPw(){

    const pw = document.getElementById("pw").value;

    const repw = document.getElementById("repw").value;

    if(pw !== repw){

        alert("비밀번호가 일치하지 않습니다")
        return;
    }


    const token = localStorage.getItem("accessToken");
    const body = JSON.stringify({pw:pw});


    const resp = await fetch("/auth/newPw",{
        method : 'PATCH',
        headers : { 'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'},
        body: body
    });

    if(resp.ok){

        const data = await resp.json();

        alert(data.msg);

        window.location.href = "/";


    }else if(resp.status === 400){
        const data = await resp.json();

        alert(data.msg);
        window.location.href = "/auth/resetPw";

    }else if(resp.status === 500){

        const data = await resp.json();
        alert(data.msg);

        window.location.href = "/auth/resetPw";

    }


}

async function gotomypage(){

   const LoggedIn = await checkLogin();

   if(LoggedIn){
       window.location.href = "/auth/mypage";
   }else{
       alert("다시 로그인 해주세요!");
       window.location.href = "/";
   }

}


async function myPage(){
    const loggedIn = await checkLogin();

    if(loggedIn){

        const token = localStorage.getItem('accessToken');
        const resp = await fetch("/auth/api/mypage", {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });


        if(resp.ok){
            const data = await resp.json();

            if(data.userEm){
                renderMyPage(data);

            }else{
                alert("error :" +data.msg);
                window.location.href ="/";
            }
        }
    }else{
        alert("로그인 시간 만료");
        window.location.href = "/";

    }
}


function renderMyPage(data){
    const tbody = document.getElementById("my-info");

    tbody.innerHTML = "";
    if(data){

        const tr =  document.createElement("tr");

        const td1 = document.createElement("td");
        const td2 = document.createElement("td");
        const td3 = document.createElement("td");
        const td4 = document.createElement("td");

        td1.innerText = data.userPhone;
        td2.innerText = data.userName;
        td3.innerText = data.userEm;
        td4.innerText = data.date;

        tr.appendChild(td1);
        tr.appendChild(td2);
        tr.appendChild(td3);
        tr.appendChild(td4);


        tbody.appendChild(tr);

    }
}


function formatTime(dateString) {
    // '2024-11-19T17:30:04.565895'와 같은 날짜 문자열을 Date 객체로 변환
    const date = new Date(dateString);

    // 시간, 분, 초를 구함
    let hours = date.getHours();
    let minutes = date.getMinutes();
    let seconds = date.getSeconds();

    // 오후/오전 여부 구하기
    const period = hours >= 12 ? "오후" : "오전";

    // 12시간제로 변경
    hours = hours % 12;
    hours = hours ? hours : 12;  // 0시를 12로 처리

    // 2자리 수로 표시하기 위해 앞에 0 추가 (예: 5 => 05)
    minutes = minutes < 10 ? "0" + minutes : minutes;
    seconds = seconds < 10 ? "0" + seconds : seconds;

    // 포맷된 시간 문자열 반환
    return `${period} ${hours}:${minutes}:${seconds}`;
}
