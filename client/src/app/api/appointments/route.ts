import { NextRequest, NextResponse } from "next/server";

import { config } from "@/lib/config";

export async function GET(req: NextRequest) {
  const scheduleId = req.nextUrl.searchParams.get("scheduleId");
  const semester = req.nextUrl.searchParams.get("semester");

  if (!scheduleId) {
    return NextResponse.json({ error: "Missing scheduleId" }, { status: 400 });
  }

  if (!semester) {
    return NextResponse.json({ error: "Missing semester" }, { status: 400 });
  }

  const backendUrl = `${config.scheduleManagerBaseUrl}/schedule/${scheduleId}/appointments?semester=${semester}`;

  try {
    const response = await fetch(backendUrl);

    if (!response.ok) {
      return NextResponse.json(
        { error: "Failed to fetch from backend" },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error fetching appointments:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}


