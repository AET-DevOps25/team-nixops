"use client";

import { Button } from "@/components/ui/button";
import {
  ChatBubble,
  ChatBubbleAction,
  ChatBubbleActionWrapper,
  ChatBubbleAvatar,
  ChatBubbleMessage,
} from "@/components/ui/chat/chat-bubble";
import { ChatInput } from "@/components/ui/chat/chat-input";
import { ChatMessageList } from "@/components/ui/chat/chat-message-list";
import { Copy, CornerDownLeft } from "lucide-react";
import useChat from "./lib";
import { useForm } from "react-hook-form";
import { ScrollArea } from "@/components/ui/scroll-area";

export default function Chat() {
  let api = process.env.NEXT_PUBLIC_SERVER_API_URL || "http://localhost:8000";
  const { messages, sendMessage, isGenerating } = useChat(api);
  const { register, handleSubmit, reset, getValues } = useForm();

  const submit = (data: string) => {
    sendMessage(data);
    reset({
      message: "",
    });
  };
  const handleOnKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      if (isGenerating) return;
      let message = getValues("message");
      console.log(message);
      submit(message);
    }
  };

  return (
    <div className="flex flex-col h-screen">
      <div className="h-17/20">
        <ChatMessageList>
          {messages.map((message) => (
            <ChatBubble
              variant={message.role === "user" ? "sent" : "received"}
              key={message.id}
            >
              <ChatBubbleAvatar
                fallback={message.role === "user" ? "🎓" : "🤖"}
              />
              <ChatBubbleMessage
                variant={message.role === "user" ? "sent" : "received"}
                isLoading={message.content === ""}
              >
                {message.content}
                {message.role === "bot" && (
                  <ChatBubbleActionWrapper>
                    <ChatBubbleAction
                      className="size-6"
                      key="copy"
                      icon={<Copy className="size-3" />}
                      onClick={() =>
                        navigator.clipboard.writeText(message.content)
                      }
                    />
                  </ChatBubbleActionWrapper>
                )}
              </ChatBubbleMessage>
            </ChatBubble>
          ))}
        </ChatMessageList>
      </div>
      <div className="flex-grow" />
      <form
        className="relative rounded-lg border bg-background focus-within:ring-1 focus-within:ring-ring p-1 m-5"
        onSubmit={handleSubmit((data) => {
          submit(data.message);
        })}
      >
        <ChatInput
          placeholder="Type your message here..."
          className="min-h-12 resize-none rounded-lg bg-background border-0 p-3 shadow-none focus-visible:ring-0"
          onKeyDown={handleOnKeyDown}
          {...register("message")}
        />
        <div className="flex items-center p-3 pt-0">
          <Button
            type="submit"
            disabled={isGenerating}
            size="sm"
            className="ml-auto gap-1.5"
          >
            Send Message
            <CornerDownLeft className="size-3.5" />
          </Button>
        </div>
      </form>
    </div>
  );
}
