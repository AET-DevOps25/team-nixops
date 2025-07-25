import { useCallback, useEffect, useState } from "react";
import { SSE } from "sse.js";

type Message = {
  id: number;
  role: string;
  content: string;
};

function useChat(conversationId: string | null, api: string | null, studyId: number | null, semester: string | null) {
  const [currentQuestion, setCurrentQuestion] = useState("");
  const [messages, setMessages] = useState<Message[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);

  useEffect(() => {
    if (currentQuestion !== "" && conversationId !== null && studyId !== null && api !== null) {
      const source = new SSE(
        api +
          "/chat?prompt=" +
          currentQuestion +
          "&convId=" +
          conversationId +
          "&studyProgramId=" +
          studyId +
          "&semester=" +
          semester,
      );

      source.addEventListener("message", (e: any) => {
        setMessages((prev) => {
          const newMessages = [...prev];
          let currentMessageIndex = newMessages.length - 1;
          newMessages[currentMessageIndex] = {
            ...newMessages[currentMessageIndex],
            content: newMessages[currentMessageIndex].content + e.data,
          };
          return newMessages;
        });
      });
      source.addEventListener("readystatechange", (e: any) => {
        if (e.readyState === 1) {
          setIsGenerating(true);
        } else if (e.readyState === 2) {
          setIsGenerating(false);
        }
      });

      return () => source.close();
    }
  }, [currentQuestion, conversationId, studyId, api]);

  const sendMessage = useCallback(
    async (msg: string) => {
      if (msg !== "") {
        setIsGenerating(true);
        setCurrentQuestion(msg);
        setMessages((prev) => [
          ...prev,
          { id: messages.length, role: "user", content: msg },
        ]);
        setMessages((prev) => [
          ...prev,
          { id: messages.length + 1, role: "bot", content: "" },
        ]);
      }
    },
    [api, messages],
  );
  return { messages, sendMessage, isGenerating };
}

export default useChat;
