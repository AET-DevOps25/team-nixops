import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
  const scheduleId = req.nextUrl.searchParams.get("scheduleId");

  if (!scheduleId) {
    return NextResponse.json({ error: "Missing scheduleId" }, { status: 400 });
  }

  const backendUrl = `http://localhost:8042/schedule/${scheduleId}/appointments`;

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


