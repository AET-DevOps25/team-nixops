import { useState, useEffect, useCallback } from 'react';
import { v4 as uuidv4 } from 'uuid';

export function useSessionData() {
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [semester, setSemester] = useState<string | null>(null);
  const [studyId, setStudyId] = useState<string | null>(null);

  const generateNewSessionId = useCallback(() => {
    const newId = uuidv4();
    sessionStorage.setItem('sessionId', newId);
    setSessionId(newId);
  }, []);

  useEffect(() => {
    const storedSessionId = sessionStorage.getItem('sessionId');
    const storedSemester = sessionStorage.getItem('semester');
    const storedStudyId = sessionStorage.getItem('studyId');

    if (storedSemester) setSemester(storedSemester);
    if (storedStudyId) setStudyId(storedStudyId);

    if (storedSessionId) {
      setSessionId(storedSessionId);
    } else {
      generateNewSessionId();
    }
  }, [generateNewSessionId]);

  const updateSemester = (newSemester: string) => {
    sessionStorage.setItem('semester', newSemester);
    setSemester(newSemester);
  };

  const updateStudyId = (newStudyId: string) => {
    sessionStorage.setItem('studyId', newStudyId);
    setStudyId(newStudyId);
  };

  useEffect(() => {
    if (semester !== null && studyId !== null) {
      generateNewSessionId();
    }
  }, [semester, studyId, generateNewSessionId]);

  return {
    sessionId,
    semester,
    studyId,
    updateSemester,
    updateStudyId,
  };
}
