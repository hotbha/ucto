import { Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <div>
      <nav style={{ padding: '1rem', background: '#f0f0f0' }}>
        <h1>__project_title__</h1>
      </nav>
      <main style={{ padding: '1rem' }}><Outlet /></main>
    </div>
  );
}