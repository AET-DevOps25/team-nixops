"use client";

import "@/app/calendar.css";
import React, { useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";

const events = [
  { id: "1", title: "Meeting with LLM", start: "2025-07-12T10:00:00", end: "2025-07-12T13:00:00" },
  { id: "2", title: "Code review", start: "2025-07-07T14:00:00", end: "2025-07-07T15:30:00" },
];

export default function FullCalendarClient() {
  const [calendarEvents, setCalendarEvents] = useState(events);

  return (
    <FullCalendar
      plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
      initialView="timeGridWeek"
      headerToolbar={{
        left: "prev,next today",
        center: "title",
        right: "dayGridMonth,timeGridWeek,timeGridDay",
      }}
      events={calendarEvents}
      editable={false}
      height="100%"
      eventColor="#3b82f6"
      />
  );
}
