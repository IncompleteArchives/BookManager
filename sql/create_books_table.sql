CREATE TABLE IF NOT EXISTS public.books (
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    publication_year integer,
    author_id integer,
    available_copies INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT books_pkey PRIMARY KEY (id),
    CONSTRAINT fk_books_author FOREIGN KEY (author_id)
        REFERENCES public.authors (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)