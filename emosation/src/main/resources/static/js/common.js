document.addEventListener('DOMContentLoaded', function (){





    if(window.location.pathname==='/'){
        const signUpform = document.getElementById("sign-up")
        signUpform.addEventListener('submit',function (event){
            event.preventDefault();
            const name = document.getElementById("name").value;
            const email = document.getElementById("email").value;
            const pw = document.getElementById("upPw").value;
            const phone = document.getElementById("Phone").value;
            const confirmpw = document.getElementById("cupPW").value;
            const body = JSON.stringify({
                Name : name,
                Email : email,
                Pw : pw,
                Phone : phone,
            })

            fetch('/register',{
                method : 'POST',
                headers : {'Content-Type': 'application/json'},

                body : body,

            })
                .then(resp =>{
                    if (!resp){
                        alert("회원가입 실패 다시시도");
                        return;
                    }
                    alert("회원가입 성공");
                    window.location.href = '/';
                })
                .catch((error) =>{
                    alert("회원가입 실패");
                })

        });

    }

    if(window.location.pathname === '/main'){

            main();



    }



});

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
            localStorage.setItem("accessToken",data.accessToken);
            localStorage.setItem("refreshToken",data.refreshToken);

            alert(data.msg);
            window.location.href ='/main';
        }else{
            alert(data.msg);
        }
    }catch (error) {
        alert("서버 요청 실패" + error);
    }






}

async function main(){
    const LoggedIn = await checkLogin();

    if(LoggedIn.loggedIn){
        const userId = LoggedIn.userId;
        try{
            const resp = await fetch(`/main/myfrlist?userid=${userId}`,{
                method:'GET',
                headers:{'Content-Type':'application/json'},

            });
            if(resp.ok){
                const data = await resp.json();
               if(data && data.length > 0){
                   const fr = data[0];
                   const tr = document.getElementById('frlist');
                   const td = document.createElement('td');
                   td.innerText = fr.name;  // 이메일을 td로 설정
                   tr.appendChild(td);

               }
            }else {
                alert("서버응답 실패");
            }
        }catch (error){
            alert("호출 실패" + error);
        }
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
            console.log('로그인 상태 확인됨:', data.userId);
            if (data.accessToken) {
                localStorage.setItem('accessToken', data.accessToken);
            }
            userId = data.userId;

            return { loggedIn: true, userId: data.userId };
        } else {
            console.log('인증 실패:', response.status);
            window.location.href = '/'
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');

            alert("인증 실패");

            return { loggedIn: false};
        }
    } catch (error) {
        console.error('서버 요청 실패:', error);
        alert("서버 요청 실패");
        return { loggedIn: false};
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
                const modal = document.querySelector('.modal')
                const close = document.querySelector('.close_btn')
                const tr = document.getElementById("searchResult");
                const td = document.createElement('td')
                // 모달창 내용
                td.innerText = data.user.name;
                td.id = data.user.id;

                const btntd = document.createElement('td')
                const btn = document.createElement('button');
                    btn.className="button button--wayra button--border-medium button--text-upper button--size-s button--text-thick button--inverted"
                    btn.innerText="친구추가"
                btntd.appendChild(btn);

                tr.appendChild(td);
                tr.appendChild(btntd);
                if(data.user.name){
                    modal.style.display = 'block'; // 데이터가 있어야만 뛰우자.
                }
                close.addEventListener('click',function (){
                    modal.style.display = 'none';
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
