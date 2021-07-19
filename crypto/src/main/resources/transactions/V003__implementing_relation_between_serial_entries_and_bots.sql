BEGIN TRANSACTION
GO

	CREATE TABLE bot(
		id_bot		UNIQUEIDENTIFIER	NOT NULL,
		bot_name	NVARCHAR(256)		NOT NULL,
		bot_state	INT					NOT NULL
	)
	GO

	ALTER TABLE bot
		ADD CONSTRAINT PK_bot
			PRIMARY KEY (id_bot)
	GO

	ALTER TABLE serial_entry
		ADD id_bot UNIQUEIDENTIFIER
	GO

	ALTER TABLE serial_entry
		ADD CONSTRAINT FK_Serial_entry_Bot
			FOREIGN KEY (id_bot)
			REFERENCES bot(id_bot)
	GO

COMMIT
GO