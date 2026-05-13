import os

BASE = 'x:/Unicornator/backend/src/main/resources/templates/spring-react'

files = {}

# ── pom.xml ──
files['backend/pom.xml'] = '''<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.0</version>
    </parent>
    <groupId>com.__package__</groupId>
    <artifactId>__project_slug__</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>__project_title__</name>
    <description>__project_description__</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-database-postgresql</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
        </plugins>
    </build>
</project>'''

# ── Application.java ──
files['backend/src/main/java/com/__package__/__AppName__Application.java'] = '''package com.__package__;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class __AppName__Application {
    public static void main(String[] args) {
        SpringApplication.run(__AppName__Application.class, args);
    }
}'''

# ── application.properties ──
files['backend/src/main/resources/application.properties'] = '''spring.application.name=__project_slug__
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
'''

# ── SecurityConfig.java ──
files['backend/src/main/java/com/__package__/config/SecurityConfig.java'] = '''package com.__package__.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}'''

# ── HealthController.java ──
files['backend/src/main/java/com/__package__/controller/HealthController.java'] = '''package com.__package__.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "__project_slug__"));
    }
}'''

# ── SampleEntity.java ──
files['backend/src/main/java/com/__package__/entity/SampleEntity.java'] = '''package com.__package__.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sample_entities")
public class SampleEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}'''

# ── SampleEntityRepository.java ──
files['backend/src/main/java/com/__package__/repository/SampleEntityRepository.java'] = '''package com.__package__.repository;

import com.__package__.entity.SampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleEntityRepository extends JpaRepository<SampleEntity, Long> {}'''

# ── SampleEntityService.java ──
files['backend/src/main/java/com/__package__/service/SampleEntityService.java'] = '''package com.__package__.service;

import com.__package__.entity.SampleEntity;
import com.__package__.repository.SampleEntityRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SampleEntityService {
    private final SampleEntityRepository repo;
    public SampleEntityService(SampleEntityRepository repo) { this.repo = repo; }

    public List<SampleEntity> findAll() { return repo.findAll(); }
    public SampleEntity create(String name, String description) {
        SampleEntity e = new SampleEntity();
        e.setName(name); e.setDescription(description);
        return repo.save(e);
    }
}'''

# ── SampleEntityController.java ──
files['backend/src/main/java/com/__package__/controller/SampleEntityController.java'] = '''package com.__package__.controller;

import com.__package__.entity.SampleEntity;
import com.__package__.service.SampleEntityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sample-entities")
public class SampleEntityController {
    private final SampleEntityService service;
    public SampleEntityController(SampleEntityService service) { this.service = service; }

    @GetMapping
    public List<SampleEntity> getAll() { return service.findAll(); }

    @PostMapping
    public ResponseEntity<SampleEntity> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.create(body.get("name"), body.get("description")));
    }
}'''

# ── V1__init.sql ──
files['backend/src/main/resources/db/migration/V1__init.sql'] = '''CREATE TABLE IF NOT EXISTS sample_entities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
'''

# ── ApplicationTests.java ──
files['backend/src/test/java/com/__package__/__AppName__ApplicationTests.java'] = '''package com.__package__;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class __AppName__ApplicationTests {
    @Test
    void contextLoads() {}
}'''

# ── Frontend files ──
files['frontend/package.json'] = '''{
  "name": "__project_slug__",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest"
  },
  "dependencies": {
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.26.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.1",
    "typescript": "^5.5.3",
    "vite": "^5.4.0",
    "vitest": "^2.0.0"
  }
}'''

files['frontend/tsconfig.json'] = '''{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true
  },
  "include": ["src"]
}'''

files['frontend/vite.config.ts'] = '''import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { port: 3000, proxy: { '/api': 'http://localhost:8080' } }
});'''

files['frontend/index.html'] = '''<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"/><meta name="viewport" content="width=device-width,initial-scale=1.0"/>
<title>__project_title__</title></head>
<body><div id="root"></div><script type="module" src="/src/main.tsx"></script></body>
</html>'''

files['frontend/src/main.tsx'] = '''import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode><App /></React.StrictMode>
);'''

files['frontend/src/App.tsx'] = '''import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
export default App;'''

files['frontend/src/api/client.ts'] = '''const API_BASE = '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });
  if (!res.ok) throw new Error(`API error: ${res.status}`);
  return res.json();
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) => request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
};'''

files['frontend/src/components/Layout.tsx'] = '''import { Outlet } from 'react-router-dom';

export default function Layout() {
  return (
    <div>
      <nav style={{ padding: '1rem', background: '#f0f0f0' }}>
        <h1>__project_title__</h1>
      </nav>
      <main style={{ padding: '1rem' }}><Outlet /></main>
    </div>
  );
}'''

files['frontend/src/pages/Home.tsx'] = '''import { useEffect, useState } from 'react';
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
}'''

files['frontend/src/hooks/useApi.ts'] = '''import { useState, useEffect } from 'react';
import { api } from '../api/client';

export function useApi<T>(path: string) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<T>(path).then(setData).finally(() => setLoading(false));
  }, [path]);

  return { data, loading };
}'''

files['frontend/src/types/index.ts'] = '''export interface SampleEntity {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}'''

files['frontend/tests/App.test.tsx'] = '''import { describe, it, expect } from 'vitest';

describe('App', () => {
  it('renders without crashing', () => {
    expect(true).toBe(true);
  });
});'''

# Write all files
for relpath, content in files.items():
    full = os.path.join(BASE, relpath)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        f.write(content.lstrip('\n'))
    print(f"  Wrote: {relpath}")

print(f"\n✅ Created {len(files)} template files")
