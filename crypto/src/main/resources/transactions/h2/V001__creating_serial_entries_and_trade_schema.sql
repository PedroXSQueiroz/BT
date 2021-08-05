create table serial_entry (
  id_serial_entry uuid not null,
  entry_date timestamp not null,
  opening_price float not null,
  closing_price float not null,
  max_price float not null,
  min_price float not null,
  variance_price float
);

alter table serial_entry
  alter column id_serial_entry
    set default random_uuid();

alter table serial_entry
  add constraint PK_Serial_entry
    primary key (id_serial_entry);

create table trade_movement (
  id_trade_movement uuid not null,
  ammount float not null,
  total_value float not null,
  trade_movement_type int not null,
  profit float,
  id_serial_entry uuid,
  id_related_trade_movement uuid
);

alter table trade_movement
  add constraint PK_trade_movement
    primary key (id_trade_movement);

alter table trade_movement
  alter column id_trade_movement
    set default random_uuid();

alter table trade_movement
  add constraint FK_Trade_movement_Serial_entry
    foreign key (id_serial_entry)
    references serial_entry (id_serial_entry);

alter table trade_movement
  add constraint FK_Trade_movement_Related_trade_movement
    foreign key (id_related_trade_movement)
    references trade_movement (id_trade_movement);