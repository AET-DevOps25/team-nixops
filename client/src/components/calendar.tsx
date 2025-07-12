import React, { useState } from "react";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";

type Event = {
  title: string;
  date: string; // in "YYYY-MM-DD" format
};

type Props = {
  events: Event[];
};

export default function MyCalendar({ events }: Props) {
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  // Convert event dates to Date objects for comparison
  const eventDates = events.map(e => new Date(e.date));

  // Get events for the selected date
  const eventsForSelectedDate = selectedDate
    ? events.filter(
        e =>
          new Date(e.date).toDateString() === selectedDate.toDateString()
      )
    : [];

  // Tile content to highlight event dates
  function tileContent({ date, view }: { date: Date; view: string }) {
    if (
      view === "month" &&
      eventDates.find(d => d.toDateString() === date.toDateString())
    ) {
      return <div style={{
        backgroundColor: "#3b82f6",
        borderRadius: "50%",
        width: 8,
        height: 8,
        margin: "0 auto",
        marginTop: 2,
      }} />;
    }
    return null;
  }

  return (
    <div style={{ padding: 16 }}>
      <Calendar
        onClickDay={setSelectedDate}
        tileContent={tileContent}
        calendarType="US"
      />

      {selectedDate && (
        <div style={{ marginTop: 16 }}>
          <h3>Events on {selectedDate.toDateString()}:</h3>
          {eventsForSelectedDate.length === 0 ? (
            <p>No events</p>
          ) : (
            <ul>
              {eventsForSelectedDate.map((e, i) => (
                <li key={i}>{e.title}</li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
