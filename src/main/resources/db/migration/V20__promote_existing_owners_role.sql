-- V20: Promote users who have an active owner profile to ROLE_OWNER
UPDATE users u
SET role = 'ROLE_OWNER'
WHERE EXISTS (
    SELECT 1 FROM owners o WHERE o.user_id = u.id AND o.deleted_at IS NULL
)
AND u.role = 'ROLE_USER';
