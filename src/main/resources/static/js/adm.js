
document.addEventListener('DOMContentLoaded',function (){


        if(window.location.pathname === '/admin/home'){
            adhome();
            loadUserli();
            loadScount();
            last7daysChart();

        }
}
);


async function admLogin(){
    const id = document.getElementById("email").value;
    const pw = document.getElementById("passw").value;

    const params = new URLSearchParams();
    params.append("em",id);
    params.append("pw",pw);
    if(!id || !pw){
        alert("이메일과 비밀번호 모두 입력")
        return;
    }



        const resp = await fetch("/admin/login",{

            method : "POST",
            headers : {'Content-Type'  : 'application/x-www-form-urlencoded'},
            body : params.toString()
        });


        if(resp.ok){

            const data = await resp.json();

            if(data && data.accessToken){

                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken",data.refreshToken);

                window.location.href = "/admin/home";
            }
        }else{
            const data = await resp.json();
            alert(data.msg);
        }
}

let curPage = 1;
const pagesize = 10;


async function loadUserli(page){

    const loggedIn = await window.checkLogin();
    if(loggedIn){
        const resp = await fetch(`/admin/loadUsers?page=${curPage}&size=${pagesize}`,{
            method : 'GET',
            headers : {'Content-Type' : "application/json"}
        });

        if(resp.ok){

            const data = await resp.json();
            const tbody = document.getElementById("user-list");
            tbody.innerHTML = "";

            if(data.userLi.length > 0 && data.userLi){
                data.userLi.forEach(user => {
                    const userId = user.id;
                    const userName = user.name;
                    const userEm = user.email;
                    const userRegDate = user.registrationDate;
                    const userStatus = user.userStatus;
                    const tr = document.createElement('tr');
                    const td = document.createElement('td');
                    const td2 = document.createElement('td');
                    const td3 = document.createElement('td');
                    const td4 = document.createElement('td');
                    const td5 = document.createElement('td');
                    const btn = document.createElement("button");


                    if(user.email === loggedIn.userEmail){
                        td5.innerText = "나"
                    }else{
                        btn.innerText = "삭제";
                        btn.onclick = function (ev){
                            ev.preventDefault();
                            delUser(userEm);
                        }
                    }
                    if(userStatus === "DELETED"){
                        btn.innerText = "복구";
                        btn.onclick = function (ev){
                            ev.preventDefault();
                            rollBackUser(userEm);
                        }

                    }
                    td5.appendChild(btn);


                    td.innerText = userId;
                    td2.innerText = userName;
                    td3.innerText = userEm;
                    td4.innerText = userRegDate;

                    tr.appendChild(td);
                    tr.appendChild(td2);
                    tr.appendChild(td3);
                    tr.appendChild(td4);
                    tr.appendChild(td5);
                    tbody.appendChild(tr);

                });
                updatePage(data.totalPage);

            }else{
                const data = await resp.json();
                alert(data.msg);
            }

        }

    }
}


function updatePage(totalPages){

    const pageIndicator = document.getElementById("pageIndicator");
    pageIndicator.innerText = curPage+ "/" + totalPages;

    // 이전 버튼 활성화 여부
    const prevBtn = document.getElementById("prevBtn");
    prevBtn.disabled = curPage === 1;
    prevBtn.onclick = () => goToPage(curPage - 1);

    // 다음 버튼 활성화 여부
    const nextBtn = document.getElementById("nextBtn");
    nextBtn.disabled = curPage === totalPages;
    nextBtn.onclick = () => goToPage(curPage + 1,totalPages);

}


function goToPage(page,totalPage){
    if(page < 1 || page > totalPage) return;
    curPage = page;
    loadUserli(page);
    updatePage();


}


async function delUser(em){

    const resp = await fetch(`/admin/deleteUser?userEm=${em}`,{
        method : 'PATCH',
        headers: {'Content-Type' : 'application/json'}
    });

    if(resp.ok){

        const data = await resp.json();
        if(data){
            alert(data.msg);
            loadUserli();
        }else{
            alert(data.msg);

        }

    }



}


