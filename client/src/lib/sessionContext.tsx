import React, { createContext, useContext, ReactNode } from 'react';
import { useSessionId } from './sessionId';

const SessionContext = createContext<string | null>(null);

interface SessionProviderProps {
  children: ReactNode;
}

export function SessionProvider({ children }: SessionProviderProps) {
  const sessionId = useSessionId();

  return (
    <SessionContext.Provider value={sessionId}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  return useContext(SessionContext);
}

