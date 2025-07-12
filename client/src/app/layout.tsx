import type { Metadata } from "next";
import { ThemeProvider } from "@/components/theme-provider";
import "./globals.css";
import { PetApiFactory, Configuration } from "../api";
import FullCalendarClient from "@/components/FullCalendarClient";

const petApiConfig = new Configuration({ basePath: "http://localhost:8000" });
const petApi = PetApiFactory(petApiConfig);

export const metadata = {
  title: "My App",
  description: "Description",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head />
      <body>
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange>
          <div style={{ display: "flex", height: "100vh" }}>
            <div style={{ flex: 1, borderRight: "1px solid #ddd", overflowY: "auto" }}>
              {children}
            </div>

            <div style={{ width: 800, borderLeft: "1px solid #ddd", overflowY: "auto" }}>
              <FullCalendarClient/>
            </div>
          </div>
        </ThemeProvider>
      </body>
    </html>
  );
}
