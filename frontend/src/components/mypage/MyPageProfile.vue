<template>
  <div class="mypage-profile">
    <div>
      <img
        class="profile"
        v-if="userStore.loginUserInfo.userPicture"
        :src="`${userStore.loginUserInfo.userPicture}?timestamp=${new Date().getTime()}`"
      />
      <img class="profile" v-else src="@/assets/img/default.jpg" />
      <div>{{ userStore.loginUserInfo.userNickname }}</div>
      <div>{{ userStore.loginUserInfo.userEmail }}</div>
    </div>
    <div>
      <span v-if="userTotalStudyTime != null">
        {{ formatTime(userTotalStudyTime) }} |
      </span>
      <span v-else>&nbsp;- 분 | </span>

      <span v-if="userStore.loginUserInfo.userRank != null">
        {{ userStore.loginUserInfo.userRank }}위 |
      </span>
      <span v-else>&nbsp;- 위 | </span>

      <span>
        {{ userStore.loginUserInfo.userPoint }}
        <img src="@/assets/img/item/coin.png" class="coin" />
      </span>
    </div>
    <div>
      <span v-if="userStatusMessage">{{ userStatusMessage }}</span>
      <span v-else>상태메시지가 없습니다.</span>
      <i
        class="bi bi-pencil-fill common-pointer"
        data-bs-toggle="collapse"
        data-bs-target="#statusmsg-accordion"
        aria-expanded="true"
        aria-controls="collapseOne"
      ></i>
      <div
        id="statusmsg-accordion"
        class="accordion-collapse collapse"
        aria-labelledby="headingOne"
      >
        <div class="accordion-body input-group">
          <input
            type="text"
            id="statusmsg"
            :value="userStatusMessage"
            @input="onInputStatus"
          />
          <button class="btn common-btn" @click="changeStatus">수정</button>
        </div>
      </div>
    </div>
    <div v-if="userStore.loginUserInfo.mokkojiName != null">
      모꼬지: {{ userStore.loginUserInfo.mokkojiName }}
    </div>
    <div v-else>가입된 모꼬지가 없습니다</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { useUserStore } from "@/stores/user";
const userStore = useUserStore();
const userStatusMessage = ref("");
const userTotalStudyTime = ref("");
const formatTime = (seconds) => {
  if (isNaN(seconds) || seconds < 0) {
    return "Invalid input";
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const formattedHours = String(hours).padStart(2, "0");
  const formattedMinutes = String(minutes).padStart(2, "0");

  return `${formattedHours}시간 ${formattedMinutes}분`;
};

onMounted(() => {
  getUserProfileInfo();
});

const getUserProfileInfo = function () {
  const body = {
    sign: "getUserInformation",
    userNickname: userStore.loginUserInfo.userNickname,
  };
  axios
    .post(`${import.meta.env.VITE_API_BASE_URL}user`, body, {
      headers: {
        "Content-Type": "application/json",
      },
    })
    .then((res) => {
      userStatusMessage.value = res.data.result.userStatusMessage;
      userTotalStudyTime.value = res.data.result.userTotalStudyTime;
    });
};

const onInputStatus = function (event) {
  userStatusMessage.value = event.currentTarget.value;
};

const changeStatus = function () {
  const body = {
    sign: "ModifyUserStatusMessage",
    newStatusMessage: userStatusMessage.value,
  };
  axios
    .post(`${import.meta.env.VITE_API_BASE_URL}user`, body, {
      headers: {
        "Content-Type": "application/json",
      },
    })
    .then((res) => res.data)
    .then((json) => {
      if (json.code === 1000) {
        //성공
        alert("수정되었습니다.");
      } else {
        //실패
        alert("실패");
      }
    });
};
</script>

<style lang="scss" scoped>
.mypage-profile {
  background-color: white;
  flex-basis: 300px;
  text-align: center;
  margin: 20px 0px;
  border-radius: 10px;
  // box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
  box-shadow: rgba(0, 0, 0, 0.4) 0px 2px 4px, rgba(0, 0, 0, 0.3) 0px 7px 13px -3px,
    rgba(0, 0, 0, 0.2) 0px -3px 0px inset;
  padding: 120px 0px;

  > div {
    margin-bottom: 30px;
  }
}
img {
  width: 120px;
  border-radius: 50%;
}
.profile {
  width: 100px;
  height: 100px;
}
.coin {
  width: 25px;
}

i {
  margin: 0px 5px;
}

.accordion-collapse {
  padding: 0px 10px;
}
.accordion-body {
  display: flex;
  margin-top: 5px;
  justify-content: center;

  > input {
    width: 65%;
    font-size: 1rem;
    border: 1px solid rgb(43, 43, 43);
    border-top-left-radius: var(--bs-border-radius);
    border-bottom-left-radius: var(--bs-border-radius);
  }
  > input:focus {
    outline: none;
  }
}
</style>
