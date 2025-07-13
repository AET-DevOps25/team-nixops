import { NextRequest, NextResponse } from "next/server";
import { config } from "@/lib/config";

export async function GET(req: NextRequest) {
  const backendUrl = `${config.genaiBaseUrl}/embed/studyPrograms`;

  try {
    const response = await fetch(backendUrl);

    if (!response.ok) {
      return NextResponse.json(
        { error: "Failed to fetch study programs from backend" },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("Error fetching study programs:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

