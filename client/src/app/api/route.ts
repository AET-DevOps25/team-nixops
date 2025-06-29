export async function GET() {
  return Response.json({ url: process.env.API_URL })
}
