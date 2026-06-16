-- Schema manuale tabella preventivo (PostgreSQL / Supabase)
-- Esegui questo script una sola volta nel database target.

create extension if not exists pgcrypto;

create table if not exists preventivo (
    id uuid primary key default gen_random_uuid(),
    created_at timestamptz not null default now(),
    nome varchar(255) not null,
    email varchar(255) not null,
    telefono varchar(255),
    appartamento varchar(255),
    check_in date,
    check_out date,
    persone integer,
    messaggio text,
    preferenza_ricontatto varchar(255),
    source varchar(255)
);

create index if not exists idx_preventivo_created_at on preventivo(created_at desc);
create index if not exists idx_preventivo_email on preventivo(email);
