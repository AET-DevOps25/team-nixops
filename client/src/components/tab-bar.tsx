"use client";

import * as React from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

export function TabBar() {
  return (
    <div className="fixed top-4 z-29 flex w-full justify-center">
      <Tabs defaultValue="chat">
        <TabsList>
          <TabsTrigger value="chat">Chat</TabsTrigger>
          <TabsTrigger value="calendar">Calendar</TabsTrigger>
        </TabsList>
        <TabsContent value="chat">
        </TabsContent>
        <TabsContent value="calendar">
	  		</TabsContent>
      </Tabs>
    </div>
  );
}
