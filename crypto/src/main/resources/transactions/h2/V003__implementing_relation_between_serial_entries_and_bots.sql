create table bot (
  id_bot uuid not null,
  bot_name nvarchar(256) not null,
  bot_state int not null
);

alter table bot
  add constraint PK_bot
    primary key (id_bot);

alter table bot
  alter column id_bot
    set default random_uuid();

alter table serial_entry
  add id_bot uuid;

alter table serial_entry
  add constraint FK_Serial_entry_Bot
    foreign key (id_bot)
    references bot (id_bot);