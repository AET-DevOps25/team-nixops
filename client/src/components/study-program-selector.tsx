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

export function StudyProgramSelector() {
  const onSubmit = (data: any) => {
    console.log(data);
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
                  onValueChange={field.onChange}
                  defaultValue={field.value}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue placeholder="Select a Study Program" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value="M.Sc. Informatik">
                      M.Sc. Informatik
                    </SelectItem>
                    <SelectItem value="M.Sc. Elektrotechnik">
                      M.Sc. Elektrotechnik
                    </SelectItem>
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
                    <SelectItem value="2025w">2025w</SelectItem>
                    <SelectItem value="2025s">2025s</SelectItem>
                  </SelectContent>
                </Select>
              </FormItem>
            )}
          />
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={() => form.handleSubmit(onSubmit)()}>
              Save changes
            </Button>
          </DialogFooter>
        </DialogContent>
      </form>
    </Form>
  );
}
