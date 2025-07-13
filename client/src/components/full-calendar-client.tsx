"use client";

import "@/app/calendar.css";
import React, { useEffect, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";

import { useSession } from '@/lib/sessionContext';

type Appointment = {
  appointmentType: string;
  moduleCode: string;
  moduleTitle: string;
  seriesBeginDate: string;
  seriesEndDate: string;
  beginTime: string;
  endTime: string;
  weekdays: string[];
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

export default function FullCalendarClient() {
  const [calendarEvents, setCalendarEvents] = useState<CalendarEvent[]>([]);

  const { sessionId, semester } = useSession();

  useEffect(() => {
    let isMounted = true; // to avoid setting state if unmounted

    const fetchAppointments = async () => {
      try {
        const res = await fetch(`/api/appointments?scheduleId=${sessionId}&semester=${semester}`);
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

        if (isMounted) setCalendarEvents(events);
      } catch (err) {
        console.error("Error loading schedule:", err);
      }
    };

    fetchAppointments();

    const intervalId = setInterval(fetchAppointments,  1000);

    return () => {
      isMounted = false;
      clearInterval(intervalId);
    };
  }, [sessionId]);

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
