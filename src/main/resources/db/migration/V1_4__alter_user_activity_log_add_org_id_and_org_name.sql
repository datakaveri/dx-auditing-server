-- Migration script to alter user_activity_log table to add org_id and org_name columns
ALTER TABLE user_activity_log
ADD COLUMN org_name VARCHAR(512),
ADD COLUMN org_id UUID;

--add indexes for the columns
CREATE INDEX idx_user_activity_log_user_id ON user_activity_log(user_id);
CREATE INDEX idx_user_activity_log_org_id ON user_activity_log(org_id);
CREATE INDEX idx_user_activity_log_operation ON user_activity_log(operation);
CREATE INDEX idx_user_activity_log_asset_id ON user_activity_log(asset_id);
CREATE INDEX idx_user_activity_log_asset_type ON user_activity_log(asset_type);
CREATE INDEX idx_user_activity_log_created_at ON user_activity_log(created_at);