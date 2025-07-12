import { useCallback, useEffect, useState } from "react";
import { SSE } from "sse.js";

type Message = {
  id: number;
  role: string;
  content: string;
};

function useChat(conversationId: String, api: string, studyProgramId: number, semester: string) {
  const [currentQuestion, setCurrentQuestion] = useState("");
  const [messages, setMessages] = useState<Message[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);

  useEffect(() => {
    if (currentQuestion !== "") {
      const source = new SSE(
        api +
          "/chat?prompt=" +
          currentQuestion +
          "&convId=" +
          conversationId +
          "&studyProgramId=" +
          studyProgramId +
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
          return newMessages; // Return the new state
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
  }, [currentQuestion]);

  const sendMessage = useCallback(
    async (msg: string) => {
      if (msg !== "") {
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
