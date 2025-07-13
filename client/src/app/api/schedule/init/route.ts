import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  const schedule_id = req.nextUrl.searchParams.get("scheduleId");

  if (!schedule_id) {
    return NextResponse.json({ error: 'Missing schedule_id' }, { status: 400 });
  }

  const body = await req.json();

  if (
    typeof body.studyId !== 'number' ||
    typeof body.semester !== 'string'
  ) {
    return NextResponse.json({ error: 'Invalid request body' }, { status: 400 });
  }

  try {
    const response = await fetch(`http://localhost:8042/schedule/${schedule_id}/init`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (response.status === 204) {
      return new NextResponse(null, { status: 204 });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Error from upstream server' }, { status: response.status });
    }
  } catch (err) {
    return NextResponse.json({ error: 'Internal server error', detail: (err as Error).message }, { status: 500 });
  }
}
