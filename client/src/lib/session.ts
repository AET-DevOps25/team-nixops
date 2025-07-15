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

  const updateStudyId = (newStudyId: string) => {
    if (studyId !== null && newStudyId !== studyId) {
      sessionStorage.setItem('studyId', newStudyId);
      setStudyId(newStudyId);
      generateNewSessionId();
    }
  };

  const updateSemester = (newSemester: string) => {
    if (semester !== null && newSemester !== semester) {
      sessionStorage.setItem('semester', newSemester);
      setSemester(newSemester);
      generateNewSessionId();
    }
  };

  return {
    sessionId,
    semester,
    studyId,
    updateSemester,
    updateStudyId,
  };
}
