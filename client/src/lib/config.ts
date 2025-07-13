export const config = {
  apiBaseUrl: process.env.API_URL || "http://localhost:8000",
  scheduleManagerBaseUrl: process.env.SCHEDULE_MANAGER_BASE_URL || "http://localhost:8042",
  genaiBaseUrl: process.env.GENAI_BASE_URL || "http://localhost:8000",
};
