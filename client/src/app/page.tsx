"use client";

import Chat from "@/components/chat/component";
import { MenuOverlay } from "@/components/menu";
import { useState } from "react";

export default function Page() {
  const [studyProgramId, setStudyProgramId] = useState("0");
  const [semester, setSemester] = useState("");
  return (
    <>
      <Chat studyProgramId={studyProgramId} semester={semester} />
      <MenuOverlay
        setStudyProgramId={setStudyProgramId}
        setSemester={setSemester}
      />
    </>
  );
}
