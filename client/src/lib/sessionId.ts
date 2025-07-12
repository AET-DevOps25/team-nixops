import { useState, useEffect } from 'react';
import { v4 as uuidv4 } from 'uuid'; // make sure you run: npm install uuid

export function useSessionId() {
  const [sessionId, setSessionId] = useState(null);

  useEffect(() => {
    let id = sessionStorage.getItem('sessionId');
    if (!id) {
      id = uuidv4();
      sessionStorage.setItem('sessionId', id);
    }
    setSessionId(id);
  }, []);

  return sessionId;
}
