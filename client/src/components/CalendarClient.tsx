"use client";

import React, { useState } from "react";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";

type Event = {
  date: string;
  title: string;
};

const events: Event[] = [
  { date: "2025-07-12", title: "Meeting with team" },
  { date: "2025-07-14", title: "Project deadline" },
];

export default function CalendarClient() {
  const [date, setDate] = useState(new Date());

  // Helper to check if a date has an event
  const getEventsForDate = (date: Date) =>
    events.filter(
      (event) => new Date(event.date).toDateString() === date.toDateString()
    );

  return (
    <Calendar
      onChange={setDate}
      value={date}
      tileContent={({ date, view }) => {
        if (view === "month") {
          const dayEvents = getEventsForDate(date);
          return dayEvents.length > 0 ? (
            <ul style={{ margin: 0, padding: 0, listStyle: "none" }}>
              {dayEvents.map((event, index) => (
                <li
                  key={index}
                >
                  {event.title}
                </li>
              ))}
            </ul>
          ) : null;
        }
        return null;
      }}
    />
  );
}