async function rollBackUser(em){

    const resp = await fetch(`/admin/rollbackUser?userEm=${em}`,{
        method : 'PATCH',
        headers : {'Content-Type' : 'application/json'}
    });

    if(resp.ok){
        const data = await resp.json();

        if(data.isRecovered){
            alert(data.msg);
            loadUserli();
        }else {
            alert(data.msg)
            loadUserli();
        }

    }

}



async function loadScount(){

    const resp = await fetch("/admin/sessionCnt",{
        method : 'GET',
        headers : {"Content-Type" : "application/json"}
    });


    if(resp.ok){

        const data = await resp.json();

        if(data){
            const curCnt = data.curSsCnt;

            const tbody = document.getElementById("sessions");

            const tr = document.createElement("tr");

            const td = document.createElement("td");

            td.innerText = curCnt;

            tr.appendChild(td);
            tbody.appendChild(tr);

        } else{
            alert("오류");
        }

    }

}




async function last7daysChart() {
    try {
        // 데이터 가져오기
        const resp = await fetch("/admin/dayschart", {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!resp.ok) throw new Error("서버 응답 실패");

        const data = await resp.json();
        console.log(data);

        if (data) {
            const labels = Object.keys(data).reverse();
            const counts = Object.values(data).reverse();


            const cht = document.getElementById('weeklyChart').getContext('2d');
            // 차트 생성
            new Chart(cht, {
                type: "bar", // 막대 차트
                data: {
                    labels: labels, // X축 라벨
                    datasets: [{
                        label: "최근 7일 동안 접속량",
                        data: counts, // Y축 데이터
                        backgroundColor: "rgba(75, 192, 192, 0.2)", // 막대 색상
                        borderColor: "rgba(75, 192, 192, 1)", // 막대 테두리 색상
                        borderWidth: 1, // 막대 테두리 두께
                    }]
                },
                options: {
                    responsive: true, // 반응형 활성화
                    maintainAspectRatio: false, // 비율 유지 비활성화
                    scales: {
                        x: {
                            type: "category", // X축을 카테고리로 설정
                            ticks: {
                                autoSkip: false, // 모든 라벨 표시
                                maxRotation: 45, // 라벨 최대 회전 각도
                                minRotation: 0,  // 라벨 최소 회전 각도
                            },
                        },
                        y: {
                            beginAtZero: true, // Y축 0부터 시작
                            ticks: {
                                stepSize: 1, // 간격 1씩 증가
                            },
                        }
                    },
                    plugins: {
                        legend: {
                            display: true, // 범례 표시
                        },
                        tooltip: {
                            enabled: true, // 툴팁 활성화
                        }
                    }
                }
            });
        } else {
            console.error("데이터가 없습니다.");
        }
    } catch (error) {
        console.error("차트 생성 중 오류 발생:", error);
    }
}


async function adhome(){
    const loggedIn = await checkLogin();

    if(loggedIn.loggedIn){
        connetWsforAd();

    }

}

let admws = null;
function connetWsforAd(){
    const token = localStorage.getItem("accessToken");
    const wsUrl =  "wss://localhost:8080/ws-login?Authorization=" + encodeURIComponent('Bearer ' + token);

    admws = new WebSocket(wsUrl);

    admws.onopen = function (){
        console.log("로그인 성공");
        console.log("WebSocket 연결 상태:", admws.readyState);
    };
}


async function sendToAll(){

    const loggedIn = await checkLogin();
    if(loggedIn.loggedIn){
        if(!admws || admws.readyState !== WebSocket.OPEN ){
            console.log("연결 실패");
            connetWsforAd();
            return;
        }
        const msginput = document.getElementById("textArea");
        if (!msginput || !msginput.value.trim()) {
            alert("내용을 입력해주세요.");
            return;
        }
        const msg = {
            type: "to-All",
            payload: {
                sender: loggedIn.userEmail, // 발신자 이메일
                content: msginput.value
            }
        };
        if (admws && admws.readyState === WebSocket.OPEN){
            admws.send(JSON.stringify(msg));
            alert("공지 전송 완료");
            msginput.value = "";

        }else {
            alert("WebSocket 연결이 안정적이지 않습니다. 재연결을 시도합니다.");
            connetWsforAd();
        }
    }




}

