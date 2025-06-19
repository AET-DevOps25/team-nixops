"use client";

import { Button } from "@/components/ui/button";
import {
  ChatBubble,
  ChatBubbleAvatar,
  ChatBubbleMessage,
} from "@/components/ui/chat/chat-bubble";
import { ChatInput } from "@/components/ui/chat/chat-input";
import { ChatMessageList } from "@/components/ui/chat/chat-message-list";
import { CornerDownLeft, Mic, Paperclip } from "lucide-react";
import { useState } from "react";
import useChat from "./lib/chat";
import { useForm } from "react-hook-form";
import { ScrollArea } from "@/components/ui/scroll-area"

export default function Chat() {
  const { messages, sendMessage } = useChat("");
  const {
    register,
    handleSubmit,
	 reset,
  } = useForm();

  return (
    <div className="flex flex-col h-screen">
	     <ScrollArea className="h-17/20">
      <ChatMessageList>
        {messages.map((message) => (
          <ChatBubble variant={message.role === "user" ? "sent" : "received"} key={message.id}>
            <ChatBubbleAvatar
              fallback={message.role === "user" ? "🎓" : "🤖"}
            />
            <ChatBubbleMessage
              variant={message.role === "user" ? "sent" : "received"}
				  isLoading={message.content === ""}
            >
              {message.content}
            </ChatBubbleMessage>
          </ChatBubble>
        ))}
      </ChatMessageList>
		    </ScrollArea>
	 		<div className = "flex-grow"/>
      <form
        className="relative rounded-lg border bg-background focus-within:ring-1 focus-within:ring-ring p-1 m-5"
        onSubmit={
			  handleSubmit((data) => {
			reset({
          message: ""
        }, {
          keepErrors: true, 
          keepDirty: true,
        });
			  sendMessage(data.message);
		  })}
      >
        <ChatInput
          placeholder="Type your message here..."
          className="min-h-12 resize-none rounded-lg bg-background border-0 p-3 shadow-none focus-visible:ring-0"
          {...register("message")}
        />
        <div className="flex items-center p-3 pt-0">
          <Button type="submit" size="sm" className="ml-auto gap-1.5">
            Send Message
            <CornerDownLeft className="size-3.5" />
          </Button>
        </div>
      </form>
    </div>
  );
}
