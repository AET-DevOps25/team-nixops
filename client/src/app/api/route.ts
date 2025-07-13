import { config } from "@/lib/config";

export async function GET() {
  return Response.json({ url: config.apiBaseUrl })
}
