import React, { createContext, useContext, ReactNode } from 'react';
import { useSessionData } from './session';

interface SessionContextType {
  sessionId: string | null;
  studyId: number | null;
  semester: string | null;
  resetSession: () => void;
  updateSemester: (newSemester: string) => void;
  updateStudyId: (newStudyId: string) => void;
}

const SessionContext = createContext<SessionContextType | undefined>(undefined);

interface SessionProviderProps {
  children: ReactNode;
}

export function SessionProvider({ children }: SessionProviderProps) {
  const {
    sessionId,
    semester,
    studyId: rawStudyId,
    resetSession,
    updateSemester,
    updateStudyId,
  } = useSessionData();

  const studyId = rawStudyId ? Number(rawStudyId) : null;

  return (
    <SessionContext.Provider
      value={{
        sessionId,
        semester,
        studyId,
        resetSession,
        updateSemester,
        updateStudyId,
      }}
    >
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return context;
}
