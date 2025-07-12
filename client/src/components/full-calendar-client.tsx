"use client";

import "@/app/calendar.css";
import React, { useEffect, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";

type Appointment = {
  appointmentType: string;
  moduleCode: string;
  moduleTitle: string;
  seriesBeginDate: string; // e.g. "2024-03-01"
  seriesEndDate: string;   // e.g. "2024-07-01"
  beginTime: string;       // e.g. "08:00"
  endTime: string;         // e.g. "10:00"
  weekdays: string[];      // e.g. ["Mo", "Di"]
};

type CalendarEvent = {
  id: string;
  title: string;
  start: string;
  end: string;
  allDay?: boolean;
};

const weekdayMap: Record<string, number> = {
  Mo: 1,
  Di: 2,
  Mi: 3,
  Do: 4,
  Fr: 5,
  Sa: 6,
  So: 0,
};

export default function FullCalendarClient({ conversationId }) {
  const [calendarEvents, setCalendarEvents] = useState<CalendarEvent[]>([]);
  const scheduleId = conversationId;

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        const res = await fetch(`/api/appointments?scheduleId=${scheduleId}`);
        if (!res.ok) throw new Error("Failed to fetch appointments");
        const appointments: Appointment[] = await res.json();

        const events: CalendarEvent[] = [];

        appointments.forEach((appt, index) => {
          const startDate = new Date(appt.seriesBeginDate);
          const endDate = new Date(appt.seriesEndDate);

          for (
            let d = new Date(startDate);
            d <= endDate;
            d.setDate(d.getDate() + 1)
          ) {
            const jsWeekday = d.getDay(); // 0 (Sun) - 6 (Sat)

            const isoWeekday = Object.entries(weekdayMap).find(
              ([, val]) => val === jsWeekday
            );

            if (isoWeekday && appt.weekdays.includes(isoWeekday[0])) {
              const start = new Date(
                `${d.toISOString().split("T")[0]}T${appt.beginTime}`
              );
              const end = new Date(
                `${d.toISOString().split("T")[0]}T${appt.endTime}`
              );

              events.push({
                id: `${appt.moduleCode}-${index}-${start.toISOString()}`,
                title: `${appt.moduleTitle} (${appt.appointmentType})`,
                start: start.toISOString(),
                end: end.toISOString(),
              });
            }
          }
        });

        setCalendarEvents(events);
      } catch (err) {
        console.error("Error loading schedule:", err);
      }
    };

    fetchAppointments();
  }, [scheduleId]);

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
