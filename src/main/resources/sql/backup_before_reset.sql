--
-- PostgreSQL database dump
--

-- Dumped from database version 15.2 (Debian 15.2-1.pgdg110+1)
-- Dumped by pg_dump version 15.2 (Debian 15.2-1.pgdg110+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: get_active_trips_count_by_route(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_active_trips_count_by_route(route_id_param integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
active_count INTEGER;
BEGIN
SELECT COUNT(*) INTO active_count
FROM journal
WHERE route_id = route_id_param AND time_in IS NULL;

RETURN COALESCE(active_count, 0);
END;
$$;


ALTER FUNCTION public.get_active_trips_count_by_route(route_id_param integer) OWNER TO postgres;

--
-- Name: get_fastest_trip_by_route(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_fastest_trip_by_route(route_id_param integer) RETURNS TABLE(auto_id integer, auto_num character varying, auto_mark character varying, route_name character varying, trip_duration text, time_out timestamp without time zone, time_in timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
RETURN QUERY
SELECT
    j.auto_id,
    a.num as auto_num,
    a.mark as auto_mark,
    r.name as route_name,
    (j.time_in - j.time_out)::text as trip_duration,
    j.time_out,
    j.time_in
FROM journal j
         JOIN auto a ON j.auto_id = a.id
         JOIN routes r ON j.route_id = r.id
WHERE j.route_id = route_id_param
  AND j.time_in IS NOT NULL
  AND j.time_out IS NOT NULL
ORDER BY (j.time_in - j.time_out) ASC
LIMIT 1;
END;
$$;


ALTER FUNCTION public.get_fastest_trip_by_route(route_id_param integer) OWNER TO postgres;

--
-- Name: verify_car_time_in(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.verify_car_time_in() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM journal
        WHERE auto_id = NEW.auto_id AND time_in IS NULL
    ) THEN
        RAISE EXCEPTION 'Автомобиль с id % еще не вернулся из рейса!', NEW.auto_id;
    END IF;

RETURN NEW;
END;
$$;


ALTER FUNCTION public.verify_car_time_in() OWNER TO postgres;

--
-- Name: verify_car_time_out(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.verify_car_time_out() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN

	IF NEW.time_in IS NOT NULL THEN
        IF NEW.time_in < NEW.time_out THEN
            RAISE EXCEPTION 'Время прибытия (%) не может быть меньше времени отправления (%)!', NEW.time_in, NEW.time_out;
        END IF;

        IF NEW.time_in > CURRENT_TIMESTAMP THEN
            RAISE EXCEPTION 'Время прибытия (%) не может быть в будущем!', NEW.time_in;
        END IF;
    END IF;

    IF NEW.time_out > CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'Время отправления (%) не может быть в будущем!', NEW.time_out;
    END IF;

RETURN NEW;
END;
$$;


ALTER FUNCTION public.verify_car_time_out() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: auto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auto (
    id integer NOT NULL,
    num character varying(255) NOT NULL,
    color character varying(255) NOT NULL,
    mark character varying(255) NOT NULL,
    personal_id integer NOT NULL
);


ALTER TABLE public.auto OWNER TO postgres;

--
-- Name: auto_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auto_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_id_seq OWNER TO postgres;

--
-- Name: auto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.auto_id_seq OWNED BY public.auto.id;


--
-- Name: auto_personal; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auto_personal (
    id integer NOT NULL,
    first_name character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    father_name character varying(255)
);


ALTER TABLE public.auto_personal OWNER TO postgres;

--
-- Name: auto_personal_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auto_personal_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.auto_personal_id_seq OWNER TO postgres;

--
-- Name: auto_personal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.auto_personal_id_seq OWNED BY public.auto_personal.id;


--
-- Name: journal; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.journal (
    id integer NOT NULL,
    time_in timestamp(0) without time zone,
    auto_id integer NOT NULL,
    route_id integer NOT NULL,
    time_out timestamp(0) without time zone
);


ALTER TABLE public.journal OWNER TO postgres;

--
-- Name: journal_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.journal_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.journal_id_seq OWNER TO postgres;

--
-- Name: journal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.journal_id_seq OWNED BY public.journal.id;


--
-- Name: mythology; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.mythology (
    id bigint NOT NULL,
    name character varying(50) NOT NULL
);


ALTER TABLE public.mythology OWNER TO postgres;

--
-- Name: mythology_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.mythology ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.mythology_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: routes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.routes (
    id integer NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.routes OWNER TO postgres;

--
-- Name: routes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.routes_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.routes_id_seq OWNER TO postgres;

--
-- Name: routes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.routes_id_seq OWNED BY public.routes.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: auto id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auto ALTER COLUMN id SET DEFAULT nextval('public.auto_id_seq'::regclass);


--
-- Name: auto_personal id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auto_personal ALTER COLUMN id SET DEFAULT nextval('public.auto_personal_id_seq'::regclass);


--
-- Name: journal id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal ALTER COLUMN id SET DEFAULT nextval('public.journal_id_seq'::regclass);


--
-- Name: routes id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.routes ALTER COLUMN id SET DEFAULT nextval('public.routes_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: auto; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.auto (id, num, color, mark, personal_id) FROM stdin;
1	а587те	Белый	Фольксваген	1
2	г647тн	Белый	Лисян	1
3	у901ов	Белый	Тесла	2
4	р001мр	Черный	УАЗ	2
5	р555аа	Черный	Фредлайнер	3
6	а777мр	Черный	Мерседес	4
7	у321тт	Синий	АвтоВАЗ	4
8	а522ам	Голубой	АвтоВАЗ	5
9	е456то	Красный	Ниссан	5
\.


--
-- Data for Name: auto_personal; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.auto_personal (id, first_name, last_name, father_name) FROM stdin;
1	Александр	Никифоров	Вячеславович
2	Александр	Псарев	Сергеевич
3	Семён	Зинаков	Сергеевич
4	Михаил	Соболевский	Андреевич
5	Артем	Ананьев	Максимович
\.


--
-- Data for Name: journal; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.journal (id, time_in, auto_id, route_id, time_out) FROM stdin;
1	2025-09-30 08:45:00	1	1	2025-09-30 08:00:00
2	2025-09-30 10:00:00	2	2	2025-09-30 09:15:00
3	2025-09-30 11:15:00	3	1	2025-09-30 10:30:00
4	2025-09-30 12:40:00	4	3	2025-09-30 12:00:00
5	2025-09-21 08:35:00	1	1	2025-09-21 08:00:00
6	\N	2	2	2024-05-21 14:00:00
7	\N	3	1	2024-05-21 14:30:00
8	2024-05-20 11:50:00	5	4	2024-05-20 11:00:00
9	2024-05-20 13:45:00	6	4	2024-05-20 13:00:00
10	2024-05-21 09:50:00	1	2	2024-05-21 09:00:00
11	2024-05-21 09:30:00	2	1	2024-05-21 09:00:00
\.


--
-- Data for Name: mythology; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.mythology (id, name) FROM stdin;
\.


--
-- Data for Name: routes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.routes (id, name) FROM stdin;
1	Центр - Крестовский остров
2	Ленинградский Вокзал - Аэропорт
3	Балтийский вокзал - Финляндский вокзал
4	Общежитие №6 - СПБПУ
5	Кузьмолово - СПБПУ
6	Академ-парк - СПБПУ
7	Купчино - СПБПУ
8	Девяткино - СПБПУ
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password_hash, role, enabled, created_at) FROM stdin;
1	admin	$2a$10$6/D1t9IMJXwNHyE2hEEy/e0ArI5KPxUZeQF6ry6r.yD2NTJEziTi2	ADMIN	t	2025-12-19 12:46:25.650214
2	user	$2a$10$itCYCNUaq.5Zr4bYlXwzUe/Tl7MyNlsmac6HubUmgGnhU8gVZlvGK	USER	t	2025-12-19 12:46:25.79981
3	Саша	$2a$10$.vQGmy/OQBCzu/JJRcjmIufotTULas1..V3j6laeuhqdvI2uHvBfe	OPERATOR	t	2025-12-19 12:48:32.149244
\.


--
-- Name: auto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auto_id_seq', 9, true);


--
-- Name: auto_personal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auto_personal_id_seq', 5, true);


--
-- Name: journal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.journal_id_seq', 11, true);


--
-- Name: mythology_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.mythology_id_seq', 1, false);


--
-- Name: routes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.routes_id_seq', 8, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 3, true);


--
-- Name: auto auto_num_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auto
    ADD CONSTRAINT auto_num_key UNIQUE (num);


--
-- Name: auto_personal auto_personal_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auto_personal
    ADD CONSTRAINT auto_personal_pkey PRIMARY KEY (id);


--
-- Name: auto auto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auto
    ADD CONSTRAINT auto_pkey PRIMARY KEY (id);


--
-- Name: journal journal_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal
    ADD CONSTRAINT journal_pkey PRIMARY KEY (id);


--
-- Name: mythology mythology_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.mythology
    ADD CONSTRAINT mythology_pkey PRIMARY KEY (id);


--
-- Name: routes routes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.routes
    ADD CONSTRAINT routes_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: journal verify_car_time_in; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER verify_car_time_in BEFORE INSERT ON public.journal FOR EACH ROW EXECUTE FUNCTION public.verify_car_time_in();


--
-- Name: journal verify_car_time_out; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER verify_car_time_out BEFORE UPDATE ON public.journal FOR EACH ROW EXECUTE FUNCTION public.verify_car_time_out();


--
-- Name: auto fk_auto_auto_personal; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auto
    ADD CONSTRAINT fk_auto_auto_personal FOREIGN KEY (personal_id) REFERENCES public.auto_personal(id) ON DELETE CASCADE;


--
-- Name: journal fk_journal_auto; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal
    ADD CONSTRAINT fk_journal_auto FOREIGN KEY (auto_id) REFERENCES public.auto(id) ON DELETE CASCADE;


--
-- Name: journal fk_journal_routes; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.journal
    ADD CONSTRAINT fk_journal_routes FOREIGN KEY (route_id) REFERENCES public.routes(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

