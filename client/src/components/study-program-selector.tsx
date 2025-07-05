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
import { Dialog } from "@radix-ui/react-dialog";
import { useEffect, useState } from "react";
import { Label } from "@radix-ui/react-select";

export function StudyProgramSelector({
  setStudyProgramId,
  setSemester,
  setIsDialogOpen,
}) {
  const [open, setOpen] = useState(false);
  const [sem, setSem] = useState("");
  const [studyPrograms, setStudyPrograms] = useState([]);
  const [localSemesters, setLocalSemesters] = useState([]);
  const { register, handleSubmit, reset, getValues, setValue } = useForm();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(
          "http://127.0.0.1:8000/embed/studyPrograms",
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
	  console.log(data)
    if (data.studyProgram && data.semester) {
      setSemester(data.semester);
      setStudyProgramId(data.studyProgram);
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
            Select a study program and semester you want to explore or create a schedule for.
            This can always be changed via the menu at the top left.
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
                <CommandEmpty>No framework found.</CommandEmpty>
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

                        setSem(semesters[0]);
                        setValue("semester", semesters[0]);
                        setLocalSemesters(semesters);
                        setValue("studyProgram", currentValue);
                        setOpen(false);
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
          <DialogClose asChild>
            <Button variant="outline">Cancel</Button>
          </DialogClose>
          <Button onClick={handleSubmit(onSubmit)}>Save changes</Button>
        </DialogFooter>
      </DialogContent>
    </form>
  );
}
