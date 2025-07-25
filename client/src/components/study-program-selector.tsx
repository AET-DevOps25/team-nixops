"use client";

import * as React from "react";
import { Check, ChevronsUpDown } from "lucide-react";

import {
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Form, useForm } from "react-hook-form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { useEffect, useState } from "react";

import { useSession } from '@/lib/sessionContext';

import { config } from "@/lib/config";

export function StudyProgramSelector({
  setIsDialogOpen,
}:{
  setIsDialogOpen: any;
}) {
  interface StudyProgram {
    id: string;
    title: string;
    semesters: string[];
  }

  const { updateStudyId, updateSemester } = useSession();
  
  const [open, setOpen] = useState(false);
  const [sem, setSem] = useState("");
  const [studyPrograms, setStudyPrograms] = useState<StudyProgram[]>([]);
  const [localSemesters, setLocalSemesters] = useState<string[]>([]);
  const { register, handleSubmit, reset, getValues, setValue } = useForm();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(
          "api/study-programs",
        );
        const result = await response.json();
        setStudyPrograms(result);
        console.log(result);
      } catch {
        console.error("An error occurred while fetching study programs");
      }
    };

    fetchData();
  }, []);

  const onSubmit = (data: any) => {
    console.log(data);
    if (data.studyProgram && data.semester) {
      updateSemester(data.semester);

      const newStudyId = studyPrograms.find(
          (studyProgram) => studyProgram.title === data.studyProgram,
      )?.id

      if (newStudyId) {
        updateStudyId(newStudyId);
      }
      setIsDialogOpen(false);
    }
  };

  return (
    <form
      onSubmit={handleSubmit((data) => onSubmit(data))}
      className="w-2/3 space-y-6"
    >
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Select Study Program</DialogTitle>
          <DialogDescription>
            Select a study program and semester you want to explore or create a
            schedule for. This can always be changed via the menu at the top
            left.
          </DialogDescription>
        </DialogHeader>

        <p className="text-sm leading-none font-medium select-none">
          Study Program
        </p>
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger className="w-[300px]" asChild>
            <Button
              variant="outline"
              role="combobox"
              aria-expanded={open}
              className="w-[300px] justify-between"
            >
              <p className="truncate">
                {getValues("studyProgram")
                  ? studyPrograms.find(
                      (studyProgram) =>
                        studyProgram.title === getValues("studyProgram"),
                    )?.title
                  : "Select a Study Program..."}
              </p>
              <ChevronsUpDown className="opacity-50" />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-[300px] p-0">
            <Command>
              <CommandInput
                placeholder="Search Study Program..."
                className="h-9"
              />
              <CommandList>
                <CommandEmpty>No study programs found.</CommandEmpty>
                <CommandGroup className="w-[300px]">
                  {studyPrograms.map((studyProgram) => (
                    <CommandItem
                      className="w-[300px] text-ellipsis"
                      key={studyProgram.id}
                      value={studyProgram.id}
                      onSelect={(currentValue) => {
                        let semesters = studyPrograms
                          .filter((p) => p.title === currentValue)
                          .map((p) => p.semesters)
                          .at(0);

                        if (semesters) {
                          setSem(semesters[0]);
                          setValue("semester", semesters[0]);
                          setLocalSemesters(semesters);
                          setValue("studyProgram", currentValue);
                          setOpen(false);
                        }
                      }}
                    >
                      <p className="truncate">{studyProgram.title}</p>
                    </CommandItem>
                  ))}
                </CommandGroup>
              </CommandList>
            </Command>
          </PopoverContent>
        </Popover>
        <p className="text-sm leading-none font-medium select-none">Semester</p>
        <Select
          onValueChange={(currentValue) => {
            setSem(currentValue);
            setValue("semester", currentValue);
          }}
          value={sem}
        >
          <SelectTrigger>
            <SelectValue placeholder="Select a Semester" />
          </SelectTrigger>
          <SelectContent>
            {localSemesters.map((s) => (
              <SelectItem value={s} key={s}>
                {s}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <DialogFooter>
          <Button onClick={handleSubmit(onSubmit)}>Save changes</Button>
        </DialogFooter>
      </DialogContent>
    </form>
  );
}
