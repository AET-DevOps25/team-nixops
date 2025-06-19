import { useCallback, useEffect, useState } from "react";
import { SSE } from "sse.js";

type Message = {
	id: number;
	role: string;
	content: string;
}

function useChat(api: string) {
  const [currentQuestion, setCurrentQuestion] = useState("");
  const [messages, setMessages] = useState<Message[]>([]);

useEffect(() => {
	if(currentQuestion !== ""){
    const source = new SSE("http://localhost:8000/stream?prompt=" + currentQuestion, {withCredentials: true}); // credentials passess cookies

    source.addEventListener("message", (e: any) => {
        setMessages(prev => {
            const newMessages = [...prev];
				let currentMessageIndex = newMessages.length - 1;
            newMessages[currentMessageIndex] = {
                ...newMessages[currentMessageIndex],
                content: newMessages[currentMessageIndex].content + e.data
            };
            return newMessages; // Return the new state
        });
    });

    return () => source.close();
	}
}, [currentQuestion]);

  const sendMessage = useCallback(async (msg: string) => {
	if(msg !== ""){
	  setCurrentQuestion(msg);
		setMessages(prev => [...prev, {id: messages.length, role: "user", content: msg}])
		setMessages(prev => [...prev, {id: messages.length + 1, role: "bot", content: ""}])
	}
	 }, [api]);
  return { messages, sendMessage };
}

export default useChat;
