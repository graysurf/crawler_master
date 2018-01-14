create or replace function public.update_changetimestamp_column()
  returns trigger as $$
begin
  NEW.time_last_update = now();
  return NEW;
end;
$$ language 'plpgsql';

create schema master;
set search_path = master;

create table container_log
(
  seqno            serial                                 not null,
  container_name   text                                   not null,
  task_id          integer                                not null,
  crawler_id       integer                                not null,
  instance_name    text                                   not null,
  ip               text                                   not null,
  status           text                                   not null,
  time_insert      timestamp with time zone default now() not null,
  time_last_update timestamp with time zone
);

create trigger container_log_changetimestamp
before update
  on container_log
for each row execute procedure
  public.update_changetimestamp_column();

create table instance_log
(
  seqno            serial                                 not null,
  instance_name    text                                   not null,
  task_id          integer                                not null,
  action           text                                   not null,
  container_num    integer                                not null,
  container_ids    integer []                             not null,
  ip               text                                   not null,
  time_insert      timestamp with time zone default now() not null,
  time_last_update timestamp with time zone
);

create trigger instance_log_changetimestamp
before update
  on instance_log
for each row execute procedure
  public.update_changetimestamp_column();

create table instance_state
(
  seqno            serial                                 not null,
  instance_name    text primary key                       not null,
  task_id          integer                                not null,
  status           text                                   not null,
  ip               text                                   not null,
  active_time      timestamp                              not null,
  stop_time        timestamp,
  container_num    integer                                not null,
  container_ids    integer []                             not null,
  time_insert      timestamp with time zone default now() not null,
  time_last_update timestamp with time zone
);

create trigger instance_state_changetimestamp
before update
  on instance_state
for each row execute procedure
  public.update_changetimestamp_column();

create table task_instance_scale
(
  seqno            serial                                 not null,
  task_id          integer                                not null,
  action           text                                   not null,
  instance_num     integer                                not null,
  instance_names   text []                                not null,
  next_crawler_ids integer []                             not null,
  next_crawl_time  timestamp,
  time_insert      timestamp with time zone default now() not null,
  time_last_update timestamp with time zone
);

create trigger task_instance_scale_changetimestamp
before update
  on task_instance_scale
for each row execute procedure
  public.update_changetimestamp_column();

create table task_log
(
  seqno            serial                                 not null,
  task_id          integer                                not null,
  action           text                                   not null,
  time_insert      timestamp with time zone default now() not null,
  time_last_update timestamp with time zone
);

create trigger task_log_changetimestamp
before update
  on task_log
for each row execute procedure
  public.update_changetimestamp_column();