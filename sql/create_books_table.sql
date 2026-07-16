CREATE TABLE IF NOT EXISTS public.books (
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    author character varying(255) COLLATE pg_catalog."default",
    publication_year integer,
    CONSTRAINT books_pkey PRIMARY KEY (id)
    );