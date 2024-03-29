import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useQuestionStore = defineStore('questionStore', () => {
  // state: () => {
  //   return {
  //     // all these properties will have their type inferred automatically
  //     question: []
  //   }
  // }
  const question = ref([])
  const answer = ref(new Map())

  const setQuestion = async function (data) {
    question.value.push(data)
  }

  const setAnswer = async function (questionId, data) {

    if (!answer.value.has(questionId)) {
      answer.value.set(questionId, [])
    }

    answer.value.get(questionId).push(data)
  }

  return { question, setQuestion, answer, setAnswer }
})
