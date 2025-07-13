import type { Metadata } from "next";
import type { ReactNode } from "react"
import { useState, useRef } from "react"

import { ThemeProvider } from "@/components/theme-provider";
import ResizableLayout from "@/components/resizable-layout"

import { PetApiFactory, Configuration } from "../api";

import "./globals.css";

const petApiConfig = new Configuration({ basePath: "http://localhost:8000" });
const petApi = PetApiFactory(petApiConfig);

export const metadata = {
  title: "My App",
  description: "Description",
};

export default function RootLayout({
  children,
}: {
  children: ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head />
      <body>
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          <ResizableLayout>{children}</ResizableLayout>
        </ThemeProvider>
      </body>
    </html>
  )
}
