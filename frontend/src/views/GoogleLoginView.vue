<template>
  <div class="container">
    <div class="card">
      <div class="card-header">
        <div>연동하려면 로그인 하세요.</div>
      </div>
      <div class="card-body">
        <div class="mb-3">
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-person"></i></span>
            <input
              :disabled="disableInputId"
              type="text"
              class="form-control no-outline"
              id="id"
              placeholder="아이디"
              required
              autofocus
              v-model="id"
            />
          </div>
        </div>
        <div class="mb-3">
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-lock"></i></span>
            <input
              :disabled="disableInputPassword"
              type="password"
              class="form-control no-outline"
              id="password"
              placeholder="비밀번호"
              required
              v-model="password"
              @keyup.enter="login"
            />
          </div>
        </div>
        <div class="mb-3 form-check">
          <input
            :disabled="disableCheckId"
            type="checkbox"
            class="form-check-input no-outline"
            id="rememberId"
          />
          <label class="form-check-label" for="rememberId">아이디 저장</label>
        </div>
        <button
          class="btn btn-primary common-btn"
          :disabled="disableLoginButton"
          @click="login"
        >
          로그인
        </button>
        <!-- <div class="or-seperator"><i>또는</i></div> 
          <div class="text-center social-btn">
            <img src="@/assets/img/login/googleLoginImg.png" alt="구글로그인" @click="googleLogin()"/>
            <img src="@/assets/img/login/kakaoLoginImg.png" alt="카카오로그인" />
          </div>
          -->
      </div>
    </div>

    <div class="sub-card">
      <RouterLink to="/findid">아이디 찾기</RouterLink><span>&nbsp;|&nbsp;</span>
      <RouterLink to="/findpw">비밀번호 찾기</RouterLink><span>&nbsp;|&nbsp;</span>
      <RouterLink to="/regist">회원가입</RouterLink>
    </div>

    <vue-recaptcha
      v-show="true"
      sitekey="6Lcufl8pAAAAAN7h2t1u9Dgm1_zo9wKoaYRX59H6"
      @verify="recaptchaVerified"
      @expire="recaptchaExpired"
      @fail="recaptchaFailed"
      @error="recaptchaError"
    ></vue-recaptcha>
  </div>
</template>

<script setup>
import { ref, onBeforeMount } from 'vue';
import axios from 'axios';
import vueRecaptcha from 'vue3-recaptcha2';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
const id = ref('');
const password = ref('');
// const rememberId = ref(false);

// reCAPTCHA
const disableInputId = ref(true);
const disableInputPassword = ref(true);
const disableCheckId = ref(true);
const disableLoginButton = ref(true);

// 그러면 누르자마자 ,,,, axios 보내고 email 보내오면 바로 홈 화면, 아니면 그대로 남아있기?

onBeforeMount(async () => {
  const code = route.query.code;
  await axios
    .get(
      `${import.meta.env.VITE_API_BASE_URL}user` +
        '/googleOAuth?googleCode=' +
        code,
    )
    .then((res) => {
      if (res.data.code == 1000) {
        userStore.getLoginUserInfo();
        // alert("로그인에 성공했습니다.")
        router.push({
          name: 'home',
        });
      }
    });
});

