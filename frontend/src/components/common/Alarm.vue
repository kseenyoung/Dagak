<template>
  <!-- 알림 아이콘 -->
  <button
    class="position-relative alarm"
    data-bs-toggle="modal"
    data-bs-target="#exampleModal"
  >
    <i class="bi bi-bell"></i>
    <span
      class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
    >
      {{ alarmStore.alarmUnReadTotal }}
    </span>
  </button>
</template>

<script setup>
import { useAlarmStore } from '@/stores/alarm';
const alarmStore = useAlarmStore();
</script>

<style lang="scss" scoped>
.alarm {
  width: 30px;
  height: 30px;
  margin-right: 20px;
  border: none;
  background-color: transparent;

  i {
    position: relative;
    top: -22px;
    left: 0px;
    font-size: 1.4rem;
    color: white;
  }

  .bg-danger {
    background-color: $color-light-5 !important;
  }
}
</style>
