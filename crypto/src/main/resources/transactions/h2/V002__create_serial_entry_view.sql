create view "bot_trade_results"
as
select
  serial_entry.*,
  trade_movement.ammount,
  trade_movement.total_value,
  trade_movement.trade_movement_type,
  trade_movement.profit
from serial_entry
  left outer join trade_movement
    on serial_entry.id_serial_entry = trade_movement.id_serial_entry