//로그인
const login = async function () {
  const body = {
    sign: 'login',
    userId: id.value,
    userPassword: password.value,
  };
  await axios
    .post(`${import.meta.env.VITE_API_BASE_URL}user`, body, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
    .then((res) => {
      if (res.data.code === 1000) {
        userStore.getLoginUserInfo();
        //성공 시 홈으로
        // alert('연동에 성공했습니다.');
        router.push({
          name: 'home',
        });
      } else if (res.data.code === 1405) {
        alert(res.data.result);
      }
    });
  id.value = '';
  password.value = '';
};

// const getGoogleToken = async function () {
//   const code = route.query.code
//   await axios.get(`${import.meta.env.VITE_API_BASE_URL}user`  + "/googleOAuth?googleCode=" + code)
//   .then((res) => {
//     console.log(res.data);
//     if (res.data.code=1000) {
//       router.push({
//           name: 'home',
//         });
//     }
//   })
// }

//로그인
// const login = async function () {
//   const body = {
//     sign: 'login',
//     userId: id.value,
//     userPassword: password.value,
//   };
//   await axios
//     .post(`${import.meta.env.VITE_API_BASE_URL}user`, body, {
//       headers: {
//         'Content-Type': 'application/json',
//       },
//     })
//     .then((res) => {
//       if (res.data.code === 1000) {
//         userStore.getLoginUserInfo();
//         //성공 시 홈으로
//         router.push({
//           name: 'home',
//         });
//       } else if (res.data.code === 1405) {
//         alert(res.data.result);
//       }
//     });
//   id.value = '';
//   password.value = '';
// };

const recaptchaExpired = async function (response) {
  disableInputId.value = true;
  disableInputPassword.value = true;
  disableCheckId.value = true;
  ``;
  disableLoginButton.value = true;
  const body = {
    recaptchaResponse: '만료',
  };
  await axios.post(`${import.meta.env.VITE_API_BASE_URL}user/recaptcha`, body, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

const recaptchaVerified = async function (response) {
  disableInputId.value = false;
  disableInputPassword.value = false;
  disableCheckId.value = false;
  disableLoginButton.value = false;
  const body = {
    recaptchaResponse: response,
  };
  await axios
    .post(`${import.meta.env.VITE_API_BASE_URL}user/recaptcha`, body, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
    .then((res) => res.data);
};
</script>

<style lang="scss" scoped>
.container {
  max-width: 450px;
  margin: 80px auto;
  letter-spacing: -0.4px;
}

.card {
  border: 1px solid rgb(226, 226, 226);
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  padding: 0px 10px 10px;
}

.card-header {
  background-color: white;
  color: rgb(44, 44, 44);
  text-align: center;
  border-bottom: none;
  padding: 30px 15px 10px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

img {
  width: 130px;
}

.form-control {
  border-radius: 5px;
}

.btn-primary {
  border: none;
  width: 100%;
}

.float-end {
  color: black;
  text-decoration: none;
  font-size: 14px;
}

.social-btn {
  display: flex;
  flex-direction: column;
  align-items: center;

  .btn {
    border: none;
    margin: 10px 3px 0;
    opacity: 1;
    width: 100%;
    height: 35px;

    &:hover {
      opacity: 0.9;
    }
  }

  .btn-primary {
    background: #fee500;
  }

  .btn-danger {
    background: #df4930;
  }

  > img {
    width: fit-content;
    cursor: pointer;
    padding: 8px;
  }
}

.or-seperator {
  margin-top: 40px;
  text-align: center;
  border-top: 1px solid #ccc;

  i {
    padding: 0 10px;
    background: #ffffff;
    position: relative;
    top: -11px;
    z-index: 1;
  }
}

.input-group {
  border: 1px solid #ced4da;
  border-radius: 5px;
  margin-bottom: 15px;

  .input-group-text {
    background-color: white;
    border: none;
    color: rgb(44, 44, 44);
    height: 100%;

    i {
      font-size: 1.2rem;
      line-height: inherit;
    }
  }
}

.no-outline {
  outline: none !important;
  box-shadow: none !important;
  border: 0px;
}

.sub-card {
  display: flex;
  justify-content: center;
  align-items: center;
  letter-spacing: -0.4px;

  > a {
    padding: 10px;
    color: rgb(44, 44, 44);
    font-size: 0.92rem;
    text-decoration: none;
  }

  span {
    font-size: small;
  }
}

.form-check-input {
  border: 1px solid rgb(113, 113, 113);
}

label {
  font-size: 1rem;
  position: relative;
  top: -2.5px;
}
</style>
