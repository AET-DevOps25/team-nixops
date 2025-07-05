"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import Link from "next/link";
import { useForm } from "react-hook-form";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useEffect, useState } from "react";

export function StudyProgramSelector({
  setStudyProgramId,
  setSemester,
  setIsDialogOpen,
}) {
  const [studyPrograms, setStudyPrograms] = useState([]);
  const [localSemesters, setLocalSemesters] = useState([]);

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
    console.log(data);
    setSemester(data.semester);
    setStudyProgramId(data.studyProgram);
    setIsDialogOpen(false);
  };
  const form = useForm();
  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((data) => console.log(data))}
        className="w-2/3 space-y-6"
      >
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Select Study Program</DialogTitle>
            <DialogDescription>
              Select a study program and semester you want to explore or
              schedule. This can always be changed via the menu at the top left.
            </DialogDescription>
          </DialogHeader>

          <FormField
            control={form.control}
            name="studyProgram"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Study Program</FormLabel>
                <Select
                  onValueChange={(data) => {
                    field.onChange(data);
                    setLocalSemesters(
                      studyPrograms
                        .filter((p) => p.id === data)
                        .map((p) => p.semesters)
                        .at(0),
                    );
                  }}
                  defaultValue={field.value}
                >
                  <FormControl>
                    <SelectTrigger className="w-[320px]">
                      <SelectValue placeholder="Select a Study Program" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent >
                    {studyPrograms.map((p) => (
                      <SelectItem value={p.id} key={p.id}>
                        {p.title}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="semester"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Semester</FormLabel>
                <Select
                  onValueChange={field.onChange}
                  defaultValue={field.value}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select a Semester" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {localSemesters.map((s) => (
                      <SelectItem value={s} key={s}>
                        {s}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormItem>
            )}
          />
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={form.handleSubmit(onSubmit)}>Save changes</Button>
          </DialogFooter>
        </DialogContent>
      </form>
    </Form>
  );
}
