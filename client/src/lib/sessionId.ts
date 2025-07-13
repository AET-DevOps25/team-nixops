import { useState, useEffect, useCallback } from 'react';
import { v4 as uuidv4 } from 'uuid';

export function useSessionId() {
  const [sessionId, setSessionId] = useState<string | null>(null);

  const generateNewSessionId = useCallback(() => {
    const newId = uuidv4();
    sessionStorage.setItem('sessionId', newId);
    setSessionId(newId);
  }, []);

  useEffect(() => {
    let id = sessionStorage.getItem('sessionId');
    if (!id) {
      generateNewSessionId();
    } else {
      setSessionId(id);
    }
  }, [generateNewSessionId]);

  const resetSession = () => {
    sessionStorage.removeItem('sessionId');
    generateNewSessionId();
  };

  return { sessionId, resetSession };
}
