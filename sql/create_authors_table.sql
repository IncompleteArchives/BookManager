CREATE TABLE IF NOT EXISTS public.authors (
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    first_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    middle_name character varying(100) COLLATE pg_catalog."default",
    last_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    gender character varying(20) COLLATE pg_catalog."default",
    birth_date date,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT authors_pkey PRIMARY KEY (id)
)