import { useState, useEffect } from 'react';
import { api } from '../api/client';

export function useApi<T>(path: string) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<T>(path).then(setData).finally(() => setLoading(false));
  }, [path]);

  return { data, loading };
}