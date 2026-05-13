import { useEffect, useState } from 'react';
import { api } from '../api/client';

export default function Home() {
  const [data, setData] = useState<any[]>([]);

  useEffect(() => {
    api.get<any[]>('/sample-entities')
      .then(setData)
      .catch(() => setData([]));
  }, []);

  return (
    <div>
      <h2>Welcome to __project_title__</h2>
      <p>Sample entities: {data.length}</p>
    </div>
  );
}