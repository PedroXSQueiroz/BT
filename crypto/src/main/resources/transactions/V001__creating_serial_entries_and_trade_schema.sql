BEGIN TRANSACTION
GO

	CREATE TABLE serial_entry(
		id_serial_entry		UNIQUEIDENTIFIER	NOT NULL,
		entry_date			DATETIME			NOT NULL,
		opening_price		FLOAT				NOT NULL,
		closing_price		FLOAT				NOT NULL,
		max_price			FLOAT				NOT NULL,
		min_price			FLOAT				NOT NULL,
		variance_price		FLOAT
	)
	GO

	ALTER TABLE serial_entry
		ADD CONSTRAINT DF_PK_Serial_entry
			DEFAULT NEWID() FOR id_serial_entry
	GO

	ALTER TABLE serial_entry
		ADD CONSTRAINT PK_Serial_entry
			PRIMARY KEY (id_serial_entry)
	GO



	CREATE TABLE trade_movement(
		id_trade_movement			UNIQUEIDENTIFIER	NOT NULL,
		ammount						FLOAT				NOT NULL,
		total_value					FLOAT				NOT NULL,
		trade_movement_type			INT					NOT NULL,
		profit						FLOAT,
		id_serial_entry				UNIQUEIDENTIFIER,
		id_related_trade_movement	UNIQUEIDENTIFIER
	)
	GO

	ALTER TABLE trade_movement
		ADD CONSTRAINT PK_trade_movement
			PRIMARY KEY (id_trade_movement)
	GO

	ALTER TABLE trade_movement
		ADD CONSTRAINT DF_PK_trade_movment
			DEFAULT NEWID() FOR id_trade_movement
	GO

	ALTER TABLE trade_movement
		ADD CONSTRAINT FK_Trade_movement_Serial_entry
			FOREIGN KEY (id_serial_entry)
			REFERENCES serial_entry(id_serial_entry)
	GO

	ALTER TABLE trade_movement
		ADD CONSTRAINT FK_Trade_movement_Related_trade_movement
			FOREIGN KEY (id_related_trade_movement)
			REFERENCES trade_movement(id_trade_movement)
	GO

COMMIT
GO