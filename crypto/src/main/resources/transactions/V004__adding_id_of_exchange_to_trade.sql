BEGIN TRANSACTION
GO

    ALTER TABLE trade_movement
        ADD market_id NVARCHAR(256)
    GO

COMMIT
GO