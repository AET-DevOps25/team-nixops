"use client";

import * as React from "react";
import { Moon, Sun, SunMoon, CodeXml, Menu } from "lucide-react";
import { useTheme } from "next-themes";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import pkg from "../../package.json";
import { Dialog } from "@radix-ui/react-dialog";
import { StudyProgramSelector } from "./study-program-selector";

const version = pkg.version;

export function MenuOverlay({setStudyProgramId, setSemester}) {
  const { setTheme } = useTheme();
  const [isDialogOpen, setIsDialogOpen] = useState(true);

  return (
    <div className="fixed top-4 left-4 z-30">
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <StudyProgramSelector setStudyProgramId={setStudyProgramId} setSemester={setSemester} setIsDialogOpen={setIsDialogOpen}/>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="icon">
              <Menu />
              <span className="sr-only">Open Menu</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-56" align="start">
            <DropdownMenuGroup>
              <DropdownMenuLabel>Settings</DropdownMenuLabel>
              <DropdownMenuSub>
                <DropdownMenuSubTrigger>Theme</DropdownMenuSubTrigger>
                <DropdownMenuPortal>
                  <DropdownMenuSubContent>
                    <DropdownMenuItem onClick={() => setTheme("light")}>
                      <Sun /> Light
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => setTheme("dark")}>
                      <Moon /> Dark
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => setTheme("system")}>
                      <SunMoon /> System
                    </DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuPortal>
              </DropdownMenuSub>
              <DropdownMenuItem onClick={() => setIsDialogOpen(true)}>
                Study Program
              </DropdownMenuItem>
            </DropdownMenuGroup>

            <DropdownMenuGroup>
              <DropdownMenuSeparator />
              <DropdownMenuLabel>About</DropdownMenuLabel>
              <DropdownMenuItem
                onClick={() =>
                  (location.href =
                    "https://github.com/AET-DevOps25/team-nixops")
                }
              >
                <CodeXml /> Source
                <Badge variant="outline">v{version}</Badge>
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </Dialog>
    </div>
  );
}
