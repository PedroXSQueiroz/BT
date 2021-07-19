CREATE VIEW [dbo].[bot_trade_results]
AS
	SELECT
		serial_entry.*,
		trade_movement.ammount,
		trade_movement.total_value,
		trade_movement.trade_movement_type,
		trade_movement.profit
	FROM serial_entry
		LEFT JOIN trade_movement ON serial_entry.id_serial_entry = trade_movement.id_serial_entry
GO


